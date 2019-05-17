package com.muggle.bootdubboserver;

import com.alibaba.dubbo.config.annotation.Service;
import com.muggle.SmsServer;
import org.springframework.stereotype.Component;

/**
 * @program: poseidon-cloud
 * @description:
 * @author: muggle
 * @create: 2019-05-15
 **/
@Component
@Service
public class TestServer implements SmsServer {
    @Override
    public String sendSms(String test) {
        System.out.println(test);
        return "test";
    }
}
