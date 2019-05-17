package com.muggle.securitytest.controller;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: security-test
 * @description: 测试
 * @author: muggle
 * @create: 2019-04-11
 **/
@RestController
public class TestController {
    @GetMapping("test")
    public String test(){
        SecurityContext context = SecurityContextHolder.getContext();
        return "hi 你好啊";
    }
}
