package com.bootcamp.customerservice.entity;

import com.bootcamp.customerservice.model.CustomerType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BusinessCustomer extends Customer{
    private boolean canHaveSavings;
    private boolean canHaveFixedTerm;

    public BusinessCustomer() {
        super();
        this.setType(CustomerType.BUSINESS);
        this.canHaveSavings = false;
        this.canHaveFixedTerm = false;
    }

}
