package com.bizflow.customerledger;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerLedgerRepository extends JpaRepository<CustomerLedger, Long> {

	List<CustomerLedger> findByCustomerIdOrderByEntryDateAscIdAsc(Long customerId);

	@Query("""
			select c from CustomerLedger c
			where not exists (
				select 1 from CustomerLedger later
				where later.customerId = c.customerId
					and (later.entryDate > c.entryDate
						or (later.entryDate = c.entryDate and later.id > c.id))
			)
			order by c.customerName asc, c.customerId asc
			""")
	List<CustomerLedger> findLatestPerCustomer();

	@Query("""
			select c from CustomerLedger c
			where c.customerId = :customerId
				and not exists (
					select 1 from CustomerLedger later
					where later.customerId = c.customerId
						and (later.entryDate > c.entryDate
							or (later.entryDate = c.entryDate and later.id > c.id))
				)
			""")
	Optional<CustomerLedger> findLatestByCustomerId(@Param("customerId") Long customerId);

	List<CustomerLedger> findAllByOrderByEntryDateAscIdAsc();

	Optional<CustomerLedger> findBySalesOrderId(Long salesOrderId);

	Optional<CustomerLedger> findByCashbookId(Long cashbookId);
}
