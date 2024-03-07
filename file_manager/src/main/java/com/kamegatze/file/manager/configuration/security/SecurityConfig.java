package com.kamegatze.file.manager.configuration.security;

import com.kamegatze.authorization.remote.security.filter.JwtRemoteFilter;
import com.kamegatze.authorization.remote.security.provider.JwtRemoteAuthenticationProvider;
import com.kamegatze.file.manager.configuration.security.details.UsersServiceDetails;
import com.kamegatze.file.manager.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.Filter;
import org.springframework.web.client.RestTemplate;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final UsersService usersService;
    private final AuthenticationConfiguration authenticationConfiguration;
    @Value("${service.authentication.is-authentication.url}")
    private String urlIsAuthentication;
    private Filter jwtRemoteFilter() throws Exception {
        return new JwtRemoteFilter(
            authenticationConfiguration.getAuthenticationManager(),
            new RestTemplate(),
                urlIsAuthentication
        );
    }
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        UsersServiceDetails usersServiceDetails = new UsersServiceDetails();
        usersServiceDetails.setUsersService(usersService);
        authenticationManagerBuilder.authenticationProvider(new JwtRemoteAuthenticationProvider(usersServiceDetails));
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .addFilterAt(jwtRemoteFilter(), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(authorization ->
                    authorization.requestMatchers("/api/**")
                        .hasAnyAuthority(EAuthority.AUTHORITY_READ.name(), EAuthority.AUTHORITY_WRITE.name())
                        .anyRequest()
                        .authenticated()
                )
                .build();
    }

}
