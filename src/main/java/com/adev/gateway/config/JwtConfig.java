package com.adev.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("jwt")
public class JwtConfig {
	//jwt生成密钥
	private String secretKey;
	
	//token过期时间
	private long tokenExpireTime;
	//refreshToken过期时间
	private long refreshTokenExpireTime;
}
