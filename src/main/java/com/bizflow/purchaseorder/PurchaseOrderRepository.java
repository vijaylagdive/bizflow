package com.bizflow.purchaseorder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

	@EntityGraph(attributePaths = "items")
	@Override
	List<PurchaseOrder> findAll();

	@EntityGraph(attributePaths = "items")
	@Override
	Optional<PurchaseOrder> findById(Long id);
}
