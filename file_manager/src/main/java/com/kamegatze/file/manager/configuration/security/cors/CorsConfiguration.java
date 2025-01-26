package com.kamegatze.file.manager.configuration.security.cors;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {
    @Bean
    public WebMvcConfigurer corsConfig() {

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings( CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowCredentials(true)
                        .allowedOrigins("http://192.168.0.100:4200/", "http://192.168.0.100:8080/, http://localhost:4200/, http://localhost:8080/, http://desktop-vao5je6:4200, http://desktop-vao5je6:8080")
                        .allowedHeaders(
                                HttpHeaders.CONTENT_TYPE,
                                HttpHeaders.ACCEPT
                        )
                        .allowedMethods(
                                HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(),
                                HttpMethod.OPTIONS.name(), HttpMethod.PATCH.name(), HttpMethod.HEAD.name(), HttpMethod.TRACE.name()
                        )
                        .maxAge(3600L);
            }
        };
    }
}
