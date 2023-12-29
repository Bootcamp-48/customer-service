package com.bootcamp.customerservice.webclient;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface BankAccountWebClient {

    Mono<Double> getSavingAccountBalance(String customerId);

    Mono<Boolean> hasCurrentAccount(String customerId);
}