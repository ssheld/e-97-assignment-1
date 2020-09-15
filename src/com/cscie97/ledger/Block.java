package com.cscie97.ledger;

import java.util.*;

/**
 * Author: Stephen Sheldon
 **/
public class Block implements Cloneable {

    /**
     * A sequentially incrementing block number assigned to the block.
     * The first block or genesis block is assigned a block number of 1.
     */
    private Integer blockNumber;

    /**
     * The hash of the previous block in the blockchain. For the genesis
     * block, this is empty. SHA-256 algorithm and merkle tree are used to compute
     * this hash value.
     */
    private String previousHash;

    /**
     * The hash of the current block is computed based on all the properties and
     * associations of the current block except for this attribute.
     */
    private String hash;

    /**
     * Reference to the preceding Block in the blockchain.
     */
    private Block previousBlock;

    /**
     * An ordered list of transactions that are included in the current block.
     * There should be exactly 10 transactions per block.
     */
    private List<Transaction> transactionList;

    /**
     * The full set of accounts managed by the Ledger. The account balances
     * should reflect the account state after all transactions of the current block
     * have been applied. Each block has it's own immutable copy of this property.
     */
    private Map<String, Account> accountBalanceMap;

    /**
     * The Merkle tree that contains the transaction hashes for this block.
     */
    private MerkleTree merkleTree;

    /**
     * Constructor for Genesis block.
     * @param blockNumber The unique block number for the block.
     */
    public Block(Integer blockNumber) {

        this.blockNumber = blockNumber;

        // Create a new transaction list when we create a new block
        transactionList = new ArrayList<>();

        // Create new account balance map when we create a new block
        accountBalanceMap = new TreeMap<>();

        this.previousBlock = null;
        this.previousHash = null;
    }

    /**
     * This constructor is used when creating a new block and copying over the previous
     * blocks accountBalanceMap and a reference to the previous block.
     * By doing it through the constructor versus a setter method
     * we have made it immutable once the block is created.
     * @param blockNumber        The unique block number for the block.
     * @param accountBalanceMap  The account balance map from the previous block.
     * @param previousBlock      Reference to the previous block in the blockchain.
     */
    public Block(Integer blockNumber, Map<String, Account> accountBalanceMap, Block previousBlock) {

        this.blockNumber = blockNumber;

        // Create a new transaction list when we create a new block
        transactionList = new ArrayList<>();

        this.accountBalanceMap = accountBalanceMap;

        this.previousBlock = previousBlock;
    }

    /**
     * Method to add an account to our accountBalanceMap.
     * @param account The account object we wish to add to our accountBalanceMap.
     */
    public void addAccount(Account account) {
        accountBalanceMap.put(account.getAddress(), account);
    }

    // Private so it's immutable to third parties.
    private void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    // Private so that previous hash is immutable to third parties.
    private void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    private void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    // Private so it's immutable to third parties.
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

    /**
     * Clone method is used for cloning a block. Utilized when we reach 10 transactions
     * and want to do a deep copy of our current block to a block that will be stored
     * in our blockchain.
     * @return Cloned copy of block.
     */
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
        return " blockNumber=" + blockNumber +
               "\n previousHash=" + previousHash  +
               "\n hash=" + hash +
               "\n previousBlock=" + previousBlock +
               "\n transactionList=" + transactionList +
               "\n accountBalanceMap=" + accountBalanceMap +
               "\n merkleTree=" + merkleTree;
    }
}
