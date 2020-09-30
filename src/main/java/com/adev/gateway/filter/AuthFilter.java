package com.adev.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.adev.gateway.domain.SecurityUser;
import com.adev.gateway.service.AuthenticationService;
import com.adev.gateway.utils.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ServerWebExchange;

import com.adev.common.base.domian.BaseResult;
import com.adev.common.base.enums.ResultEnum;
import com.adev.gateway.service.TokenService;

import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered{

	private static final Logger LOG = LoggerFactory.getLogger(AuthFilter.class);

	private static String AUTH_URL_PREFIX="/api/user";

	private static String REFRESH_TOKEN_URL="/api/user/refreshToken";

	private static String LOGIN_URL="/api/login";

	private static String PERMISSIONS_URL="/api/user/permissions/";

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private TokenService tokenService;

	@Override
	public int getOrder() {
		return -100;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String url = exchange.getRequest().getURI().getPath();
		ServerHttpResponse response = exchange.getResponse();
		if(url.startsWith(AUTH_URL_PREFIX)) {
			//从请求头中取出token
	        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
	        if(StringUtils.isEmpty(token)) {//token为空直接返回
	        	return ResponseUtil.writeResult(response,HttpStatus.UNAUTHORIZED,BaseResult.failure(ResultEnum.USER_NOT_LOGGED_IN));
	        }
	        if(REFRESH_TOKEN_URL.equals(url)) {//刷新token
	        	return ResponseUtil.writeResult(response,HttpStatus.OK,tokenService.refreshToken(token));
	        }
	        BaseResult result=tokenService.verifyToken(token);
	        if(!ResultEnum.SUCCESS.code().equals(result.getCode())) {
	        	return ResponseUtil.writeResult(response,HttpStatus.OK,result);
	        }
			String loginName=tokenService.getUserFromToken(token);
			exchange.getRequest().mutate().header("username",loginName);
	        if(url.startsWith(PERMISSIONS_URL)) {//需要授权的url，先判断是否拥有权限
                if(!StringUtils.isEmpty(loginName)) {
                    //调用接口判断loginName是否具有权限
                    String urlSuffix=url.substring(PERMISSIONS_URL.length());
                    String permissions=urlSuffix.substring(0, urlSuffix.indexOf("/"));
                    String permissionCode=permissions;
                    boolean auth=authenticationService.authentication(loginName,permissionCode);
                    if(!auth){
                        return ResponseUtil.writeResult(response,HttpStatus.UNAUTHORIZED,BaseResult.failure(ResultEnum.PERMISSION_NO_ACCESS));
                    }
                }else {
                    return ResponseUtil.writeResult(response,HttpStatus.UNAUTHORIZED,BaseResult.failure(ResultEnum.USER_NOT_LOGGED_IN));
                }
	        }
		}	else if(LOGIN_URL.equals(url)){//登录
			String method = exchange.getRequest().getMethodValue();
			if(!RequestMethod.POST.name().equalsIgnoreCase(method)) {
				return ResponseUtil.writeResult(response,HttpStatus.METHOD_NOT_ALLOWED,BaseResult.failure(ResultEnum.METHOD_NOT_ALLOWED));
			}
			String loginName=exchange.getRequest().getQueryParams().getFirst("loginName");
			String password=exchange.getRequest().getQueryParams().getFirst("password");
            BaseResult loginResult=authenticationService.login(loginName,password);
            if(loginResult.getCode().equals(ResultEnum.SUCCESS.code())){
                Object data=loginResult.getData();
                SecurityUser securityUser=null;
                if(null!=data){
                    securityUser=(SecurityUser)data;
                }
                return ResponseUtil.writeResult(response, HttpStatus.OK, securityUser);
            }else {
                return ResponseUtil.writeResult(response, HttpStatus.OK, loginResult);
            }
		}
		return chain.filter(exchange);
	}

}
