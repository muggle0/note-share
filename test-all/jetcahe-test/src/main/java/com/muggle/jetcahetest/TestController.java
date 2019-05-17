package com.muggle.jetcahetest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: poseidon-cloud
 * @description:
 * @author: muggle
 * @create: 2019-05-17
 **/
@RestController
public class TestController {

    @Autowired
    Server server;

    @GetMapping("/test")
    public String test(){
        System.out.println(server.test("ss"));
        return "ss";
    }
}
