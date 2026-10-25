package com.bizflow.customerledger;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bizflow.salesorder.SalePostingService;

import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/customer-ledgers")
public class CustomerLedgerController {

	private final CustomerLedgerRepository customerLedgerRepository;
	private final SalePostingService salePostingService;

	public CustomerLedgerController(CustomerLedgerRepository customerLedgerRepository,
			SalePostingService salePostingService) {
		this.customerLedgerRepository = customerLedgerRepository;
		this.salePostingService = salePostingService;
	}

	@GetMapping
	public List<CustomerLedger> list(@RequestParam(required = false) Long customerId) {
		if (customerId != null) {
			return customerLedgerRepository.findByCustomerIdOrderByEntryDateAscIdAsc(customerId);
		}
		return customerLedgerRepository.findAllByOrderByEntryDateAscIdAsc();
	}

	@GetMapping("/{id}")
	public CustomerLedger get(@PathVariable Long id) {
		return customerLedgerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer ledger entry not found"));
	}

	@PostMapping
	public ResponseEntity<CustomerLedger> recordReceipt(@Valid @RequestBody CustomerReceiptRequest request) {
		CustomerLedger saved = salePostingService.recordReceipt(request.getDate(), request.getCustomerId(),
				request.getCustomerName(), request.getCredit());
		return ResponseEntity.status(201).body(saved);
	}

}
