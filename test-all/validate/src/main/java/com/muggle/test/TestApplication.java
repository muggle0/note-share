package com.muggle.test;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableMethodCache(basePackages = "com.muggle.test")
@EnableCreateCacheAnnotation
@EnableAsync
public class TestApplication {


    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
    @Bean
    public VolidatorBean volidatorBean(){
        return new VolidatorBean().setId(1L).setField("barcode").setType(1);
    }
}

