package com.kamegatze.authorization.configuration.security;

import com.kamegatze.authorization.configuration.security.http.filter.BearerTokenAuthenticationFilterWithRefreshToken;
import com.kamegatze.authorization.repoitory.AuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersAuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersRepository;
import com.kamegatze.authorization.service.JwtService;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtService jwtService;
    private final JwtIssuerValidator jwtIssuerValidator;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersAuthorityRepository usersAuthorityRepository;
    private final AuthorityRepository authorityRepository;

    private Filter bearerTokenAuthenticationFilterWithRefreshToken() throws Exception {
        return new BearerTokenAuthenticationFilterWithRefreshToken(
                authenticationManager(authenticationConfiguration),
                jwtService,
                jwtIssuerValidator,
                usersRepository,
                usersAuthorityRepository,
                authorityRepository
                );
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(bearerTokenAuthenticationFilterWithRefreshToken(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize ->
                    authorize
                            .requestMatchers("/api/auth/service/**").permitAll()
                            .requestMatchers("/api/users/info/**")
                            .hasAnyAuthority("READ", "WRITE")
                            .anyRequest().authenticated()
                )
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
                .authenticationProvider(authenticationProvider())
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
