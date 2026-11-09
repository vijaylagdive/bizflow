package com.bizflow.cashbook;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bizflow.account.Account;
import com.bizflow.account.AccountRepository;
import com.bizflow.common.PageResponse;
import com.bizflow.purchaseorder.PurchasePostingService;
import com.bizflow.salesorder.SalePostingService;

import jakarta.persistence.criteria.Predicate;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class CashbookService {

	private static final String TYPE_PAYMENT = "payment";
	private static final String TYPE_RECEIPT = "receipt";
	private static final String FROM_OTHER = "other";

	private final CashbookRepository cashbookRepository;
	private final AccountRepository accountRepository;
	private final PurchasePostingService purchasePostingService;
	private final SalePostingService salePostingService;

	public CashbookService(CashbookRepository cashbookRepository, AccountRepository accountRepository,
			PurchasePostingService purchasePostingService, SalePostingService salePostingService) {
		this.cashbookRepository = cashbookRepository;
		this.accountRepository = accountRepository;
		this.purchasePostingService = purchasePostingService;
		this.salePostingService = salePostingService;
	}

	public PageResponse<CashbookEntry> list(Long accountId, String transactionType, String fromTo, LocalDate fromDate,
			LocalDate toDate, Pageable pageable) {
		String type = normalize(transactionType);
		if (!type.isEmpty() && !TYPE_PAYMENT.equals(type) && !TYPE_RECEIPT.equals(type)) {
			throw new ResponseStatusException(BAD_REQUEST, "transactionType must be payment or receipt");
		}
		String party = fromTo == null ? "" : fromTo.trim();
		Specification<CashbookEntry> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (accountId != null) {
				predicates.add(cb.equal(root.get("accountId"), accountId));
			}
			if (!type.isEmpty()) {
				predicates.add(cb.equal(root.get("transactionType"), type));
			}
			if (!party.isEmpty()) {
				predicates.add(cb.equal(cb.lower(root.get("fromTo")), party.toLowerCase()));
			}
			if (fromDate != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("entryDate"), fromDate));
			}
			if (toDate != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("entryDate"), toDate));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
		return PageResponse.from(cashbookRepository.findAll(spec, pageable));
	}

	public CashbookEntry get(Long id) {
		return cashbookRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cashbook entry not found"));
	}

	public CashbookEntry create(CashbookRequest request) {
		CashbookEntry entry = new CashbookEntry();
		applyRequest(entry, request);
		CashbookEntry saved = cashbookRepository.save(entry);
		recalculateBalances();
		applyLedger(saved);
		return cashbookRepository.findById(saved.getId()).orElse(saved);
	}

	public CashbookEntry update(Long id, CashbookRequest request) {
		CashbookEntry entry = get(id);
		clearLedger(entry);
		applyRequest(entry, request);
		cashbookRepository.save(entry);
		recalculateBalances();
		applyLedger(entry);
		return cashbookRepository.findById(id).orElse(entry);
	}

	public void delete(Long id) {
		CashbookEntry entry = get(id);
		clearLedger(entry);
		cashbookRepository.delete(entry);
		recalculateBalances();
	}

	private void applyRequest(CashbookEntry entry, CashbookRequest request) {
		String transactionType = normalize(request.getTransactionType());
		if (!TYPE_PAYMENT.equals(transactionType) && !TYPE_RECEIPT.equals(transactionType)) {
			throw new ResponseStatusException(BAD_REQUEST, "transactionType must be payment or receipt");
		}
		String fromTo = request.getFromTo().trim();
		if (TYPE_PAYMENT.equals(transactionType) && !isOther(fromTo) && request.getPartyId() == null) {
			throw new ResponseStatusException(BAD_REQUEST, "partyId is required when paying a vendor");
		}
		if (TYPE_RECEIPT.equals(transactionType) && !isOther(fromTo) && request.getPartyId() == null) {
			throw new ResponseStatusException(BAD_REQUEST, "partyId is required when receiving from a customer");
		}
		Account account = accountRepository.findById(request.getAccountId())
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
		entry.setEntryDate(request.getDate());
		entry.setTransactionType(transactionType);
		entry.setAccountId(account.getAccountId());
		entry.setAccount(account.getName());
		entry.setFromTo(fromTo);
		entry.setPartyId(isOther(fromTo) ? null : request.getPartyId());
		entry.setAmount(request.getAmount());
		entry.setRemark(request.getRemark());
	}

	private void applyLedger(CashbookEntry entry) {
		if (isVendorPayment(entry)) {
			purchasePostingService.recordPayment(entry.getEntryDate(), entry.getPartyId(), entry.getFromTo(),
					entry.getAmount(), entry.getId());
		}
		else if (isCustomerReceipt(entry)) {
			salePostingService.recordReceipt(entry.getEntryDate(), entry.getPartyId(), entry.getFromTo(),
					entry.getAmount(), entry.getId());
		}
	}

	private void clearLedger(CashbookEntry entry) {
		if (isVendorPayment(entry)) {
			purchasePostingService.reverseCashbookPayment(entry.getId(), entry.getPartyId());
		}
		else if (isCustomerReceipt(entry)) {
			salePostingService.reverseCashbookReceipt(entry.getId(), entry.getPartyId());
		}
	}

	private static boolean isVendorPayment(CashbookEntry entry) {
		return TYPE_PAYMENT.equals(entry.getTransactionType()) && !isOther(entry.getFromTo())
				&& entry.getPartyId() != null;
	}

	private static boolean isCustomerReceipt(CashbookEntry entry) {
		return TYPE_RECEIPT.equals(entry.getTransactionType()) && !isOther(entry.getFromTo())
				&& entry.getPartyId() != null;
	}

	private static boolean isOther(String fromTo) {
		return FROM_OTHER.equalsIgnoreCase(fromTo);
	}

	private void recalculateBalances() {
		List<CashbookEntry> entries = cashbookRepository.findAllByOrderByEntryDateAscIdAsc();
		BigDecimal running = BigDecimal.ZERO;
		for (CashbookEntry entry : entries) {
			BigDecimal amount = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
			if (TYPE_RECEIPT.equals(entry.getTransactionType())) {
				running = running.add(amount);
			}
			else {
				running = running.subtract(amount);
			}
			entry.setBalance(running);
		}
		cashbookRepository.saveAll(entries);
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase();
	}

}
