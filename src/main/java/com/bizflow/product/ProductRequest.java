package com.bizflow.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProductRequest {

	@NotBlank
	@Size(max = 200)
	private String productName;

	@NotNull
	private Long categoryId;

	@NotNull
	@DecimalMin(value = "0.00")
	@DecimalMax(value = "100.00")
	@Digits(integer = 3, fraction = 2)
	private BigDecimal gstPercent;

	@Size(max = 8)
	@Pattern(regexp = "^$|\\d{4,8}")
	private String hsnSacCode;

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
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

}
