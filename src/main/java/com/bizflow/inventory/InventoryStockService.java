package com.bizflow.inventory;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bizflow.product.Product;
import com.bizflow.product.ProductRepository;

@Service
@Transactional
public class InventoryStockService {

	private final InventoryRepository inventoryRepository;
	private final ProductRepository productRepository;

	public InventoryStockService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
		this.inventoryRepository = inventoryRepository;
		this.productRepository = productRepository;
	}

	public void addStock(Long productId, BigDecimal qty, String unit, BigDecimal value) {
		changeStock(productId, qty, unit, value, true);
	}

	public void removeStock(Long productId, BigDecimal qty, String unit, BigDecimal value) {
		changeStock(productId, qty, unit, value, false);
	}

	private void changeStock(Long productId, BigDecimal qty, String unit, BigDecimal value, boolean add) {
		if (productId == null) {
			return;
		}
		String normalizedUnit = unit == null ? "" : unit;
		InventoryItem stock = inventoryRepository.findByProductIdAndUnit(productId, normalizedUnit).orElseGet(() -> {
			InventoryItem created = new InventoryItem();
			created.setProductId(productId);
			created.setProductName(resolveProductName(productId));
			created.setUnit(normalizedUnit);
			created.setCurrentStock(BigDecimal.ZERO);
			created.setStockValue(BigDecimal.ZERO);
			return created;
		});
		BigDecimal movementQty = qty != null ? qty : BigDecimal.ZERO;
		BigDecimal movementValue = value != null ? value : BigDecimal.ZERO;
		if (add) {
			stock.setCurrentStock(nullSafe(stock.getCurrentStock()).add(movementQty));
			stock.setStockValue(nullSafe(stock.getStockValue()).add(movementValue));
		}
		else {
			stock.setCurrentStock(nullSafe(stock.getCurrentStock()).subtract(movementQty));
			stock.setStockValue(nullSafe(stock.getStockValue()).subtract(movementValue));
		}
		stock.setProductName(resolveProductName(productId));
		inventoryRepository.save(stock);
	}

	private String resolveProductName(Long productId) {
		return productRepository.findById(productId)
				.map(Product::getProductName)
				.filter(name -> name != null && !name.isBlank())
				.orElse("Product " + productId);
	}

	private static BigDecimal nullSafe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}

}
