package com.nexus.docking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        SimpleClientHttpRequestFactory simpleFactory = new SimpleClientHttpRequestFactory();
        simpleFactory.setConnectTimeout(Duration.ofSeconds(5));
        simpleFactory.setReadTimeout(Duration.ofSeconds(30));

        // Wrap with BufferingClientHttpRequestFactory to allow reading the body multiple times during retries
        BufferingClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(simpleFactory);

        List<ClientHttpRequestInterceptor> interceptors = List.of(
                new RetryClientHttpRequestInterceptor()
        );

        return RestClient.builder()
                .requestFactory(factory)
                .requestInterceptors(i -> i.addAll(interceptors))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json");
    }
}
