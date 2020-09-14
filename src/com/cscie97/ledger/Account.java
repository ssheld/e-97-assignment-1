package com.cscie97.ledger;

/**
 * Author: Stephen Sheldon
 **/
public class Account {

    private String address;

    private Integer balance;

    public Account(String address) {

        this.address = address;

        // Set our default balance to 0 for new accounts
        this.balance = 0;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public String getAddress() {
        return address;
    }

    public Integer getBalance() {
        return balance;
    }
}
