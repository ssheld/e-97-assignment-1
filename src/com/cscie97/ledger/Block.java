package com.cscie97.ledger;

import java.util.*;

/**
 * Author: Stephen Sheldon
 **/
public class Block implements Cloneable {

    private Integer blockNumber;

    // Each block contains a header that includes the hash of
    // the previous block using the sha-256 algorithm to compute the hash based on
    // a merkle tree of the contained transactions
    private String previousHash;

    private String hash;

    private Block previousBlock;

    // Max of 10 transactions
    private List<Transaction> transactionList;

    // Each block contains a map of all accounts and their balances which reflect
    // any transactions contained within the block
    private  Map<String, Account> accountBalanceMap;

    // Merkle tree to store transaction hashes of block
    private MerkleTree merkleTree;

    public Block(Integer blockNumber) {

        this.blockNumber = blockNumber;

        // Create a new transaction list when we create a new block
        transactionList = new ArrayList<>();

        // Create new account balance map when we create a new block
        accountBalanceMap = new TreeMap<>();
    }

    // This constructor is used when creating a new block and copying over the previous
    // blocks accountBalanceMap. By doing it through the constructor versus a setter method
    // we have made it immutable once the block is created.
    public Block(Integer blockNumber, Map<String, Account> accountBalanceMap) {

        this.blockNumber = blockNumber;

        // Create a new transaction list when we create a new block
        transactionList = new ArrayList<>();

        this.accountBalanceMap = accountBalanceMap;
    }

    public void addAccount(Account account) {
        // Check if account exist
        accountBalanceMap.put(account.getAddress(), account);
    }

    private void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
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

    private void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    private void setAccountBalanceMap(Map<String, Account> accountBalanceMap) {
        this.accountBalanceMap = accountBalanceMap;
    }

    public void setMerkleTree(MerkleTree merkleRoot) {
        this.merkleTree = merkleRoot;
    }

    public Integer getBlockNumber() {
        return blockNumber;
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

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public Map<String, Account> getAccountBalanceMap() {
        return accountBalanceMap;
    }

    public MerkleTree getMerkleTree() {
        return merkleTree;
    }

    // Override clone method from interface
    @Override
    protected Object clone() {
        Block cloned = null;
        try {
            cloned = (Block) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported exception.");
        }

        cloned.setBlockNumber(this.getBlockNumber());
        cloned.setPreviousHash(this.getPreviousHash());
        cloned.setHash(this.getHash());
        cloned.setTransactionList(this.getTransactionList());
        cloned.setAccountBalanceMap(this.getAccountBalanceMap());

        return cloned;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockNumber=" + blockNumber +
                ", previousHash='" + previousHash + '\'' +
                ", hash='" + hash + '\'' +
                ", previousBlock=" + previousBlock +
                ", transactionList=" + transactionList +
                ", accountBalanceMap=" + accountBalanceMap +
                ", merkleTree=" + merkleTree +
                '}';
    }
}
