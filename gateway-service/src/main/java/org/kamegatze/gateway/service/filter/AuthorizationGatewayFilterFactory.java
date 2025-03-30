package org.kamegatze.gateway.service.filter;

import org.kamegatze.gateway.service.configuration.properties.AuthorizationProperty;
import org.kamegatze.gateway.service.filter.exceptions.CookieNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Mono;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


@Component
public class AuthorizationGatewayFilterFactory extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

    private final Logger log = LoggerFactory.getLogger(AuthorizationGatewayFilterFactory.class);
    private static final String RESPONSE_BY_ERROR = """
                                    {
                                        "timestamp": "%s",
                                        "path": "%s",
                                        "status": "%s",
                                        "error": "%s",
                                        "stacktrace": "%s"
                                    }
                                    """;

    private final AuthorizationProperty authorizationProperty;
    private final RestClient restClientAuthorization;
    public AuthorizationGatewayFilterFactory(AuthorizationProperty authorizationProperty, RestClient restClientAuthorization) {
        super(NameConfig.class);
        this.authorizationProperty = authorizationProperty;
        this.restClientAuthorization = restClientAuthorization;
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        return (exchange, chain) -> {
            var path = exchange.getRequest().getPath().pathWithinApplication().value();
            if (isSkip(path)) {
                return chain.filter(exchange);
            }

            try {
                var cookieValue = exchange.getRequest().getHeaders().asSingleValueMap().get(HttpHeaders.COOKIE);
                restClientAuthorization.get()
                        .uri(authorizationProperty.getUri())
                        .cookie(getAccessTokenKey(cookieValue), getAccessTokenValue(cookieValue))
                        .cookie(getRefreshTokenKey(cookieValue), getRefreshTokenValue(cookieValue))
                        .retrieve().toBodilessEntity();
            } catch (CookieNotFoundException e) {
                log.error("", e);
                return chain.filter(exchange)
                        .then(Mono.fromRunnable(() -> {
                            var body = String.format(RESPONSE_BY_ERROR, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                                    path,
                                    HttpStatus.INTERNAL_SERVER_ERROR, "Cookie for authorization not found", toStringStackTrace(e));
                            var response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                            response.getHeaders().remove(HttpHeaders.SET_COOKIE);
                            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            var bodyWrap = response.bufferFactory().wrap(body.getBytes());
                            response.getHeaders().setContentLength(body.length());
                            response.writeWith(Mono.just(bodyWrap)).subscribe();
                        }));
            } catch (HttpStatusCodeException e) {
                log.error("", e);
                return chain.filter(exchange)
                        .then(Mono.fromRunnable(() -> {
                            var response = exchange.getResponse();
                            response.setStatusCode(e.getStatusCode());
                            var bodyBates = e.getResponseBodyAsByteArray();
                            var body = response.bufferFactory().wrap(bodyBates);
                            response.getHeaders().setContentLength(bodyBates.length);
                            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            response.getHeaders().setAll(Objects.requireNonNull(e.getResponseHeaders()).asSingleValueMap());
                            response.writeWith(Mono.just(body)).subscribe();
                        }));
            }

            return chain.filter(exchange);
        };
    }

    private boolean isSkip(String path) {
        for (var route : authorizationProperty.getRoutesSkip()) {

            if (route.endsWith("/**") && path.startsWith(route.substring(0, route.length() - 3))) {
                return true;
            }

            if (path.equals(route)) {
                return true;
            }
        }

        return false;
    }

    private String toStringStackTrace(Exception e) {
        var stringWriter = new StringWriter();
        var printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private String getAccessTokenKey(String cookie) {
        try {
            return cookie.split(";")[0].split("=")[0];
        } catch (Exception e) {
            throw new CookieNotFoundException(e);
        }
    }

    private String getAccessTokenValue(String cookie) {
        try {
            return cookie.split(";")[0].split("=")[1];
        } catch (Exception e) {
            throw new CookieNotFoundException(e);
        }
    }

    private String getRefreshTokenKey(String cookie) {
        try {
            return cookie.split(";")[1].split("=")[0];
        } catch (Exception e) {
            throw new CookieNotFoundException(e);
        }
    }

    private String getRefreshTokenValue(String cookie) {
        try {
            return cookie.split(";")[1].split("=")[1];
        } catch (Exception e) {
            throw new CookieNotFoundException(e);
        }
    }
}
