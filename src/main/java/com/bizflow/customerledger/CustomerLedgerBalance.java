package com.bizflow.customerledger;

import java.math.BigDecimal;

public record CustomerLedgerBalance(Long customerId, String customerName, BigDecimal currentBalance) {

	public static CustomerLedgerBalance from(CustomerLedger entry) {
		return new CustomerLedgerBalance(entry.getCustomerId(), entry.getCustomerName(), entry.getBalance());
	}

}
