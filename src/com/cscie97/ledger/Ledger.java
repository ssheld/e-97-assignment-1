package com.cscie97.ledger;

import java.util.HashMap;

/**
 * Author: Stephen Sheldon
 **/

public class Ledger {

    private String name;

    private String description;

    private String seed;

    private HashMap<Integer, Block> blockMap;

    private Block currentBlock;

    public Ledger(String name, String description, String seed) throws LedgerException {

        // Set name
        this.name = name;

        // Set description
        this.description = description;

        // Set seed
        this.seed = seed;

        // Create genesis block
        Block genesisBlock = new Block(1);

        // Set currentBlock reference to genesis block
        currentBlock = genesisBlock;

        // Create master account here
        Account account = createAccount("master");

        // Assign master account max currency of 2147483647
        account.setBalance(Integer.MAX_VALUE);

        // Add master account to block
        genesisBlock.addAccount(account);

        // Genesis block is a special case being the first block so
        // set the previousBlock to null
        genesisBlock.setPreviousBlock(null);

        // Genesis block is a special case being the first block so
        // set the previousHash to null
        genesisBlock.setPreviousHash(null);

        // Create our block map
        blockMap = new HashMap<>();

        System.out.println("successfully created new ledger");
    }

    public Account createAccount(String address) throws LedgerException {

        Account account;

        if (currentBlock != null && currentBlock.getAccountBalanceMap().containsKey(address)) {
            System.out.println("THIS ACCOUNT EXISTS " + address);
            // If it does already contain this account then throw an exception
            throw new LedgerException("The account you are trying to create already exist.", "Please use a unique account address when creating a new account.");
        }

        currentBlock.getAccountBalanceMap().put(address, new Account(address));

        return currentBlock.getAccountBalanceMap().get(address);

    }


//    public String processTransaction(Transaction transaction) {
//
//        // Create transaction object with amount, fee, payer and receiver accounts
//
//        // Make sure transaction doesn't haven negative value and is in uint range
//
//        // Transactions should only be accepted if the paying and receiving addresses
//        // are linked to valid accounts
//
//        // Transactions should only be accepted if the paying account has a sufficient
//        // balance to cover the amount and the associated transaction fee
//
//        // IF transaction is accepted then assign it a unique transaction ID (use uuid)
//
//        // Then add transaction to block
//
//        // Deduct fee (AT LEAST 10 units) from the payers account and transfer to the
//        // master account
//
//        // Debt and credit the accounts
//
//
//
//    }


    // Return balance for given account
    public Integer getAccountBalance(String address) throws LedgerException {

        // Check if blockMap is null or empty or doesn't contain the account
        if (blockMap == null ||
                blockMap.isEmpty() ||
                !blockMap.get(blockMap.size()).getAccountBalanceMap().containsKey(address)) {
            throw new LedgerException("The specified account has not been committed to a block.", "Please either create the account if it does not exist or wait until at" +
                    "least 10 transactions to occur before trying again.");
        }

        // Return balance from account
        return blockMap.get(blockMap.size()).getAccountBalanceMap().get(address).getBalance();

    }

    /*
    // Return a map of account names and balances if no account is specified
    public Map<String, String> getAccountBalances() {

    }

    // Return block for the given block number. Include the list of transactions,
    // and also a map of the account balances stored with the block
    public Block getBlock(Integer blockNumber) throws Exception {

        // Verify block number given is greater than zero
        if (blockNumber < 0) {
            throw new LedgerException("The block number you entered isn't positive", "Please enter a positive unsigned integer value");
        }
        // Verify that our block map contains the block number specified
        else if (!blockMap.containsKey(blockNumber)) {
            throw new LedgerException("The ledger doesn't contain a block with the specified block number", "Please enter a valid block number");
        }
        else {
            return blockMap.get(blockNumber);
        }
    }

    // Return the transaction for the given transaction ID
    public Transaction getTransaction(String transactionId) {

    }
    */

    public void validate() {

    }

}
