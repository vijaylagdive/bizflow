package com.bizflow.account;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AccountRequest {

	@NotBlank
	@Size(max = 200)
	@JsonProperty("accountName")
	@JsonAlias("name")
	private String accountName;

	@NotBlank
	@Size(max = 20)
	@JsonProperty("accountType")
	@JsonAlias("type")
	private String accountType;

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

}
