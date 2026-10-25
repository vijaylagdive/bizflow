package com.bizflow.cashbook;

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
@Table(name = "cashbook")
public class CashbookEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "entry_date", nullable = false)
	@JsonProperty("date")
	private LocalDate entryDate;

	@Column(name = "transaction_type", nullable = false, length = 20)
	private String transactionType;

	@Column(name = "account_id", nullable = false)
	private Long accountId;

	@Column(nullable = false, length = 200)
	private String account;

	@Column(name = "from_to", nullable = false, length = 200)
	private String fromTo;

	@Column(name = "party_id")
	private Long partyId;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal balance = BigDecimal.ZERO;

	@Column(length = 500)
	private String remark;

	public Long getId() {
		return id;
	}

	public LocalDate getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(LocalDate entryDate) {
		this.entryDate = entryDate;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getFromTo() {
		return fromTo;
	}

	public void setFromTo(String fromTo) {
		this.fromTo = fromTo;
	}

	public Long getPartyId() {
		return partyId;
	}

	public void setPartyId(Long partyId) {
		this.partyId = partyId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
