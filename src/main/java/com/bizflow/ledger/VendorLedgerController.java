package com.bizflow.ledger;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bizflow.purchaseorder.PurchasePostingService;

import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/vendor-ledgers")
public class VendorLedgerController {

	private final VendorLedgerRepository vendorLedgerRepository;
	private final PurchasePostingService purchasePostingService;

	public VendorLedgerController(VendorLedgerRepository vendorLedgerRepository,
			PurchasePostingService purchasePostingService) {
		this.vendorLedgerRepository = vendorLedgerRepository;
		this.purchasePostingService = purchasePostingService;
	}

	@GetMapping
	public List<VendorLedger> list(@RequestParam(required = false) Long vendorId) {
		if (vendorId != null) {
			return vendorLedgerRepository.findByVendorIdOrderByEntryDateAscIdAsc(vendorId);
		}
		return vendorLedgerRepository.findAllByOrderByEntryDateAscIdAsc();
	}

	@GetMapping("/{id}")
	public VendorLedger get(@PathVariable Long id) {
		return vendorLedgerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vendor ledger entry not found"));
	}

	@PostMapping
	public ResponseEntity<VendorLedger> recordPayment(@Valid @RequestBody VendorPaymentRequest request) {
		VendorLedger saved = purchasePostingService.recordPayment(request.getDate(), request.getVendorId(),
				request.getVendorName(), request.getCredit());
		return ResponseEntity.status(201).body(saved);
	}

}
