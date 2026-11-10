package com.bizflow.ledger;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendorLedgerRepository extends JpaRepository<VendorLedger, Long> {

	List<VendorLedger> findByVendorIdOrderByEntryDateAscIdAsc(Long vendorId);

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

	@Query("""
			select v from VendorLedger v
			where v.vendorId = :vendorId
				and not exists (
					select 1 from VendorLedger later
					where later.vendorId = v.vendorId
						and (later.entryDate > v.entryDate
							or (later.entryDate = v.entryDate and later.id > v.id))
				)
			""")
	Optional<VendorLedger> findLatestByVendorId(@Param("vendorId") Long vendorId);

	List<VendorLedger> findAllByOrderByEntryDateAscIdAsc();

	Optional<VendorLedger> findByPurchaseOrderId(Long purchaseOrderId);

	Optional<VendorLedger> findByCashbookId(Long cashbookId);
}
