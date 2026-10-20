package com.bizflow.inventory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

	List<InventoryItem> findByProductId(Long productId);

	Optional<InventoryItem> findByProductIdAndUnit(Long productId, String unit);
}
