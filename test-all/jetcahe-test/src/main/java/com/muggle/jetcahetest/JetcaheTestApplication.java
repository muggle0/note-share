package com.muggle.jetcahetest;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMethodCache(basePackages = "com.muggle.jetcahetest")
@EnableCreateCacheAnnotation
public class JetcaheTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(JetcaheTestApplication.class, args);
    }

}
