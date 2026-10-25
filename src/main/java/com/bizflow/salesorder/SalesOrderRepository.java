package com.bizflow.salesorder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

	@EntityGraph(attributePaths = "items")
	@Override
	List<SalesOrder> findAll();

	@EntityGraph(attributePaths = "items")
	@Override
	Optional<SalesOrder> findById(Long id);
}
