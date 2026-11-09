package com.bizflow.purchaseorder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>,
		JpaSpecificationExecutor<PurchaseOrder> {

	@EntityGraph(attributePaths = "items")
	List<PurchaseOrder> findByIdIn(Collection<Long> ids);

	@EntityGraph(attributePaths = "items")
	@Override
	Optional<PurchaseOrder> findById(Long id);
}
