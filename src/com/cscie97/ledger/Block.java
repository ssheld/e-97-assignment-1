package com.cscie97.ledger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Author: Stephen Sheldon
 **/
public class Block {

    private Integer blockNumber;

    // Each block contains a header that includes the hash of
    // the previous block using the sha-256 algorithm to compute the hash based on
    // a merkle tree of the contained transactions
    private String previousHash;

    private String hash;

    private Block previousBlock;

    // Max of 10 transactions
    private final List<Transaction> transactionList;

    // Each block contains a map of all accounts and their balances which reflect
    // any transactions contained within the block
    private final Map<String, Account> accountBalanceMap;

    public Block(Integer blockNumber) {

        this.blockNumber = blockNumber;

        // Create a new transaction list when we create a new block
        transactionList = new LinkedList<>();

        // Create new account balance map when we create a new block
        accountBalanceMap = new HashMap<>();
    }

    public void addAccount(Account account) {
        // Check if account exist
        accountBalanceMap.put(account.getAddress(), account);
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setPreviousBlock(Block previousBlock) {
        this.previousBlock = previousBlock;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public Block getPreviousBlock() {
        return previousBlock;
    }

    public Map<String, Account> getAccountBalanceMap() {
        return accountBalanceMap;
    }
}
