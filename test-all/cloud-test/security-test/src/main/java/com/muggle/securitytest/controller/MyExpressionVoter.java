package com.muggle.securitytest.controller;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.Collection;

/**
 * @program: security-test
 * @description: 鉴权投票器
 * @author: muggle
 * @create: 2019-04-11
 **/

public class MyExpressionVoter extends WebExpressionVoter {
    @Override
    public int vote(Authentication authentication, FilterInvocation fi, Collection<ConfigAttribute> attributes) {
//        这里写鉴权逻辑
        return 0;
    }
}
