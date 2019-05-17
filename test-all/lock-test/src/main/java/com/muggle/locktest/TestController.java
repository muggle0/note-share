package com.muggle.locktest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: lock-test
 * @description: test
 * @author: muggle
 * @create: 2019-03-25
 **/
@RestController
public class TestController {
    @Autowired
    TestService testService;

    @GetMapping("/test")
    public String test2(){
        return testService.test();
    }
}
