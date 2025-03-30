package org.kamegatze.gateway.service.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
@ConfigurationProperties("authorization")
public class AuthorizationProperty {
    private String uri;
    private List<String> routesSkip;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<String> getRoutesSkip() {
        return routesSkip;
    }

    public void setRoutesSkip(List<String> routesSkip) {
        this.routesSkip = routesSkip;
    }
}
