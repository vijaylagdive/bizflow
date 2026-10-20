package com.bizflow.product;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/product-categories")
public class ProductCategoryController {

	private final ProductCategoryRepository productCategoryRepository;

	public ProductCategoryController(ProductCategoryRepository productCategoryRepository) {
		this.productCategoryRepository = productCategoryRepository;
	}

	@GetMapping
	public List<ProductCategory> list() {
		return productCategoryRepository.findAll();
	}

	@GetMapping("/{categoryId}")
	public ProductCategory get(@PathVariable Long categoryId) {
		return productCategoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product category not found"));
	}

	@PostMapping
	public ResponseEntity<ProductCategory> create(@Valid @RequestBody ProductCategoryRequest request) {
		ProductCategory category = new ProductCategory();
		category.setCategoryName(request.getCategoryName());
		ProductCategory saved = productCategoryRepository.save(category);
		return ResponseEntity.created(URI.create("/product-categories/" + saved.getCategoryId())).body(saved);
	}

	@PutMapping("/{categoryId}")
	public ProductCategory update(@PathVariable Long categoryId,
			@Valid @RequestBody ProductCategoryRequest request) {
		ProductCategory category = productCategoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product category not found"));
		category.setCategoryName(request.getCategoryName());
		return productCategoryRepository.save(category);
	}

	@DeleteMapping("/{categoryId}")
	public ResponseEntity<Void> delete(@PathVariable Long categoryId) {
		if (!productCategoryRepository.existsById(categoryId)) {
			throw new ResponseStatusException(NOT_FOUND, "Product category not found");
		}
		productCategoryRepository.deleteById(categoryId);
		return ResponseEntity.noContent().build();
	}

}
