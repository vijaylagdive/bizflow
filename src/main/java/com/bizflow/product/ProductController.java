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
@RequestMapping("/products")
public class ProductController {

	private final ProductRepository productRepository;
	private final ProductCategoryRepository productCategoryRepository;

	public ProductController(ProductRepository productRepository,
			ProductCategoryRepository productCategoryRepository) {
		this.productRepository = productRepository;
		this.productCategoryRepository = productCategoryRepository;
	}

	@GetMapping
	public List<Product> list() {
		return productRepository.findAll();
	}

	@GetMapping("/{productId}")
	public Product get(@PathVariable Long productId) {
		return productRepository.findById(productId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
	}

	@PostMapping
	public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
		ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product category not found"));
		Product product = new Product();
		product.setProductName(request.getProductName());
		product.setCategory(category);
		Product saved = productRepository.save(product);
		Product loaded = productRepository.findById(saved.getProductId()).orElse(saved);
		return ResponseEntity.created(URI.create("/products/" + loaded.getProductId())).body(loaded);
	}

	@PutMapping("/{productId}")
	public Product update(@PathVariable Long productId, @Valid @RequestBody ProductRequest request) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
		ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product category not found"));
		product.setProductName(request.getProductName());
		product.setCategory(category);
		productRepository.save(product);
		return productRepository.findById(productId).orElse(product);
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<Void> delete(@PathVariable Long productId) {
		if (!productRepository.existsById(productId)) {
			throw new ResponseStatusException(NOT_FOUND, "Product not found");
		}
		productRepository.deleteById(productId);
		return ResponseEntity.noContent().build();
	}

}
