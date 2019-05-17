package com.muggle;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@SpringBootApplication
public class BootProtobufApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootProtobufApplication.class, args);
    }
    @Bean
    ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }

    @Bean
    RestTemplate restTemplate(ProtobufHttpMessageConverter protobufHttpMessageConverter){
        return new RestTemplate(Collections.singletonList(protobufHttpMessageConverter));
    }

}
