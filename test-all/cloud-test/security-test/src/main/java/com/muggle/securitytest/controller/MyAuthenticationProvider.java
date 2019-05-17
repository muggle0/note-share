package com.muggle.securitytest.controller;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @program: security-test
 * @description: 验证器
 * @author: muggle
 * @create: 2019-04-11
 **/

public class MyAuthenticationProvider  implements AuthenticationProvider {
    private UserDetailsService userDetailsService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public MyAuthenticationProvider(UserDetailsService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//       这里写验证逻辑
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }
}
