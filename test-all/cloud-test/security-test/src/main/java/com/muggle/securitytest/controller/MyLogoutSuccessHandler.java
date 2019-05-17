package com.muggle.securitytest.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @program: security-test
 * @description: 登出成功处理器
 * @author: muggle
 * @create: 2019-04-11
 **/

public class MyLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        final PrintWriter writer = response.getWriter();

        writer.write("{\"code\":\"200\",\"msg\":\"登出成功\"}");
        writer.close();
    }
}
