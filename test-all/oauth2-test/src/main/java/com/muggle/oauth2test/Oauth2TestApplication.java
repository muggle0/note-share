package com.muggle.oauth2test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

@SpringBootApplication

public class Oauth2TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(Oauth2TestApplication.class, args);
    }

}
