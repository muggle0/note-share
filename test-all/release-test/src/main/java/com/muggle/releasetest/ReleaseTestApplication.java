package com.muggle.releasetest;

import com.alibaba.dubbo.spring.boot.annotation.EnableDubboConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubboConfiguration
public class ReleaseTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReleaseTestApplication.class, args);
    }

}
