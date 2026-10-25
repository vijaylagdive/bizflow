package com.bizflow.customer;

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
@RequestMapping("/customers")
public class CustomerController {

	private final CustomerRepository customerRepository;

	public CustomerController(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@GetMapping
	public List<Customer> list() {
		return customerRepository.findAll();
	}

	@GetMapping("/{customerId}")
	public Customer get(@PathVariable Long customerId) {
		return customerRepository.findById(customerId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
	}

	@PostMapping
	public ResponseEntity<Customer> create(@Valid @RequestBody CustomerRequest request) {
		Customer customer = new Customer();
		customer.setName(request.getName());
		customer.setAddress(request.getAddress());
		customer.setContactNumber(request.getContactNumber());
		Customer saved = customerRepository.save(customer);
		return ResponseEntity.created(URI.create("/customers/" + saved.getCustomerId())).body(saved);
	}

	@PutMapping("/{customerId}")
	public Customer update(@PathVariable Long customerId, @Valid @RequestBody CustomerRequest request) {
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
		customer.setName(request.getName());
		customer.setAddress(request.getAddress());
		customer.setContactNumber(request.getContactNumber());
		return customerRepository.save(customer);
	}

	@DeleteMapping("/{customerId}")
	public ResponseEntity<Void> delete(@PathVariable Long customerId) {
		if (!customerRepository.existsById(customerId)) {
			throw new ResponseStatusException(NOT_FOUND, "Customer not found");
		}
		customerRepository.deleteById(customerId);
		return ResponseEntity.noContent().build();
	}

}
