package com.cscie97.ledger;

/**
 * Author: Stephen Sheldon
 **/
public class Transaction {

    private String transactionId;

    private Integer amount;

    private Integer fee;

    private String note;

    private Account receiver;

    private Account payer;

    public Transaction(String transactionId, Integer amount, Integer fee, String note, Account receiver, Account payer) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.fee = fee;
        this.note = note;
        this.receiver = receiver;
        this.payer = payer;
    }

    // Creating a transaction should check to make sure it's valid (uint)



}
