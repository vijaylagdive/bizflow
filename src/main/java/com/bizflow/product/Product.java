package com.bizflow.product;

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
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Long productId;

	@Column(name = "product_name", nullable = false, length = 200)
	private String productName = "";

	@Column(name = "gst_percent", nullable = false, precision = 5, scale = 2)
	private BigDecimal gstPercent = BigDecimal.ZERO;

	@Column(name = "hsn_sac", nullable = false, length = 8)
	private String hsnSacCode = "";

	@JsonIgnore
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private ProductCategory category;

	public Long getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public BigDecimal getGstPercent() {
		return gstPercent;
	}

	public void setGstPercent(BigDecimal gstPercent) {
		this.gstPercent = gstPercent;
	}

	public String getHsnSacCode() {
		return hsnSacCode;
	}

	public void setHsnSacCode(String hsnSacCode) {
		this.hsnSacCode = hsnSacCode;
	}

	public Long getCategoryId() {
		return category != null ? category.getCategoryId() : null;
	}

	public String getCategoryName() {
		return category != null ? category.getCategoryName() : null;
	}

	public ProductCategory getCategory() {
		return category;
	}

	public void setCategory(ProductCategory category) {
		this.category = category;
	}

}
