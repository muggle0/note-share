package com.muggle.securitytest.controller;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * @program: security-test
 * @description: 用户对象
 * @author: muggle
 * @create: 2019-04-11
 **/
@Data
@Accessors(chain = true)
public class MyUserDetail implements UserDetails {

    private List<MyGrantedAuthority> authorities;

    private String password;

    private String username;

    private boolean accountNonExpired;

    private boolean accountNonLocked;

    private boolean credentialsNonExpired;

    private boolean enabled;


}
