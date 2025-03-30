package org.kamegatze.gateway.service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestClient restClientAuthorization(RestTemplate restTemplate) {
        return RestClient.builder(restTemplate)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    var messageBody = new String(response.getBody().readAllBytes());
                    throw HttpClientErrorException.create(String.format("Error authorization, body: %s", messageBody), response.getStatusCode(), response.getStatusText(), request.getHeaders(), messageBody.getBytes(), Charset.defaultCharset());
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    var messageBody = new String(response.getBody().readAllBytes());
                    throw HttpServerErrorException.create(String.format("Error authorization, body: %s", messageBody), response.getStatusCode(), response.getStatusText(), request.getHeaders(), messageBody.getBytes(), Charset.defaultCharset());
                })
                .build();
    }

}
