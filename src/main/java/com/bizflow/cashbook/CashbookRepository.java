package com.bizflow.cashbook;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CashbookRepository extends JpaRepository<CashbookEntry, Long>, JpaSpecificationExecutor<CashbookEntry> {

	List<CashbookEntry> findAllByOrderByEntryDateAscIdAsc();

}
