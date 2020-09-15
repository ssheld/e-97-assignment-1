package com.cscie97.ledger;

/**
 * Author: Stephen Sheldon
 **/
public class Account {

    /**
     * Unique identifier for the account, assigned upon creation
     * of account instance.
     */
    private String address;

    /**
     * Balance of the account which reflects the total transfers to
     * and from the account, including fees for transactions where
     * the account is the payer.
      */
    private Integer balance;

    /**
     * Constructor for account class.
     * @param address The unique identifier for the account.
     */
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

    @Override
    public String toString() {
        return address + balance.toString();
    }
}
