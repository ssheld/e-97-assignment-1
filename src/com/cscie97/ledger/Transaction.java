package com.cscie97.ledger;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    private String transactionHash;

    public Transaction(String transactionId, Integer amount, Integer fee, String note, Account receiver, Account payer) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.fee = fee;
        this.note = note;
        this.receiver = receiver;
        this.payer = payer;

        // Compute the hash for this transaction

        // First concatenate our hash string
        String hashString = transactionId + amount.toString() + fee.toString() + note + receiver.toString() + payer.toString();

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("SHA-256 algorithm not found.");
        }
        byte[] hash = digest.digest(hashString.getBytes(StandardCharsets.UTF_8));

        // Convert our byte array to a string and assign to transactionhash
        transactionHash = new String(hash, StandardCharsets.UTF_8);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Integer getAmount() {
        return amount;
    }

    public Integer getFee() {
        return fee;
    }

    public Account getReceiver() {
        return receiver;
    }

    public Account getPayer() {
        return payer;
    }

    public String getTransactionHash() {
        return transactionHash;
    }
}
