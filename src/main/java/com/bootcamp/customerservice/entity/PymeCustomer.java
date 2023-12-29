package com.bootcamp.customerservice.entity;

import com.bootcamp.customerservice.model.CustomerType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PymeCustomer extends BusinessCustomer{
    private boolean hasCurrentAccount;
    private boolean hasCreditCard;

    public PymeCustomer() {
        super();
        this.setType(CustomerType.PYME);
        this.hasCurrentAccount = false;
        this.hasCreditCard = false;
    }
}
