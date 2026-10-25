package com.bizflow.salesorder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/sales-orders")
@Transactional
public class SalesOrderController {

	private final SalesOrderRepository salesOrderRepository;
	private final SalePostingService salePostingService;

	public SalesOrderController(SalesOrderRepository salesOrderRepository, SalePostingService salePostingService) {
		this.salesOrderRepository = salesOrderRepository;
		this.salePostingService = salePostingService;
	}

	@GetMapping
	public List<SalesOrder> list() {
		return salesOrderRepository.findAll();
	}

	@GetMapping("/{id}")
	public SalesOrder get(@PathVariable Long id) {
		return salesOrderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sales order not found"));
	}

	@PostMapping
	public ResponseEntity<SalesOrder> create(@Valid @RequestBody SalesOrderRequest request) {
		SalesOrder order = new SalesOrder();
		applyRequest(order, request);
		SalesOrder saved = salesOrderRepository.save(order);
		SalesOrder loaded = salesOrderRepository.findById(saved.getId()).orElse(saved);
		salePostingService.applySale(loaded);
		return ResponseEntity.created(URI.create("/sales-orders/" + loaded.getId())).body(loaded);
	}

	@PutMapping("/{id}")
	public SalesOrder update(@PathVariable Long id, @Valid @RequestBody SalesOrderRequest request) {
		SalesOrder order = salesOrderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sales order not found"));
		Long previousCustomerId = order.getCustomerId();
		var previousItems = SalePostingService.snapshot(order.getItems());
		applyRequest(order, request);
		salesOrderRepository.save(order);
		salePostingService.reverseSale(id, previousCustomerId, previousItems);
		SalesOrder loaded = salesOrderRepository.findById(id).orElse(order);
		salePostingService.applySale(loaded);
		return loaded;
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		SalesOrder order = salesOrderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sales order not found"));
		var previousItems = SalePostingService.snapshot(order.getItems());
		Long customerId = order.getCustomerId();
		salesOrderRepository.delete(order);
		salePostingService.reverseSale(id, customerId, previousItems);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/items")
	public ResponseEntity<SalesOrderItem> addItem(@PathVariable Long id,
			@Valid @RequestBody SalesOrderItemRequest request) {
		SalesOrder order = salesOrderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sales order not found"));
		SalesOrderItem item = toItem(request);
		order.addItem(item);
		order.setSalesTotalAmount(order.getSalesTotalAmount().add(item.getTotalAmount()));
		salesOrderRepository.save(order);
		SalesOrder loaded = salesOrderRepository.findById(id).orElse(order);
		salePostingService.syncSaleLedger(loaded);
		salePostingService.removeStock(item);
		return ResponseEntity.status(201).body(item);
	}

	private static void applyRequest(SalesOrder order, SalesOrderRequest request) {
		order.setSalesDate(request.getSalesDate());
		order.setInvoiceNumber(request.getInvoiceNumber());
		order.setCustomerId(request.getCustomerId());
		order.setCustomerName(request.getCustomerName());
		order.setCashDiscount(request.getCashDiscount() != null ? request.getCashDiscount() : BigDecimal.ZERO);
		order.clearItems();

		BigDecimal itemsTotal = BigDecimal.ZERO;
		if (request.getItems() != null) {
			for (SalesOrderItemRequest itemRequest : request.getItems()) {
				SalesOrderItem item = toItem(itemRequest);
				order.addItem(item);
				itemsTotal = itemsTotal.add(item.getTotalAmount());
			}
		}

		if (request.getSalesTotalAmount() != null) {
			order.setSalesTotalAmount(request.getSalesTotalAmount());
		}
		else {
			order.setSalesTotalAmount(itemsTotal);
		}
	}

	private static SalesOrderItem toItem(SalesOrderItemRequest request) {
		SalesOrderItem item = new SalesOrderItem();
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
