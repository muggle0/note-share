package com.muggle.oauth2test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @program: test-all
 * @description:
 * @author: muggle
 * @create: 2019-05-18
 **/
@RestController
public class TestController {
    @Autowired
    RestTemplate restTemplate;
    @GetMapping("/test")
    public String tes(){
        restTemplate.post
        return "";
    }
}
