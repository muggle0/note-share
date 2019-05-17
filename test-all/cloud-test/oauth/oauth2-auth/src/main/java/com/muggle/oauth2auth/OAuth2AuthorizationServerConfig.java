package com.muggle.oauth2auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

/**
 * @program: oauth
 * @description: 授权服务器配置
 * @author: muggle
 * @create: 2019-04-15
 **/
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()                  // 使用in-memory存储客户端信息
                .withClient("client")       // client_id
                .secret("secret")                   // client_secret
                .authorizedGrantTypes("authorization_code")     // 该client允许的授权类型
                .scopes("app");
    }


}
