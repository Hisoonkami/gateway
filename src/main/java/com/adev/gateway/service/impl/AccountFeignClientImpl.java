package com.adev.gateway.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.adev.common.base.domian.BaseResult;
import com.adev.common.base.enums.ResultEnum;
import com.adev.gateway.service.AccountFeignClient;

@Service
public class AccountFeignClientImpl implements AccountFeignClient {

	@Override
	public ResponseEntity<BaseResult> findByLoginName(String loginName) {
		return ResponseEntity.ok(BaseResult.failure(ResultEnum.INTERFACE_EXCEED_LOAD));
	}

	@Override
	public ResponseEntity<BaseResult> authentication(String loginName, String permissionCode) {
		return ResponseEntity.ok(BaseResult.failure(ResultEnum.INTERFACE_EXCEED_LOAD));
	}

}
