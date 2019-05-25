package com.muggle.oauth2test;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @program: test-all
 * @description:
 * @author: muggle
 * @create: 2019-05-21
 **/

public class MyUserDetailService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        MyUserdetail myUserdetail = new MyUserdetail();
        myUserdetail.setPassword("s");
        myUserdetail.setUsername("s");
        return myUserdetail;
    }
}
