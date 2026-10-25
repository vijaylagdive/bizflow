package com.bizflow.purchaseorder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bizflow.inventory.InventoryStockService;
import com.bizflow.ledger.VendorLedger;
import com.bizflow.ledger.VendorLedgerRepository;
import com.bizflow.vendor.Vendor;
import com.bizflow.vendor.VendorRepository;

@Service
@Transactional
public class PurchasePostingService {

	private static final String TYPE_PURCHASE = "purchase";
	private static final String TYPE_PAYMENT = "payment";

	private final VendorLedgerRepository vendorLedgerRepository;
	private final InventoryStockService inventoryStockService;
	private final VendorRepository vendorRepository;

	public PurchasePostingService(VendorLedgerRepository vendorLedgerRepository,
			InventoryStockService inventoryStockService, VendorRepository vendorRepository) {
		this.vendorLedgerRepository = vendorLedgerRepository;
		this.inventoryStockService = inventoryStockService;
		this.vendorRepository = vendorRepository;
	}

	public void applyPurchase(PurchaseOrder order) {
		upsertPurchaseLedger(order);
		for (PurchaseOrderItem item : order.getItems()) {
			adjustStock(item, true);
		}
	}

	public void syncPurchaseLedger(PurchaseOrder order) {
		upsertPurchaseLedger(order);
	}

	public void addStock(PurchaseOrderItem item) {
		adjustStock(item, true);
	}

	public void reversePurchase(Long purchaseOrderId, Long vendorId, List<ItemSnapshot> items) {
		vendorLedgerRepository.findByPurchaseOrderId(purchaseOrderId).ifPresent(vendorLedgerRepository::delete);
		recalculateVendorBalance(vendorId);
		for (ItemSnapshot item : items) {
			adjustStock(item, false);
		}
	}

	public VendorLedger recordPayment(java.time.LocalDate date, Long vendorId, String vendorName, BigDecimal credit) {
		return recordPayment(date, vendorId, vendorName, credit, null);
	}

	public VendorLedger recordPayment(java.time.LocalDate date, Long vendorId, String vendorName, BigDecimal credit,
			Long cashbookId) {
		VendorLedger entry = cashbookId == null ? new VendorLedger()
				: vendorLedgerRepository.findByCashbookId(cashbookId).orElseGet(VendorLedger::new);
		entry.setEntryDate(date);
		entry.setEntryType(TYPE_PAYMENT);
		entry.setVendorId(vendorId);
		entry.setVendorName(resolveVendorName(vendorId, vendorName));
		entry.setDebit(BigDecimal.ZERO);
		entry.setCredit(credit);
		entry.setCashbookId(cashbookId);
		VendorLedger saved = vendorLedgerRepository.save(entry);
		recalculateVendorBalance(vendorId);
		return vendorLedgerRepository.findById(saved.getId()).orElse(saved);
	}

	public void reverseCashbookPayment(Long cashbookId, Long vendorId) {
		vendorLedgerRepository.findByCashbookId(cashbookId).ifPresent(vendorLedgerRepository::delete);
		recalculateVendorBalance(vendorId);
	}

	public static List<ItemSnapshot> snapshot(List<PurchaseOrderItem> items) {
		List<ItemSnapshot> snapshots = new ArrayList<>();
		if (items == null) {
			return snapshots;
		}
		for (PurchaseOrderItem item : items) {
			snapshots.add(new ItemSnapshot(item.getProductId(), item.getQty(), item.getUnit(), item.getTotalAmount()));
		}
		return snapshots;
	}

	private void upsertPurchaseLedger(PurchaseOrder order) {
		VendorLedger entry = vendorLedgerRepository.findByPurchaseOrderId(order.getId()).orElseGet(VendorLedger::new);
		entry.setEntryDate(order.getPurchaseDate());
		entry.setEntryType(TYPE_PURCHASE);
		entry.setVendorId(order.getVendorId());
		entry.setVendorName(order.getVendorName());
		entry.setDebit(nullSafe(order.getPurchaseTotalAmount()));
		entry.setCredit(BigDecimal.ZERO);
		entry.setPurchaseOrderId(order.getId());
		vendorLedgerRepository.save(entry);
		recalculateVendorBalance(order.getVendorId());
	}

	private void adjustStock(PurchaseOrderItem item, boolean add) {
		adjustStock(new ItemSnapshot(item.getProductId(), item.getQty(), item.getUnit(), item.getTotalAmount()), add);
	}

	private void adjustStock(ItemSnapshot item, boolean add) {
		if (add) {
			inventoryStockService.addStock(item.productId(), item.qty(), item.unit(), item.totalAmount());
		}
		else {
			inventoryStockService.removeStock(item.productId(), item.qty(), item.unit(), item.totalAmount());
		}
	}

	private void recalculateVendorBalance(Long vendorId) {
		if (vendorId == null) {
			return;
		}
		List<VendorLedger> entries = vendorLedgerRepository.findByVendorIdOrderByEntryDateAscIdAsc(vendorId);
		BigDecimal running = BigDecimal.ZERO;
		for (VendorLedger entry : entries) {
			running = running.add(nullSafe(entry.getDebit())).subtract(nullSafe(entry.getCredit()));
			entry.setBalance(running);
		}
		vendorLedgerRepository.saveAll(entries);
		final BigDecimal vendorBalance = running;
		vendorRepository.findById(vendorId).ifPresent(vendor -> {
			vendor.setCurrentBalance(vendorBalance);
			vendorRepository.save(vendor);
		});
	}

	private String resolveVendorName(Long vendorId, String vendorName) {
		if (vendorName != null && !vendorName.isBlank()) {
			return vendorName;
		}
		return vendorRepository.findById(vendorId).map(Vendor::getName).orElse("Vendor " + vendorId);
	}

	private static BigDecimal nullSafe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}

	public record ItemSnapshot(Long productId, BigDecimal qty, String unit, BigDecimal totalAmount) {
	}

}
