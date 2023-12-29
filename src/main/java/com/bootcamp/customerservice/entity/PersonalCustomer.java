package com.bootcamp.customerservice.entity;

import com.bootcamp.customerservice.model.CustomerType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonalCustomer extends Customer {
    private int maximumAccounts;

    public PersonalCustomer() {
        super();
        this.setType(CustomerType.PERSONAL);
        this.maximumAccounts = 1;
    }

}