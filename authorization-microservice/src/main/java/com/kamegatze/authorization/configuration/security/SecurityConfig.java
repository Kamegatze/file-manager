package com.kamegatze.authorization.configuration.security;

import com.kamegatze.authorization.configuration.security.details.UsersDetailsService;
import com.kamegatze.authorization.configuration.security.http.entry.point.ExceptionEntryPointContainer;
import com.kamegatze.authorization.configuration.security.provider.DaoAuthentication2FAProvider;
import com.kamegatze.authorization.dto.CookieProperties;
import com.kamegatze.authorization.model.EAuthority;
import com.kamegatze.authorization.remote.security.filter.CookieFilter;
import com.kamegatze.authorization.services.MFATokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Collection;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final UsersDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ExceptionEntryPointContainer exceptionEntryPointContainer;
    private final MFATokenService mfaTokenService;
    private final CookieProperties cookieProperties;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtDecoder jwtDecoder;

    @Value("${spring.application.name}")
    private String applicationName;

    private DaoAuthenticationProvider authenticationProvider() {
        DaoAuthentication2FAProvider authenticationProvider = new DaoAuthentication2FAProvider(mfaTokenService);
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());

        JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        jwtAuthenticationProvider.setJwtAuthenticationConverter(customJwtAuthenticationConverter());
        authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);

        return authenticationManagerBuilder.build();
    }

    private CookieFilter cookieFilter(AuthenticationManager authenticationManager) {
        return new CookieFilter(cookieProperties.getName(), authenticationManager, handlerExceptionResolver);
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
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authenticationManager) throws Exception {

        http
                .exceptionHandling(exception -> exception.authenticationEntryPoint(exceptionEntryPointContainer.getExceptionEntryPoint()))
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/api/v1/auth/service/**", String.format("/%s/**", applicationName), "/error", "/csrf").permitAll()
                                .requestMatchers("/api/v1/authentication/micro-service/**", "/api/v1/account/**")
                                .hasAnyAuthority(EAuthority.AUTHORITY_READ.name(), EAuthority.AUTHORITY_WRITE.name())
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer((oauth2) -> {
                            oauth2.jwt(
                                    jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(
                                            customJwtAuthenticationConverter())
                            );
                            oauth2.authenticationEntryPoint(exceptionEntryPointContainer.getExceptionEntryPoint());
                        }
                )
                .addFilterAfter(cookieFilter(authenticationManager), CsrfFilter.class)
//                .authenticationProvider(authenticationProvider())
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository())
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .sessionAuthenticationStrategy((authentication, request, response) -> {}));
        return http.build();
    }
}

