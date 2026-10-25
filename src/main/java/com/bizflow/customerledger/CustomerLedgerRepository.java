package com.bizflow.customerledger;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerLedgerRepository extends JpaRepository<CustomerLedger, Long> {

	List<CustomerLedger> findByCustomerIdOrderByEntryDateAscIdAsc(Long customerId);

	List<CustomerLedger> findAllByOrderByEntryDateAscIdAsc();

	Optional<CustomerLedger> findBySalesOrderId(Long salesOrderId);

	Optional<CustomerLedger> findByCashbookId(Long cashbookId);
}
