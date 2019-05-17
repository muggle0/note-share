package com.muggle.securitytest.controller;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: security-test
 * @description: 用户登陆逻辑过滤器
 * @author: muggle
 * @create: 2019-04-11
 **/

public class MyUsernamePasswordAuthenticationFilte extends UsernamePasswordAuthenticationFilter {
    private RedisService redisService;
    private boolean postOnly = true;

    public MyUsernamePasswordAuthenticationFilte(RedisService redisService){
        this.redisService=redisService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        redisService.getcCode(request);
        return super.attemptAuthentication(request,response);
    }
}
