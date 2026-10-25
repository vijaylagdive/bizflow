package com.bizflow.salesorder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bizflow.customer.Customer;
import com.bizflow.customer.CustomerRepository;
import com.bizflow.customerledger.CustomerLedger;
import com.bizflow.customerledger.CustomerLedgerRepository;
import com.bizflow.inventory.InventoryStockService;
import com.bizflow.purchaseorder.PurchasePostingService.ItemSnapshot;

@Service
@Transactional
public class SalePostingService {

	private static final String TYPE_SALE = "Sale";
	private static final String TYPE_RECEIPT = "receipt";

	private final CustomerLedgerRepository customerLedgerRepository;
	private final InventoryStockService inventoryStockService;
	private final CustomerRepository customerRepository;

	public SalePostingService(CustomerLedgerRepository customerLedgerRepository,
			InventoryStockService inventoryStockService, CustomerRepository customerRepository) {
		this.customerLedgerRepository = customerLedgerRepository;
		this.inventoryStockService = inventoryStockService;
		this.customerRepository = customerRepository;
	}

	public void applySale(SalesOrder order) {
		upsertSaleLedger(order);
		for (SalesOrderItem item : order.getItems()) {
			inventoryStockService.removeStock(item.getProductId(), item.getQty(), item.getUnit(), item.getTotalAmount());
		}
	}

	public void syncSaleLedger(SalesOrder order) {
		upsertSaleLedger(order);
	}

	public void removeStock(SalesOrderItem item) {
		inventoryStockService.removeStock(item.getProductId(), item.getQty(), item.getUnit(), item.getTotalAmount());
	}

	public void reverseSale(Long salesOrderId, Long customerId, List<ItemSnapshot> items) {
		customerLedgerRepository.findBySalesOrderId(salesOrderId).ifPresent(customerLedgerRepository::delete);
		recalculateCustomerBalance(customerId);
		for (ItemSnapshot item : items) {
			inventoryStockService.addStock(item.productId(), item.qty(), item.unit(), item.totalAmount());
		}
	}

	public CustomerLedger recordReceipt(java.time.LocalDate date, Long customerId, String customerName,
			BigDecimal credit) {
		return recordReceipt(date, customerId, customerName, credit, null);
	}

	public CustomerLedger recordReceipt(java.time.LocalDate date, Long customerId, String customerName,
			BigDecimal credit, Long cashbookId) {
		CustomerLedger entry = cashbookId == null ? new CustomerLedger()
				: customerLedgerRepository.findByCashbookId(cashbookId).orElseGet(CustomerLedger::new);
		entry.setEntryDate(date);
		entry.setEntryType(TYPE_RECEIPT);
		entry.setCustomerId(customerId);
		entry.setCustomerName(resolveCustomerName(customerId, customerName));
		entry.setDebit(BigDecimal.ZERO);
		entry.setCredit(credit);
		entry.setCashbookId(cashbookId);
		CustomerLedger saved = customerLedgerRepository.save(entry);
		recalculateCustomerBalance(customerId);
		return customerLedgerRepository.findById(saved.getId()).orElse(saved);
	}

	public void reverseCashbookReceipt(Long cashbookId, Long customerId) {
		customerLedgerRepository.findByCashbookId(cashbookId).ifPresent(customerLedgerRepository::delete);
		recalculateCustomerBalance(customerId);
	}

	public static List<ItemSnapshot> snapshot(List<SalesOrderItem> items) {
		List<ItemSnapshot> snapshots = new ArrayList<>();
		if (items == null) {
			return snapshots;
		}
		for (SalesOrderItem item : items) {
			snapshots.add(new ItemSnapshot(item.getProductId(), item.getQty(), item.getUnit(), item.getTotalAmount()));
		}
		return snapshots;
	}

	private void upsertSaleLedger(SalesOrder order) {
		CustomerLedger entry = customerLedgerRepository.findBySalesOrderId(order.getId()).orElseGet(CustomerLedger::new);
		entry.setEntryDate(order.getSalesDate());
		entry.setEntryType(TYPE_SALE);
		entry.setCustomerId(order.getCustomerId());
		entry.setCustomerName(order.getCustomerName());
		entry.setSaleOrderNo(saleOrderNo(order));
		entry.setDebit(nullSafe(order.getSalesTotalAmount()));
		entry.setCredit(BigDecimal.ZERO);
		entry.setSalesOrderId(order.getId());
		customerLedgerRepository.save(entry);
		recalculateCustomerBalance(order.getCustomerId());
	}

	private void recalculateCustomerBalance(Long customerId) {
		if (customerId == null) {
			return;
		}
		List<CustomerLedger> entries = customerLedgerRepository.findByCustomerIdOrderByEntryDateAscIdAsc(customerId);
		BigDecimal running = BigDecimal.ZERO;
		for (CustomerLedger entry : entries) {
			running = running.add(nullSafe(entry.getDebit())).subtract(nullSafe(entry.getCredit()));
			entry.setBalance(running);
		}
		customerLedgerRepository.saveAll(entries);
		final BigDecimal customerBalance = running;
		customerRepository.findById(customerId).ifPresent(customer -> {
			customer.setCurrentBalance(customerBalance);
			customerRepository.save(customer);
		});
	}

	private String resolveCustomerName(Long customerId, String customerName) {
		if (customerName != null && !customerName.isBlank()) {
			return customerName;
		}
		return customerRepository.findById(customerId).map(Customer::getName).orElse("Customer " + customerId);
	}

	private static String saleOrderNo(SalesOrder order) {
		if (order.getInvoiceNumber() != null && !order.getInvoiceNumber().isBlank()) {
			return order.getInvoiceNumber();
		}
		return order.getId() == null ? null : String.valueOf(order.getId());
	}

	private static BigDecimal nullSafe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}

}
