package com.bizflow.salesorder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SalesOrderRequest {

	@NotNull
	private LocalDate salesDate;

	@Size(max = 100)
	private String invoiceNumber;

	@NotNull
	private Long customerId;

	@Size(max = 200)
	private String customerName;

	private BigDecimal salesTotalAmount;

	private BigDecimal cashDiscount;

	private List<@Valid SalesOrderItemRequest> items = new ArrayList<>();

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

	public BigDecimal getSalesTotalAmount() {
		return salesTotalAmount;
	}

	public void setSalesTotalAmount(BigDecimal salesTotalAmount) {
		this.salesTotalAmount = salesTotalAmount;
	}

	public BigDecimal getCashDiscount() {
		return cashDiscount;
	}

	public void setCashDiscount(BigDecimal cashDiscount) {
		this.cashDiscount = cashDiscount;
	}

	public List<SalesOrderItemRequest> getItems() {
		return items;
	}

	public void setItems(List<SalesOrderItemRequest> items) {
		this.items = items;
	}

}
