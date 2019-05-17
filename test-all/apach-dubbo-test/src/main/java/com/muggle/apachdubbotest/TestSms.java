package com.muggle.apachdubbotest;

import com.alibaba.dubbo.config.annotation.Service;
import com.muggle.SmsServer;
import org.springframework.stereotype.Component;

/**
 * @program: poseidon-cloud
 * @description: test
 * @author: muggle
 * @create: 2019-05-16
 **/
@Service(interfaceClass = SmsServer.class)
@Component
public class TestSms implements SmsServer{

    @Override
    public String sendSms(String test) {
        System.out.println("sssssssssssssssssssss");
        return "testssssssssssssssssssssssss";
    }
}
