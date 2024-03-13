package com.kamegatze.file.manager.configuration.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "service.authentication.is-authentication")
public class ConfigurationRemoteServer {
    private String url;
    private String protocol;
    private String port;
    private String host;

    public String getUrl() {
        return String.format("%s://%s:%s/%s", protocol, host, port, url);
    }
}
