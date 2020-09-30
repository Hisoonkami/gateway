package com.adev.gateway.service;

import com.adev.common.base.domian.BaseResult;

public interface AuthenticationService {
    /**
     * 登录
     * @param loginName
     * @param password
     * @return
     */
    BaseResult login(String loginName,String password);

    /**
     * 权限检查
     * @param loginName
     * @param permission
     * @return
     */
    boolean authentication(String loginName,String permission);
}
