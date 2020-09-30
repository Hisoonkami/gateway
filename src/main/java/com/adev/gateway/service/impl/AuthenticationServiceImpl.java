package com.adev.gateway.service.impl;

import com.adev.common.base.domian.BaseResult;
import com.adev.common.base.enums.ResultEnum;
import com.adev.common.base.utils.MD5Utils;
import com.adev.gateway.domain.SecurityUser;
import com.adev.gateway.domain.TokenInfo;
import com.adev.gateway.service.AccountFeignClient;
import com.adev.gateway.service.AuthenticationService;
import com.adev.gateway.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private AccountFeignClient accountFeignClient;

    @Override
    public BaseResult login(String loginName, String password) {
        ResponseEntity<BaseResult> responseEntity=accountFeignClient.findByLoginName(loginName);
        if(null!=responseEntity){
            Object user=responseEntity.getBody().getData();
            if(null!=user){
                Map<String,Object> userInfoMap=(Map)user;
                String encodedPassword=String.valueOf(userInfoMap.get("password"));
                if(matchesPassword(password,encodedPassword)){
                    SecurityUser securityUser=new SecurityUser();
                    securityUser.setLoginName(loginName);
                    securityUser.setUserInfo(user);
                    TokenInfo tokenInfo=tokenService.createToken(loginName);
                    securityUser.setToken(tokenInfo);
                    return BaseResult.success(securityUser);
                }else {
                    return BaseResult.failure(ResultEnum.WRONG_PASSWORD);
                }
            }
        }
        return BaseResult.failure(ResultEnum.USER_NOT_EXIST);
    }

    private boolean matchesPassword(String rawPassword,String encodedPassword){
        return encodedPassword.equals(MD5Utils.MD5Encode(rawPassword,null));
    }

    @Override
    public boolean authentication(String loginName, String permission) {
        ResponseEntity<BaseResult> responseEntity=accountFeignClient.authentication(loginName,permission);
        if(null!=responseEntity){
            Integer code=responseEntity.getBody().getCode();
            return ResultEnum.SUCCESS.code().equals(code);
        }
        return false;
    }
}
