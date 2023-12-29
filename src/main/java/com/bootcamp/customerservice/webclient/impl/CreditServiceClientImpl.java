package com.bootcamp.customerservice.webclient.impl;

import com.bootcamp.customerservice.webclient.CreditServiceClient;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CreditServiceClientImpl implements CreditServiceClient {

    private final WebClient creditServiceWebClient;

    public CreditServiceClientImpl(@Qualifier("creditServiceClient") WebClient creditServiceWebClient) {
        this.creditServiceWebClient = creditServiceWebClient;
    }

    @Override
    public Mono<Boolean> hasCreditCard(String customerId) {
        return creditServiceWebClient.get()
                .uri("/exists-creditcard/{customerId}", customerId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse ->
                        Mono.error(new RuntimeException("Error checking credit card existence")))
                .bodyToMono(Boolean.class);
    }
}
