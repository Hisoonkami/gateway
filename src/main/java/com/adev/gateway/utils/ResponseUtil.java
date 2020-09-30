package com.adev.gateway.utils;

import com.adev.common.base.domian.BaseResult;
import com.adev.gateway.domain.SecurityUser;
import com.adev.gateway.domain.TokenInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    public static Mono<Void>  writeResult(ServerHttpResponse response, HttpStatus status, BaseResult result){
        return writeResult(response,status,result,null);
    }

    public static Mono<Void>  writeResult(ServerHttpResponse response, HttpStatus status, BaseResult result,Map<String,String> headers){
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
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
        response.getHeaders().add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        if(null!=headers&&!headers.isEmpty()){
            for (String headerKey:headers.keySet()){
                response.getHeaders().add(headerKey, headers.get(headerKey));
            }
        }
        return response.writeWith(Mono.just(buffer));
    }

    public static Mono<Void>  writeResult(ServerHttpResponse response, HttpStatus status, SecurityUser securityUser){
        Map<String,String> headers=new HashMap<>();
        BaseResult baseResult=BaseResult.success();
        if(null!=securityUser){
            TokenInfo tokenInfo=securityUser.getToken();
            if(null!=tokenInfo){
                headers.put("Authorization",tokenInfo.getToken());
                headers.put("RefreshToken",tokenInfo.getRefreshToken());
            }
            baseResult.setData(securityUser.getUserInfo());
        }
        return writeResult(response,status,baseResult,headers);
    }

}
