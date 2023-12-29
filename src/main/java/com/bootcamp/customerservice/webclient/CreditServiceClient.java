package com.bootcamp.customerservice.webclient;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface CreditServiceClient {

    Mono<Boolean> hasCreditCard(String customerId);
}
