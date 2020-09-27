package com.adev.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.web.server.ServerWebExchange;

import com.adev.common.base.domian.BaseResult;
import com.adev.common.base.enums.ResultEnum;
import com.adev.gateway.service.AccountFeignClient;
import com.adev.gateway.service.TokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered{

	private static final Logger LOG = LoggerFactory.getLogger(AuthFilter.class);
	
	private static String AUTH_URL_PREFIX="/api/user";
	
	private static String REFRESH_TOKEN_URL="/api/user/refreshToken";
	
	private static String LOGIN_URL="/api/login";
	
	private static String PERMISSIONS_URL="/api/user/permissions/";
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private AccountFeignClient accountFeignClient;
	
	@Override
	public int getOrder() {
		return -100;
	}

	public static void main(String[] args) {
		String str="/api/user/permissions/delete_course${courseId}/courses";
//		System.out.println(str.indexOf(REFRESH_TOKEN_URL));
		String urlSuffix=str.substring(PERMISSIONS_URL.length());
		System.out.println(urlSuffix);
		System.out.println(urlSuffix.substring(0,urlSuffix.indexOf("/")));
	}
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String url = exchange.getRequest().getURI().getPath();
		ServerHttpResponse response = exchange.getResponse();
		if(url.startsWith(AUTH_URL_PREFIX)) {
			//从请求头中取出token
	        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
	        if(StringUtils.isEmpty(token)) {//token为空直接返回
	        	return writeResult(response,HttpStatus.UNAUTHORIZED,BaseResult.failure(ResultEnum.USER_NOT_LOGGED_IN));
	        }
	        if(REFRESH_TOKEN_URL.equals(url)) {//刷新token
	        	return writeResult(response,HttpStatus.OK,tokenService.refreshToken(token));
	        }
	        BaseResult result=tokenService.verifyToken(token);
	        if(!ResultEnum.SUCCESS.code().equals(result.getCode())) {
	        	return writeResult(response,HttpStatus.OK,result);
	        }
	        if(url.startsWith(PERMISSIONS_URL)) {//需要授权的url，先判断是否拥有权限
	        	Object data=result.getData();
	        	String loginName=null;
	        	if(null!=data) {
	        		Map<String, Object> dataMap=(Map)data;
	        		Object loginNameObj=dataMap.get("loginName");
	        		if(null!=loginNameObj) {
	        			loginName=String.valueOf(loginNameObj);
	        		}
	        		if(!StringUtils.isEmpty(loginName)) {
	        			//调用接口判断loginName是否具有权限
	        			String urlSuffix=url.substring(PERMISSIONS_URL.length());
	        			String permissions=urlSuffix.substring(0, urlSuffix.indexOf("/"));
	        			String permissionCode=permissions;
	        			String targetKey=null;
	        			if(permissions.indexOf("-")>0) {
	        				String[] codeAndTargetKey=permissions.split("-");
	        				permissionCode=codeAndTargetKey[0];
	        				targetKey=codeAndTargetKey[1];
	        			}
	        			LOG.info("校验用户：{}在{}上是否有{}权限",loginName,targetKey,permissionCode);
	        		}else {
	        			return writeResult(response,HttpStatus.UNAUTHORIZED,BaseResult.failure(ResultEnum.USER_NOT_LOGGED_IN));
					}
	        	}
	        }
		}	else if(LOGIN_URL.equals(url)){//登录
			String method = exchange.getRequest().getMethodValue();
//			if(!RequestMethod.POST.name().equalsIgnoreCase(method)) {
//				return writeResult(response,HttpStatus.METHOD_NOT_ALLOWED,BaseResult.failure(ResultEnum.METHOD_NOT_ALLOWED));
//			}
			String loginName=exchange.getRequest().getQueryParams().getFirst("loginName");
			String password=exchange.getRequest().getQueryParams().getFirst("password");
			ResponseEntity<BaseResult> responseEntity=accountFeignClient.login(loginName, password);
			if(ResultEnum.SUCCESS.code().equals(responseEntity.getBody().getCode())) {
				Map<String, Object> resultMap=new HashMap<String, Object>();
				Object userInfo=responseEntity.getBody().getData();
				BaseResult tokenResult=tokenService.buildToken((Map<String, Object>)userInfo);
				if(null!=tokenResult&&ResultEnum.SUCCESS.code().equals(tokenResult.getCode())) {
					resultMap.put("userInfo", userInfo);
					resultMap.put("tokenInfo", tokenResult.getData());
					return writeResult(response, HttpStatus.OK, BaseResult.success(resultMap));
				}
				return writeResult(response, HttpStatus.OK, tokenResult);
			}else {
				return writeResult(response,HttpStatus.OK,accountFeignClient.login(loginName, password).getBody());
			}
		}
		return chain.filter(exchange);
	}

	private Mono<Void> writeResult(ServerHttpResponse response,HttpStatus status,BaseResult result){
		ObjectMapper objectMapper=new ObjectMapper();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		byte[] bits = null;
		try {
			String resultJsonString=objectMapper.writeValueAsString(result);
			bits=resultJsonString.getBytes(StandardCharsets.UTF_8);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	DataBuffer buffer = response.bufferFactory().wrap(bits);
    	response.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
    	return response.writeWith(Mono.just(buffer));
	}
}
