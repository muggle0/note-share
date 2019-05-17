package com.muggle.securitytest.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: security-test
 * @description: 用户信息service
 * @author: muggle
 * @create: 2019-04-11
 **/
@Service
public class MyUserDetailService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        List<MyGrantedAuthority> authorities = new ArrayList<>();
        MyGrantedAuthority myGrantedAuthority = new MyGrantedAuthority();
        myGrantedAuthority.setAuthority("ROLE_test");
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String test = bCryptPasswordEncoder.encode("test");
        authorities.add(myGrantedAuthority);
        return new MyUserDetail().setAuthorities(authorities).setAccountNonExpired(true)
                .setAccountNonLocked(true).setCredentialsNonExpired(true).setEnabled(true)
                .setPassword(test).setUsername("test");
    }
}
