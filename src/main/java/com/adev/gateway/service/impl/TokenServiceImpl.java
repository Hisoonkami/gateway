package com.adev.gateway.service.impl;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adev.common.base.domian.BaseResult;
import com.adev.common.base.enums.ResultEnum;
import com.adev.gateway.config.JwtConfig;
import com.adev.gateway.domain.TokenInfo;
import com.adev.gateway.service.TokenService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenServiceImpl implements TokenService {

	@Autowired
	private JwtConfig jwtConfig;
	
	@Override
	public BaseResult buildToken(Map<String,Object> userInfo) {
		Date now = new Date();
        String secretKey=jwtConfig.getSecretKey();
        Date tokenExpDate=new Date(now.getTime() + jwtConfig.getTokenExpireTime());
        Date refreshTokenExpDate=new Date(now.getTime() + jwtConfig.getRefreshTokenExpireTime());
        TokenInfo tokenInfo=new TokenInfo();
        tokenInfo.setToken(buildToken(secretKey, tokenExpDate, userInfo));
        tokenInfo.setRefreshToken(buildToken(secretKey, refreshTokenExpDate, userInfo));
		return BaseResult.success(tokenInfo);
	}

	private String buildToken(String secretKey,Date exp,Map<String,Object> claims) {
		JwtBuilder jwtBuilder=Jwts.builder().setExpiration(exp).signWith(SignatureAlgorithm.HS256, secretKey);
		jwtBuilder.addClaims(claims);
		return jwtBuilder.compact();
	}
	
	@Override
	public BaseResult refreshToken(String refreshToken) {
		BaseResult result=verifyToken(refreshToken);
		if(ResultEnum.SUCCESS.code().equals(result.getCode())) {
			Map<String, Object> userInfo=null;
			Object data=result.getData();
			if(null!=data) {
				userInfo=(Map)data;
			}
			return buildToken(userInfo);
		}
		return result;
	}

	@Override
	public BaseResult verifyToken(String token) {
		try {
			Map<String, Object> claims=Jwts.parser().setSigningKey(jwtConfig.getSecretKey()).parseClaimsJws(token).getBody();
	        return BaseResult.success(claims);
		} catch (ExpiredJwtException e) {
			return BaseResult.failure(ResultEnum.TOKEN_INVALID);
		} catch (Exception e) {
			e.printStackTrace();
			return BaseResult.failure(ResultEnum.TOKEN_ILLEGAL);
		}
	}

}
