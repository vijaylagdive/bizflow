package com.bizflow.ledger;

import java.math.BigDecimal;

public record VendorLedgerBalance(Long vendorId, String vendorName, BigDecimal currentBalance) {

	public static VendorLedgerBalance from(VendorLedger entry) {
		return new VendorLedgerBalance(entry.getVendorId(), entry.getVendorName(), entry.getBalance());
	}

}
