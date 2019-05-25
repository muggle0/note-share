package com.muggle.resourceserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: poseidon-cloud-all
 * @description:
 * @author: muggle
 * @create: 2019-05-25
 **/


@RestController
public class TestController {


    @GetMapping("/test")
    public String test(){
        return "test";
    }


}
