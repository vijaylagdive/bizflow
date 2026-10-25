package com.bizflow.cashbook;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CashbookRepository extends JpaRepository<CashbookEntry, Long> {

	List<CashbookEntry> findAllByOrderByEntryDateAscIdAsc();

	List<CashbookEntry> findByAccountIdOrderByEntryDateAscIdAsc(Long accountId);
}
