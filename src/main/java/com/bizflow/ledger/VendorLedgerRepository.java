package com.bizflow.ledger;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorLedgerRepository extends JpaRepository<VendorLedger, Long> {

	List<VendorLedger> findByVendorIdOrderByEntryDateAscIdAsc(Long vendorId);

	List<VendorLedger> findAllByOrderByEntryDateAscIdAsc();

	Optional<VendorLedger> findByPurchaseOrderId(Long purchaseOrderId);

	Optional<VendorLedger> findByCashbookId(Long cashbookId);
}
