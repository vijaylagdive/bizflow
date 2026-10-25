package com.bizflow.salesorder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sales_date", nullable = false)
	private LocalDate salesDate;

	@Column(name = "invoice_number", length = 100)
	private String invoiceNumber;

	@Column(name = "customer_id", nullable = false)
	private Long customerId;

	@Column(name = "customer_name", length = 200)
	private String customerName;

	@Column(name = "cash_discount", nullable = false, precision = 14, scale = 2)
	private BigDecimal cashDiscount = BigDecimal.ZERO;

	@Column(name = "sales_total_amount", nullable = false, precision = 14, scale = 2)
	private BigDecimal salesTotalAmount = BigDecimal.ZERO;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SalesOrderItem> items = new ArrayList<>();

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
		if (salesTotalAmount == null) {
			salesTotalAmount = BigDecimal.ZERO;
		}
		if (cashDiscount == null) {
			cashDiscount = BigDecimal.ZERO;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public LocalDate getSalesDate() {
		return salesDate;
	}

	public void setSalesDate(LocalDate salesDate) {
		this.salesDate = salesDate;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public BigDecimal getCashDiscount() {
		return cashDiscount;
	}

	public void setCashDiscount(BigDecimal cashDiscount) {
		this.cashDiscount = cashDiscount;
	}

	public BigDecimal getSalesTotalAmount() {
		return salesTotalAmount;
	}

	public void setSalesTotalAmount(BigDecimal salesTotalAmount) {
		this.salesTotalAmount = salesTotalAmount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public List<SalesOrderItem> getItems() {
		return items;
	}

	public void addItem(SalesOrderItem item) {
		items.add(item);
		item.setSalesOrder(this);
	}

	public void clearItems() {
		for (SalesOrderItem item : items) {
			item.setSalesOrder(null);
		}
		items.clear();
	}

}
