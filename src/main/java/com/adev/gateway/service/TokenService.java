package com.adev.gateway.service;


import java.util.Map;

import com.adev.common.base.domian.BaseResult;

public interface TokenService {
	/**
	 * 根据用户信息构造新token
	 * @return
	 */
	public BaseResult buildToken(Map<String,Object> userInfo);
	
	/**
	 * 刷新token
	 * @return
	 */
	public BaseResult refreshToken(String refreshToken);
	
	/**
	 * 验证token
	 * @param token
	 * @return
	 */
	public BaseResult verifyToken(String token);
}
