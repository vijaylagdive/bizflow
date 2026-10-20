package com.bizflow.inventory;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

	private final InventoryRepository inventoryRepository;

	public InventoryController(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}

	@GetMapping
	public List<InventoryItem> list() {
		return inventoryRepository.findAll();
	}

	@GetMapping("/{id}")
	public InventoryItem get(@PathVariable Long id) {
		return inventoryRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Inventory item not found"));
	}

	@GetMapping("/product/{productId}")
	public List<InventoryItem> getByProduct(@PathVariable Long productId) {
		List<InventoryItem> items = inventoryRepository.findByProductId(productId);
		if (items.isEmpty()) {
			throw new ResponseStatusException(NOT_FOUND, "Inventory item not found");
		}
		return items;
	}

}
