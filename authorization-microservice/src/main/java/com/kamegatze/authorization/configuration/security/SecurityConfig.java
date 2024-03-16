package com.kamegatze.authorization.configuration.security;

import com.kamegatze.authorization.configuration.security.http.entry.point.ExceptionEntryPoint;
import com.kamegatze.authorization.model.EAuthority;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final ExceptionEntryPoint exceptionEntryPoint;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtDecoder jwtDecoder;

    private Filter bearerTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        BearerTokenAuthenticationFilter authenticationFilter = new BearerTokenAuthenticationFilter(authenticationManager);
        authenticationFilter.setAuthenticationEntryPoint(exceptionEntryPoint);
        return authenticationFilter;
    }

    private DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private AuthenticationManager authenticationManagerWithJwtAuthenticationProvider(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(new JwtAuthenticationProvider(jwtDecoder));
        return authenticationManagerBuilder.build();
    }


    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authority");
        return jwtGrantedAuthoritiesConverter;
    }


    private JwtAuthenticationConverter customJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilter(bearerTokenAuthenticationFilter(authenticationManagerWithJwtAuthenticationProvider(http)))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(exceptionEntryPoint))
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/api/auth/service/**").permitAll()
                                .requestMatchers("/test", "/api/authentication/micro-service/**")
                                .hasAnyAuthority(EAuthority.AUTHORITY_READ.name(), EAuthority.AUTHORITY_WRITE.name())
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(
                                jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(
                                        customJwtAuthenticationConverter())
                        )
                )
                .authenticationProvider(authenticationProvider())
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

