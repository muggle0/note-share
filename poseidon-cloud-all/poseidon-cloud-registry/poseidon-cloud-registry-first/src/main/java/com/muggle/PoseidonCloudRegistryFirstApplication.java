package com.muggle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class PoseidonCloudRegistryFirstApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoseidonCloudRegistryFirstApplication.class, args);
    }

}
