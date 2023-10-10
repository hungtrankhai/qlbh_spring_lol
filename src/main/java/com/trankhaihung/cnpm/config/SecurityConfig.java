package com.trankhaihung.cnpm.config;

import com.trankhaihung.cnpm.security.*;
import com.trankhaihung.cnpm.security.JwtAuthenticationFilter;
import com.trankhaihung.cnpm.security.oauth2.CustomOAuth2UserService;
import com.trankhaihung.cnpm.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.trankhaihung.cnpm.service.UserLoginService;
import com.trankhaihung.cnpm.security.CustomUserDetailsService;
import com.trankhaihung.cnpm.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Created by rajeevkumarsingh on 01/08/17.
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private UserLoginService userLoginService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

        // The pages does not require login
        http.authorizeRequests().antMatchers("/", "/login-page", "/logout").permitAll();
        http.authorizeRequests().antMatchers("/management").access("hasRole('ROLE_ADMIN')");
        http.authorizeRequests().and().exceptionHandling().accessDeniedPage("/403");

        http.authorizeRequests()
                .and().formLogin()//
                .loginProcessingUrl("/management/product-list") // Submit URL
                .loginPage("/login-page")//
                .defaultSuccessUrl("/product-list")//
                .failureUrl("/login?error=true")//
                .usernameParameter("email")//
                .passwordParameter("password");
                // Config for Logout Page
//                .and().logout().logoutUrl("/doLogout").logoutSuccessUrl("/login-page");

        // Oauth2
//        http.oauth2Login()
//                    .loginPage("/login-page")
//                    .userInfoEndpoint()
//                    .userService(customOAuth2UserService)
//                .and()
//                .successHandler((request, response, authentication) -> {
//                    CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
//
//                    userLoginService.processOAuthPostLogin(oauthUser);
//
//                    response.sendRedirect("/");
//                });

        // Add our custom JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    }
}