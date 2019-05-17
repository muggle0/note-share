package com.muggle.bootdubboconsumer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.muggle.SmsServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: poseidon-cloud
 * @description: test
 * @author: muggle
 * @create: 2019-05-15
 **/
@RestController
public class TestController {
    @Reference
    SmsServer smsServer;
    @GetMapping("/test")
    public String test(){
        String test = smsServer.sendSms("test");
        System.out.println(test);
        return "test";
    }
}
