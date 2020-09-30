package com.adev.gateway.service;


import java.util.Map;

import com.adev.common.base.domian.BaseResult;
import com.adev.gateway.domain.TokenInfo;

public interface TokenService {
	/**
	 * 生成token
	 * @param loginName
	 * @return
	 */
	TokenInfo createToken(String loginName);
	
	/**
	 * 刷新token
	 * @return
	 */
	BaseResult refreshToken(String refreshToken);
	
	/**
	 * 验证token
	 * @param token
	 * @return
	 */
	BaseResult verifyToken(String token);

	/**
	 * 从token用户信息
	 * @param token
	 * @return
	 */
	String getUserFromToken(String token);
}
