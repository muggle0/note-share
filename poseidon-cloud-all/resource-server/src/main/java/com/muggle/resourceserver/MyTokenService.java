//package com.muggle.resourceserver;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.oauth2.common.OAuth2AccessToken;
//import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
//import org.springframework.security.oauth2.provider.OAuth2Authentication;
//import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
//import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
//import org.springframework.security.oauth2.provider.token.TokenStore;
//import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
//import org.springframework.stereotype.Service;
//
//import java.util.UUID;
//
///**
// * @program: poseidon-cloud-all
// * @description:
// * @author: muggle
// * @create: 2019-05-25
// **/
//
//
//@Service
//public class MyTokenService implements ResourceServerTokenServices {
//    @Autowired
//    private TokenStore tokenStore;
//
//    @Autowired
//    private RedisConnectionFactory redisConnectionFactory;
//    //根据accessToken加载客户端信息
//    @Override
//    public OAuth2Authentication loadAuthentication(String s) throws AuthenticationException, InvalidTokenException {
//        OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(s);
//        return oAuth2Authentication;
//    }
//    //根据accessToken获取完整的访问令牌详细信息。
//    @Override
//    public OAuth2AccessToken readAccessToken(String s) {
//        OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(s);
//        return oAuth2AccessToken;
//    }
//
//    @Bean
//    public TokenStore tokenStore() {
//
//        RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
//        // 解决同一username每次登陆access_token都相同的问题
//        redisTokenStore.setAuthenticationKeyGenerator(new RandomAuthenticationKeyGenerator());
//
//        return redisTokenStore;
//    }
//
//    public static class RandomAuthenticationKeyGenerator implements AuthenticationKeyGenerator {
//        @Override
//        public String extractKey(OAuth2Authentication oAuth2Authentication) {
//            return UUID.randomUUID().toString();
//        }
//    }
//}
