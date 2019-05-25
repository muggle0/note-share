package com.muggle.resourceserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResourceServerApplicationTests {


    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void token_password() {
  /*      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", "admin");
        params.add("password", "admin");
        params.add("scope", "scope1 scope2");
//        第三方应用登陆
        *//*String response = restTemplate.withBasicAuth("clientId", "clientSecret").
//                用户的用户名密码

        postForObject("/test", params, String.class);*//*
        restTemplate.
        System.out.println(response);*/
    }

}
