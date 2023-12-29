package com.bootcamp.customerservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("creditServiceClient")
    public WebClient creditServiceWebClient(@Value("${creditproduct.service.url}") String creditServiceUrl) {
        return WebClient.builder()
                .baseUrl(creditServiceUrl)
                .build();
    }

    @Bean
    @Qualifier("accountServiceClient")
    public WebClient accountServiceWebClient(@Value("${account.service.url}") String accountServiceUrl) {
        return WebClient.builder()
                .baseUrl(accountServiceUrl)
                .build();
    }
}