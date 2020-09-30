package com.adev.gateway.service;

import com.adev.gateway.service.impl.AccountFeignClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.adev.common.base.domian.BaseResult;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(value = "common-account", fallback = AccountFeignClientImpl.class)
public interface AccountFeignClient {

	/**
     * 根据登录名获取用户信息
     * @param loginName
     * @return
     */
    @RequestMapping(value="/api/users/findByLoginName",method = RequestMethod.GET)
    ResponseEntity<BaseResult> findByLoginName(@RequestParam(value="loginName")String loginName);

    /**
     * 鉴权
     * @param loginName
     * @param permissionCode
     * @return
     */
    @RequestMapping(value = {"/api/permissions/authentication"},method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<BaseResult> authentication(@RequestParam("loginName") String loginName,
                                              @RequestParam("permissionCode")String permissionCode);
}
