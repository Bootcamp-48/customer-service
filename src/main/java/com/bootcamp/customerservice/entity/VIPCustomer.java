package com.bootcamp.customerservice.entity;

import com.bootcamp.customerservice.model.CustomerType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VIPCustomer extends PersonalCustomer {

    public static final double MINIMUM_SAVINGS_AMOUNT = 500;

    private boolean hasCreditCard;

    public VIPCustomer() {
        super();
        this.setType(CustomerType.VIP);
    }

}