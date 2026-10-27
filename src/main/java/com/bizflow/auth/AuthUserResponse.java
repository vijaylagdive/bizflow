package com.bizflow.auth;

public class AuthUserResponse {

	private final Long userId;

	private final String username;

	public AuthUserResponse(Long userId, String username) {
		this.userId = userId;
		this.username = username;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

}
