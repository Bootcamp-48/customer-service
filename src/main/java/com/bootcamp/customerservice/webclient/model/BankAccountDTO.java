package com.bootcamp.customerservice.webclient.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


/**
 * BankAccountDTO
 */
@Getter
@Setter
@Data
public class BankAccountDTO   {
    @JsonProperty("id")
    private String id;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("type")
    private AccountType type;

    @JsonProperty("balance")
    private Double balance;
}
