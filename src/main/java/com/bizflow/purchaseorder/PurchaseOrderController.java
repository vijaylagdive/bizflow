package com.bizflow.purchaseorder;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/purchase-orders")
@Transactional
public class PurchaseOrderController {

	private final PurchaseOrderRepository purchaseOrderRepository;
	private final PurchasePostingService purchasePostingService;

	public PurchaseOrderController(PurchaseOrderRepository purchaseOrderRepository,
			PurchasePostingService purchasePostingService) {
		this.purchaseOrderRepository = purchaseOrderRepository;
		this.purchasePostingService = purchasePostingService;
	}

	@GetMapping
	public List<PurchaseOrder> list() {
		return purchaseOrderRepository.findAll();
	}

	@GetMapping("/{id}")
	public PurchaseOrder get(@PathVariable Long id) {
		return purchaseOrderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
	}

	@PostMapping
	public ResponseEntity<PurchaseOrder> create(@Valid @RequestBody PurchaseOrderRequest request) {
		PurchaseOrder order = new PurchaseOrder();
		applyRequest(order, request);
		PurchaseOrder saved = purchaseOrderRepository.save(order);
		PurchaseOrder loaded = purchaseOrderRepository.findById(saved.getId()).orElse(saved);
		purchasePostingService.applyPurchase(loaded);
		return ResponseEntity.created(URI.create("/purchase-orders/" + loaded.getId())).body(loaded);
	}

	@PutMapping("/{id}")
	public PurchaseOrder update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderRequest request) {
		PurchaseOrder order = purchaseOrderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
		Long previousVendorId = order.getVendorId();
		var previousItems = PurchasePostingService.snapshot(order.getItems());
		applyRequest(order, request);
		purchaseOrderRepository.save(order);
		purchasePostingService.reversePurchase(id, previousVendorId, previousItems);
		PurchaseOrder loaded = purchaseOrderRepository.findById(id).orElse(order);
		purchasePostingService.applyPurchase(loaded);
		return loaded;
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		PurchaseOrder order = purchaseOrderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
		var previousItems = PurchasePostingService.snapshot(order.getItems());
		Long vendorId = order.getVendorId();
		purchaseOrderRepository.delete(order);
		purchasePostingService.reversePurchase(id, vendorId, previousItems);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/items")
	public ResponseEntity<PurchaseOrderItem> addItem(@PathVariable Long id,
			@Valid @RequestBody PurchaseOrderItemRequest request) {
		PurchaseOrder order = purchaseOrderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Purchase order not found"));
		PurchaseOrderItem item = toItem(request);
		order.addItem(item);
		order.setPurchaseTotalAmount(order.getPurchaseTotalAmount().add(item.getTotalAmount()));
		purchaseOrderRepository.save(order);
		PurchaseOrder loaded = purchaseOrderRepository.findById(id).orElse(order);
		purchasePostingService.syncPurchaseLedger(loaded);
		purchasePostingService.addStock(item);
		return ResponseEntity.status(201).body(item);
	}

	private static void applyRequest(PurchaseOrder order, PurchaseOrderRequest request) {
		order.setPurchaseDate(request.getPurchaseDate());
		order.setInvoiceNumber(request.getInvoiceNumber());
		order.setVendorId(request.getVendorId());
		order.setVendorName(request.getVendorName());
		order.setCashDiscount(request.getCashDiscount() != null ? request.getCashDiscount() : BigDecimal.ZERO);
		order.clearItems();

		BigDecimal itemsTotal = BigDecimal.ZERO;
		if (request.getItems() != null) {
			for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
				PurchaseOrderItem item = toItem(itemRequest);
				order.addItem(item);
				itemsTotal = itemsTotal.add(item.getTotalAmount());
			}
		}

		if (request.getPurchaseTotalAmount() != null) {
			order.setPurchaseTotalAmount(request.getPurchaseTotalAmount());
		}
		else {
			order.setPurchaseTotalAmount(itemsTotal);
		}
	}

	private static PurchaseOrderItem toItem(PurchaseOrderItemRequest request) {
		PurchaseOrderItem item = new PurchaseOrderItem();
		item.setProductId(request.getProductId());
		item.setQty(request.getQty());
		item.setUnit(request.getUnit());
		item.setRate(request.getRate());
		item.setGstPercent(request.getGstPercent() != null ? request.getGstPercent() : BigDecimal.ZERO);
		if (request.getTotalAmount() != null) {
			item.setTotalAmount(request.getTotalAmount());
		}
		else {
			BigDecimal qty = request.getQty() != null ? request.getQty() : BigDecimal.ZERO;
			BigDecimal rate = request.getRate() != null ? request.getRate() : BigDecimal.ZERO;
			item.setTotalAmount(qty.multiply(rate).setScale(2, RoundingMode.HALF_UP));
		}
		return item;
	}

}
