package com.cscie97.ledger;

/**
 * Author: Stephen Sheldon
 **/
public class Account implements Cloneable {

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

    private void setAddress(String address) {
        this.address = address;
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

    /**
     * Clone method is used for cloning an account. Utilized when we reach 10 transactions
     * and want to do a deep copy of our current accounts so we can copy them to our new block
     * without altering the account balances in the blockchain.
     * @return Cloned copy of account
     */
    @Override
    protected Object clone() {
        Account cloned = null;
        try {
            cloned = (Account) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported exception.");
        }

        cloned.setAddress(this.getAddress());
        cloned.setBalance(this.getBalance());

        return cloned;
    }

    @Override
    public String toString() {
        return address + " " + balance.toString();
    }
}
