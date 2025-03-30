package com.kamegatze.file.manager.configuration.security;

import com.kamegatze.authorization.remote.security.expired.check.ExpiredCheck;
import com.kamegatze.authorization.remote.security.filter.CookieRemoteFilter;
import com.kamegatze.authorization.remote.security.filter.JwtRemoteFilter;
import com.kamegatze.authorization.remote.security.http.entry.point.ExceptionEntryPoint;
import com.kamegatze.authorization.remote.security.provider.CookieRemoteAuthenticationProvider;
import com.kamegatze.authorization.remote.security.provider.JwtRemoteAuthenticationProvider;
import com.kamegatze.file.manager.configuration.security.details.UsersServiceDetails;
import com.kamegatze.file.manager.service.UsersService;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;




@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final UsersService usersService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    @Value("${token.cookie.jwt.access-token.name}")
    private String cookieAccessName;
    @Value("${token.cookie.jwt.refresh-token.name}")
    private String cookieRefreshName;

    @Value("${spring.application.name}")
    private String applicationName;

    private Filter jwtRemoteFilter(AuthenticationManager authenticationManager) throws Exception {
        return new CookieRemoteFilter(authenticationManager, handlerExceptionResolver, cookieAccessName, cookieRefreshName);
    }
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        UsersServiceDetails usersServiceDetails = new UsersServiceDetails();
        usersServiceDetails.setUsersService(usersService);
        authenticationManagerBuilder.authenticationProvider(new CookieRemoteAuthenticationProvider(usersServiceDetails));
        return authenticationManagerBuilder.build();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .addFilterAt(jwtRemoteFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorization ->
                    authorization
                            .requestMatchers(String.format("/%s/**", applicationName)).permitAll()
                            .requestMatchers("/api/**")
                            .hasAnyAuthority(EAuthority.AUTHORITY_READ.name(), EAuthority.AUTHORITY_WRITE.name())
                            .anyRequest()
                            .authenticated()
                )
                .build();
    }

}
