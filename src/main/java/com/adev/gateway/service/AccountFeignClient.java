package com.adev.gateway.service;

import com.adev.gateway.service.impl.AccountFeignClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.adev.common.base.domian.BaseResult;

@FeignClient(value = "common-account", fallback = AccountFeignClientImpl.class)
public interface AccountFeignClient {

	/**
     * 登录
     * @param loginName
     * @param password
     * @return
     */
    @RequestMapping(value="/api/users/login",method = RequestMethod.GET)
    ResponseEntity<BaseResult> login(@RequestParam(value="loginName")String loginName,@RequestParam(value="password")String password);
}
