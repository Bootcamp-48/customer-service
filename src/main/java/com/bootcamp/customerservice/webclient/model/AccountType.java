package com.bootcamp.customerservice.webclient.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Type of the Bank Account
 */
public enum AccountType {

    SAVINGS("SAVINGS"),

    CURRENT("CURRENT"),

    FIXED_TERM("FIXED_TERM");

    private String value;

    AccountType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static AccountType fromValue(String value) {
        for (AccountType b : AccountType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}