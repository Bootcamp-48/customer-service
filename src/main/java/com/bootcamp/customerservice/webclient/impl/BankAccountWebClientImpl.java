package com.bootcamp.customerservice.webclient.impl;

import com.bootcamp.customerservice.webclient.BankAccountWebClient;
import com.bootcamp.customerservice.webclient.model.AccountType;
import com.bootcamp.customerservice.webclient.model.BankAccountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Service
@Slf4j
public class BankAccountWebClientImpl implements BankAccountWebClient {

   private final WebClient accountServiceWebClient;

    public BankAccountWebClientImpl(@Qualifier("accountServiceClient") WebClient accountServiceWebClient) {
        this.accountServiceWebClient = accountServiceWebClient;
    }


    public Mono<Double> getSavingAccountBalance(String customerId) {
        return accountServiceWebClient.get()
                .uri("/accounts/first-account/customer/{customerId}/type/{accountType}", customerId, AccountType.SAVINGS)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        Mono.error(new RuntimeException("Error retrieving savings account balance")))
                .onStatus(HttpStatus::is5xxServerError, response ->
                        Mono.error(new RuntimeException("Server error occurred while retrieving savings account balance")))
                .bodyToMono(BankAccountDTO.class)
                .map(BankAccountDTO::getBalance)
                .doOnError(error -> log.error("Error retrieving savings account balance for customer {}: {}", customerId, error.getMessage()));
    }

    public Mono<Boolean> hasCurrentAccount(String customerId) {
        return accountServiceWebClient.get()
                .uri("/accounts/customer/{customerId}/type/{accountType}", customerId, AccountType.CURRENT)
                .retrieve()
                .bodyToFlux(BankAccountDTO.class)
                .hasElements()
                .onErrorResume(e -> {
                    log.error("Error checking current account for customer {}", customerId);
                    return Mono.just(false);
                });
    }
}