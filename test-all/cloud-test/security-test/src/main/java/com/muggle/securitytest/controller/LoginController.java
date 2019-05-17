package com.muggle.securitytest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @program: security-test
 * @description: test
 * @author: muggle
 * @create: 2019-04-11
 **/

@Controller
public class LoginController {
    /** 
    * @Description: 登陆页面的请求 
    * @Param:  
    * @return:  
    */ 
    @GetMapping("/login_page")
    public String loginPage(){
        return "loginPage.html";
    }
}
