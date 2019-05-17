package com.muggle.securitytest.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @program: security-test
 * @description: 登陆成功处理器
 * @author: muggle
 * @create: 2019-04-11
 **/

public class MyAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");
        final PrintWriter writer = response.getWriter();
        writer.write("{\"code\":\"200\",\"msg\":\"登录成功\"}");
        writer.close();
    }
}
