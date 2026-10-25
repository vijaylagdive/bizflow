package com.bizflow.customerledger;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_ledger")
public class CustomerLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "entry_date", nullable = false)
	@JsonProperty("date")
	private LocalDate entryDate;

	@Column(name = "entry_type", nullable = false, length = 20)
	@JsonProperty("type")
	private String entryType;

	@Column(name = "customer_id", nullable = false)
	private Long customerId;

	@Column(name = "customer_name", length = 200)
	private String customerName;

	@Column(name = "sale_order_no", length = 100)
	private String saleOrderNo;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal debit = BigDecimal.ZERO;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal credit = BigDecimal.ZERO;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal balance = BigDecimal.ZERO;

	@Column(name = "sales_order_id")
	private Long salesOrderId;

	@Column(name = "cashbook_id")
	private Long cashbookId;

	public Long getId() {
		return id;
	}

	public LocalDate getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(LocalDate entryDate) {
		this.entryDate = entryDate;
	}

	public String getEntryType() {
		return entryType;
	}

	public void setEntryType(String entryType) {
		this.entryType = entryType;
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

	public String getSaleOrderNo() {
		return saleOrderNo;
	}

	public void setSaleOrderNo(String saleOrderNo) {
		this.saleOrderNo = saleOrderNo;
	}

	public BigDecimal getDebit() {
		return debit;
	}

	public void setDebit(BigDecimal debit) {
		this.debit = debit;
	}

	public BigDecimal getCredit() {
		return credit;
	}

	public void setCredit(BigDecimal credit) {
		this.credit = credit;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Long getSalesOrderId() {
		return salesOrderId;
	}

	public void setSalesOrderId(Long salesOrderId) {
		this.salesOrderId = salesOrderId;
	}

	public Long getCashbookId() {
		return cashbookId;
	}

	public void setCashbookId(Long cashbookId) {
		this.cashbookId = cashbookId;
	}

}
