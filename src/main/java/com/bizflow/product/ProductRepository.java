package com.bizflow.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@EntityGraph(attributePaths = "category")
	@Override
	List<Product> findAll();

	@EntityGraph(attributePaths = "category")
	@Override
	Optional<Product> findById(Long id);
}
