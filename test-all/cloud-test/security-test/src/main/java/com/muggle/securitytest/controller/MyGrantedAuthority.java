package com.muggle.securitytest.controller;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

/**
 * @program: security-test
 * @description: 权限对象
 * @author: muggle
 * @create: 2019-04-11
 **/
@Data
public class MyGrantedAuthority implements GrantedAuthority {
    private String authority;
}
