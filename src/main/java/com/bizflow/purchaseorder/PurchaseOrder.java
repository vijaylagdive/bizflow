package com.bizflow.purchaseorder;

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
@Table(name = "purchase_orders")
public class PurchaseOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "purchase_date", nullable = false)
	private LocalDate purchaseDate;

	@Column(name = "invoice_number", length = 100)
	private String invoiceNumber;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;

	@Column(name = "vendor_name", length = 200)
	private String vendorName;

	@Column(name = "purchase_total_amount", nullable = false, precision = 14, scale = 2)
	private BigDecimal purchaseTotalAmount = BigDecimal.ZERO;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "cash_discount", nullable = false, precision = 14, scale = 2)
	private BigDecimal cashDiscount = BigDecimal.ZERO;
	
	@OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PurchaseOrderItem> items = new ArrayList<>();

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
		if (purchaseTotalAmount == null) {
			purchaseTotalAmount = BigDecimal.ZERO;
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

	public LocalDate getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(LocalDate purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public Long getVendorId() {
		return vendorId;
	}

	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public BigDecimal getPurchaseTotalAmount() {
		return purchaseTotalAmount;
	}

	public void setPurchaseTotalAmount(BigDecimal purchaseTotalAmount) {
		this.purchaseTotalAmount = purchaseTotalAmount;
	}

	public BigDecimal getCashDiscount() {
		return cashDiscount;
	}

	public void setCashDiscount(BigDecimal cashDiscount) {
		this.cashDiscount = cashDiscount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public List<PurchaseOrderItem> getItems() {
		return items;
	}

	public void addItem(PurchaseOrderItem item) {
		items.add(item);
		item.setPurchaseOrder(this);
	}

	public void clearItems() {
		for (PurchaseOrderItem item : items) {
			item.setPurchaseOrder(null);
		}
		items.clear();
	}

}
