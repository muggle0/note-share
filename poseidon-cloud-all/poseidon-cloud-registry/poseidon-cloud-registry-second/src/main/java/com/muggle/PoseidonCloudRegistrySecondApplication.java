package com.muggle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class PoseidonCloudRegistrySecondApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoseidonCloudRegistrySecondApplication.class, args);
    }

}
