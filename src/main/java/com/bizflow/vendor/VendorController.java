package com.bizflow.vendor;

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
@RequestMapping("/vendors")
public class VendorController {

	private final VendorRepository vendorRepository;

	public VendorController(VendorRepository vendorRepository) {
		this.vendorRepository = vendorRepository;
	}

	@GetMapping
	public List<Vendor> list() {
		return vendorRepository.findAll();
	}

	@GetMapping("/{vendorId}")
	public Vendor get(@PathVariable Long vendorId) {
		return vendorRepository.findById(vendorId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor not found"));
	}

	@PostMapping
	public ResponseEntity<Vendor> create(@Valid @RequestBody VendorRequest request) {
		Vendor vendor = new Vendor();
		vendor.setName(request.getName());
		vendor.setAddress(request.getAddress());
		vendor.setContactNumber(request.getContactNumber());
		Vendor saved = vendorRepository.save(vendor);
		return ResponseEntity.created(URI.create("/vendors/" + saved.getVendorId())).body(saved);
	}

	@PutMapping("/{vendorId}")
	public Vendor update(@PathVariable Long vendorId, @Valid @RequestBody VendorRequest request) {
		Vendor vendor = vendorRepository.findById(vendorId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor not found"));
		vendor.setName(request.getName());
		vendor.setAddress(request.getAddress());
		vendor.setContactNumber(request.getContactNumber());
		return vendorRepository.save(vendor);
	}

	@DeleteMapping("/{vendorId}")
	public ResponseEntity<Void> delete(@PathVariable Long vendorId) {
		if (!vendorRepository.existsById(vendorId)) {
			throw new ResponseStatusException(NOT_FOUND, "Vendor not found");
		}
		vendorRepository.deleteById(vendorId);
		return ResponseEntity.noContent().build();
	}

}
