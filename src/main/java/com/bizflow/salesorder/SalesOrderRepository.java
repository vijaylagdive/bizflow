package com.bizflow.salesorder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {

	@EntityGraph(attributePaths = "items")
	List<SalesOrder> findByIdIn(Collection<Long> ids);

	@EntityGraph(attributePaths = "items")
	@Override
	Optional<SalesOrder> findById(Long id);
}
