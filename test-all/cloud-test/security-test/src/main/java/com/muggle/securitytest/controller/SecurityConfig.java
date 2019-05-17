package com.muggle.securitytest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

/**
 * @program: security-test
 * @description: security配置
 * @author: muggle
 * @create: 2019-04-11
 **/
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    RedisService redisService;
    @Autowired
    MyUserDetailService userDetailService;
    /**
    * @Description: 配置一个  AuthenticationManagerBuilder 它会自动构建一个AuthenticationManager ，AuthenticationManager 作用是登陆验证
    * @Param:
    * @return:
    */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(new BCryptPasswordEncoder());
    }
    @Override
    public void configure(WebSecurity web) throws Exception {

        web.ignoring().antMatchers("/resources/**/*.html", "/resources/**/*.js",
                "/resources/**/*.css", "/resources/**/*.txt",
                "/resources/**/*.png", "/**/*.bmp", "/**/*.gif", "/**/*.png", "/**/*.jpg", "/**/*.ico");
//        super.configure(web);
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        配置登录页等 permitAll表示任何权限都能访问
       http.formLogin().loginPage("/login_page").passwordParameter("username").passwordParameter("password").loginProcessingUrl("/sign_in").permitAll()
               .and().authorizeRequests().antMatchers("/test").hasRole("test")
//               任何请求都被accessDecisionManager() 的鉴权器管理
               .anyRequest().authenticated().accessDecisionManager(accessDecisionManager())
//               登出配置
               .and().logout().logoutSuccessHandler(new MyLogoutSuccessHandler())
//               关闭csrf
               .and().csrf().disable();
//      加自定义过滤器
        http.addFilterAt(getAuthenticationFilter(),UsernamePasswordAuthenticationFilter.class);
//        配置鉴权失败的处理器
        http.exceptionHandling().accessDeniedHandler(new MyAccessDeniedHandler());

    }


    MyUsernamePasswordAuthenticationFilte getAuthenticationFilter(){
        MyUsernamePasswordAuthenticationFilte myUsernamePasswordAuthenticationFilte = new MyUsernamePasswordAuthenticationFilte(redisService);
        myUsernamePasswordAuthenticationFilte.setAuthenticationFailureHandler(new MyUrlAuthenticationFailureHandler());
        myUsernamePasswordAuthenticationFilte.setAuthenticationSuccessHandler(new MyAuthenticationSuccessHandler());
        myUsernamePasswordAuthenticationFilte.setFilterProcessesUrl("/sign_in");
        myUsernamePasswordAuthenticationFilte.setAuthenticationManager(getAuthenticationManager());
        return myUsernamePasswordAuthenticationFilte;
    }
    MyAuthenticationProvider getMyAuthenticationProvider(){
        MyAuthenticationProvider myAuthenticationProvider = new MyAuthenticationProvider(userDetailService,new BCryptPasswordEncoder());
        return myAuthenticationProvider;
    }
    DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailService);
        return daoAuthenticationProvider;
    }
    protected AuthenticationManager getAuthenticationManager()  {
        ProviderManager authenticationManager = new ProviderManager(Arrays.asList(getMyAuthenticationProvider(),daoAuthenticationProvider()));
        return authenticationManager;
    }

    public AccessDecisionManager accessDecisionManager(){
        List<AccessDecisionVoter<? extends Object>> decisionVoters
                = Arrays.asList(
                new MyExpressionVoter(),
                new WebExpressionVoter(),
                new RoleVoter(),
                new AuthenticatedVoter());
        return new UnanimousBased(decisionVoters);

    }
}
