package com.cscie97.ledger;

/**
 * Author: Stephen Sheldon
 **/
public class Transaction {

    private String transactionId;

    private Integer amount;

    private Integer fee;

    private String payload;

    private Account receiver;

    private Account payer;

    public Transaction() {
        // Create transaction ID using uuid

    }

    // Creating a transaction should check to make sure it's valid (uint)



}
