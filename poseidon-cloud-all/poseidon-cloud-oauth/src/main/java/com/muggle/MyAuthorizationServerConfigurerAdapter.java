//package com.muggle;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
//
///**
// * @program: poseidon-cloud-all
// * @description: 授权适配器
// * @author: muggle
// * @create: 2019-05-18
// **/
//
//public class MyAuthorizationServerConfigurerAdapter extends AuthorizationServerConfigurerAdapter {
//    @Autowired
//    private RedisConnectionFactory redisConnectionFactory;
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Autowired
//    private CustomUserDetailsService customUserDetailsService;
//    @Autowired
//    private CustomWebResponseExceptionTranslator customWebResponseExceptionTranslator;
//    @Autowired
//    private CustomAuthEntryPoint customAuthEntryPoint;
//    @Autowired
//    private CustomAccessDeniedHandler customAccessDeniedHandler;
//
//    @Override
//    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//        security.allowFormAuthenticationForClients()
//                .checkTokenAccess("isAuthenticated()")
//                .tokenKeyAccess("permitAll()")
//                .authenticationEntryPoint(customAuthEntryPoint)
//                .accessDeniedHandler(customAccessDeniedHandler);
//        log.info("AuthorizationServerSecurityConfigurer is complete");
//    }
//
//    /**
//     * 配置客户端详情信息(Client Details)
//     * clientId：（必须的）用来标识客户的Id。
//     * secret：（需要值得信任的客户端）客户端安全码，如果有的话。
//     * scope：用来限制客户端的访问范围，如果为空（默认）的话，那么客户端拥有全部的访问范围。
//     * authorizedGrantTypes：此客户端可以使用的授权类型，默认为空。
//     * authorities：此客户端可以使用的权限（基于Spring Security authorities）。
//     */
//    @Override
//    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.withClientDetails(clientDetails());
//        log.info("ClientDetailsServiceConfigurer is complete!");
//    }
//
//    /**
//     * 配置授权、令牌的访问端点和令牌服务
//     * tokenStore：采用redis储存
//     * authenticationManager:身份认证管理器, 用于"password"授权模式
//     */
//    @Override
//    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
//        endpoints
//                .authenticationManager(authenticationManager)
//                .userDetailsService(customUserDetailsService)
//                .tokenServices(tokenServices())
//                .exceptionTranslator(customWebResponseExceptionTranslator);
//
//        log.info("AuthorizationServerEndpointsConfigurer is complete.");
//    }
//
//
//    /**
//     * redis存储方式
//     *
//     * @return
//     */
//    @Bean("redisTokenStore")
//    public TokenStore redisTokenStore() {
//        return new RedisTokenStore(redisConnectionFactory);
//    }
//
//   /* @Bean
//    public TokenStore tokenStore() {
//        return new JwtTokenStore(jwtTokenEnhancer());
//    }*/
//
//
//    /**
//     * 客户端信息配置在数据库
//     *
//     * @return
//     */
//    @Bean
//    public ClientDetailsService clientDetails() {
//        return new JdbcClientDetailsService(dataSource);
//    }
//
//    /**
//     * 采用RSA加密生成jwt
//     *
//     * @return
//     */
//    @Bean
//    public JwtAccessTokenConverter jwtTokenEnhancer() {
//        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("hq-jwt.jks"), "hq940313".toCharArray());
//       /* JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
//        jwtAccessTokenConverter.setKeyPair(keyStoreKeyFactory.getKeyPair("hq-jwt"));*/
//        CustJwtAccessTokenConverter tokenConverter = new CustJwtAccessTokenConverter();
//        tokenConverter.setKeyPair(keyStoreKeyFactory.getKeyPair("hq-jwt"));
//        return tokenConverter;
//    }
//    /**
//     * 配置生成token的有效期以及存储方式（此处用的redis）
//     *
//     * @return
//     */
//    @Bean
//    public DefaultTokenServices tokenServices() {
//        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
//        defaultTokenServices.setTokenStore(redisTokenStore());
//        defaultTokenServices.setTokenEnhancer(jwtTokenEnhancer());
//        defaultTokenServices.setSupportRefreshToken(true);
//        defaultTokenServices.setAccessTokenValiditySeconds((int) TimeUnit.MINUTES.toSeconds(30));
//        defaultTokenServices.setRefreshTokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(1));
//        return defaultTokenServices;
//    }
//
//}
