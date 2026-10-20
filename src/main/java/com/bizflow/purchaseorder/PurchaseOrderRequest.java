package com.bizflow.purchaseorder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PurchaseOrderRequest {

	@NotNull
	private LocalDate purchaseDate;

	@Size(max = 100)
	private String invoiceNumber;

	@NotNull
	private Long vendorId;

	@Size(max = 200)
	private String vendorName;

	private BigDecimal purchaseTotalAmount;

	private BigDecimal cashDiscount;

	private List<@Valid PurchaseOrderItemRequest> items = new ArrayList<>();

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

	public List<PurchaseOrderItemRequest> getItems() {
		return items;
	}

	public void setItems(List<PurchaseOrderItemRequest> items) {
		this.items = items;
	}

}
