package com.adev.gateway.domain;

import lombok.Data;

@Data
public class SecurityUser {
    /**
     * 登录名
     */
    private String loginName;

    /**
     * 用户信息
     */
    private Object userInfo;

    /**
     * token
     */
    private TokenInfo token;
}
