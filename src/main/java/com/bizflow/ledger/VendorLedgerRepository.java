package com.bizflow.ledger;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VendorLedgerRepository extends JpaRepository<VendorLedger, Long> {

	List<VendorLedger> findByVendorIdOrderByEntryDateAscIdAsc(Long vendorId);

	List<VendorLedger> findAllByOrderByEntryDateAscIdAsc();

	@Query("""
			select v from VendorLedger v
			where not exists (
				select 1 from VendorLedger later
				where later.vendorId = v.vendorId
					and (later.entryDate > v.entryDate
						or (later.entryDate = v.entryDate and later.id > v.id))
			)
			order by v.vendorName asc, v.vendorId asc
			""")
	List<VendorLedger> findLatestPerVendor();

	Optional<VendorLedger> findByPurchaseOrderId(Long purchaseOrderId);

	Optional<VendorLedger> findByCashbookId(Long cashbookId);
}
