package com.muggle.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(TestController.class)//当BookService在类路径中时并且当前容器中没有这个Bean的情况下,自动配置
public class AutoConfig {


    /*@Bean
    @ConditionalOnMissingBean(TestController.class)//当容器中没有指定Bean的情况下
    public TestController bookService(){
        TestController bookService = new TestController();
        return bookService;
    }*/
}
