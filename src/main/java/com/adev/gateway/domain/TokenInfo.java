package com.adev.gateway.domain;

import lombok.Data;

@Data
public class TokenInfo {
	/**
	 * token
	 */
	private String token;
	
	/**
	 * 刷新token
	 */
	private String refreshToken;
}
