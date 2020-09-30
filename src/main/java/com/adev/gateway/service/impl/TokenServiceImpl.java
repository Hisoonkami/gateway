package com.adev.gateway.service.impl;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
	public TokenInfo createToken(String loginName) {
		Date now = new Date();
		String secretKey=jwtConfig.getSecretKey();
		Date tokenExpDate=new Date(now.getTime() + jwtConfig.getTokenExpireTime());
		Date refreshTokenExpDate=new Date(now.getTime() + jwtConfig.getRefreshTokenExpireTime());
		TokenInfo tokenInfo=new TokenInfo();
		tokenInfo.setToken(buildToken(secretKey, tokenExpDate, loginName));
		tokenInfo.setRefreshToken(buildToken(secretKey, refreshTokenExpDate, loginName));
		return tokenInfo;
	}

	private String buildToken(String secretKey,Date exp,String loginName) {
		JwtBuilder jwtBuilder=Jwts.builder().setExpiration(exp).signWith(SignatureAlgorithm.HS256, secretKey);
		jwtBuilder.claim("loginName",loginName);
		return jwtBuilder.compact();
	}
	
	@Override
	public BaseResult refreshToken(String refreshToken) {
		BaseResult result=verifyToken(refreshToken);
		if(ResultEnum.SUCCESS.code().equals(result.getCode())) {
			String loginName=getUserFromToken(refreshToken);
			if(StringUtils.isNotBlank(loginName)){
				return BaseResult.success(createToken(loginName));
			}else {
				BaseResult.failure(ResultEnum.USER_NOT_EXIST);
			}
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

	@Override
	public String getUserFromToken(String token) {
		try {
			Map<String, Object> claims=Jwts.parser().setSigningKey(jwtConfig.getSecretKey()).parseClaimsJws(token).getBody();
			if(null!=claims){
				return String.valueOf(claims.get("loginName"));
			}
		} catch (ExpiredJwtException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

}
