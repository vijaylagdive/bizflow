package com.bizflow.salesorder;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales_order_items")
public class SalesOrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "sales_order_id", nullable = false)
	@JsonIgnore
	private SalesOrder salesOrder;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(nullable = false, precision = 14, scale = 3)
	private BigDecimal qty;

	@Column(length = 30)
	private String unit;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal rate = BigDecimal.ZERO;

	@Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@Column(name = "gst_percent", nullable = false, precision = 5, scale = 2)
	private BigDecimal gstPercent = BigDecimal.ZERO;

	public Long getId() {
		return id;
	}

	public Long getSalesOrderId() {
		return salesOrder != null ? salesOrder.getId() : null;
	}

	public SalesOrder getSalesOrder() {
		return salesOrder;
	}

	public void setSalesOrder(SalesOrder salesOrder) {
		this.salesOrder = salesOrder;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public BigDecimal getQty() {
		return qty;
	}

	public void setQty(BigDecimal qty) {
		this.qty = qty;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getGstPercent() {
		return gstPercent;
	}

	public void setGstPercent(BigDecimal gstPercent) {
		this.gstPercent = gstPercent;
	}

}
