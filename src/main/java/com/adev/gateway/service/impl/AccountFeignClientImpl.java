package com.adev.gateway.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.adev.common.base.domian.BaseResult;
import com.adev.common.base.enums.ResultEnum;
import com.adev.gateway.service.AccountFeignClient;

@Service
public class AccountFeignClientImpl implements AccountFeignClient {

	@Override
	public ResponseEntity<BaseResult> login(String loginName, String password) {
		return ResponseEntity.ok(BaseResult.failure(ResultEnum.INTERFACE_EXCEED_LOAD));
	}

}
