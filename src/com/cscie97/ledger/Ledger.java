package com.cscie97.ledger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author: Stephen Sheldon
 **/

public class Ledger {

    private String name;

    private String description;

    private String seed;

    private final Map<Integer, Block> blockMap;

    private Block currentBlock;

    private MerkleTree merkleTree;

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
        blockMap = new TreeMap<>();

        // Create our merkle tree
        merkleTree = new MerkleTree();

        System.out.println("successfully created new ledger");
    }

    public Account createAccount(String address) throws LedgerException {

        if (currentBlock != null && currentBlock.getAccountBalanceMap().containsKey(address)) {
            // If it does already contain this account then throw an exception
            throw new LedgerException("The account you are trying to create already exist.", "Please use a unique account address when creating a new account.");
        }

        currentBlock.getAccountBalanceMap().put(address, new Account(address));

        return currentBlock.getAccountBalanceMap().get(address);

    }

    public Transaction createTransaction(String transactionId, Integer amount, Integer fee, String note, String payer, String receiver) throws LedgerException {

        // Verify that both the receiver and payer accounts are in our account balance map
        if (!currentBlock.getAccountBalanceMap().containsKey(receiver)) {
            throw new LedgerException("The specified receiver does not have an account in the ledger.", "Please enter a valid receiver.");
        } else if (!currentBlock.getAccountBalanceMap().containsKey(payer)) {
            throw new LedgerException("The specified payer does not have an account in the ledger.", "Please enter a valid payer.");
        }

        return new Transaction(transactionId, amount, fee, note, currentBlock.getAccountBalanceMap().get(receiver), currentBlock.getAccountBalanceMap().get(payer));
    }


    // Modified
    public String processTransaction(Transaction transaction) throws LedgerException {

        int payerBalance, receiverBalance, masterBalance;
        Account payerAccount, receiverAccount, masterAccount;

        // Check to make sure transaction amount and fee are unsigned integers
        if (transaction.getAmount() <= 0 ||
            transaction.getFee() < 0) {
            throw new LedgerException("The transaction amount cannot be zero or negative and the fee cannot be negative.", "Please use positive transaction amounts and non-negative fee amounts.");
        }

        payerBalance = currentBlock.getAccountBalanceMap().get(transaction.getPayer().getAddress()).getBalance();
        receiverBalance = currentBlock.getAccountBalanceMap().get(transaction.getReceiver().getAddress()).getBalance();
        masterBalance = currentBlock.getAccountBalanceMap().get("master").getBalance();

        // Transactions should only be accepted if the paying account has a sufficient
        // balance to cover the amount and the associated transaction fee
        if (payerBalance < (transaction.getFee() + transaction.getAmount())) {
            throw new LedgerException("The payer has insufficient funds for the transaction.", "Please change the transaction amount.");
        }

        payerAccount = currentBlock.getAccountBalanceMap().get(transaction.getPayer().getAddress());
        receiverAccount = currentBlock.getAccountBalanceMap().get(transaction.getReceiver().getAddress());
        masterAccount = currentBlock.getAccountBalanceMap().get("master");

        // If we currently have 9 transactions in our list then we need to
        // add this transaction to the list and commit this block
        if (currentBlock.getTransactionList().size() == 9) {
            currentBlock.getTransactionList().add(transaction);
            currentBlock = incrementCurrentBlock(currentBlock);
        }
        // Case - we have less than 9 transactions so just add it to the current block
        else {
            currentBlock.getTransactionList().add(transaction);
        }

        // SPECIAL CASE WHERE MASTER ACCOUNT IS PAYER
        if (transaction.getPayer().getAddress().equals("master")) {
            masterBalance -= transaction.getAmount();

            // Add transaction amount to receiver account
            receiverBalance += transaction.getAmount();



        } else {

            // Deduct fee (AT LEAST 10 units) from the payers account and transfer to the
            // master account
            payerBalance -= transaction.getFee();

            // Deduct transaction amount from payer account
            payerBalance -= transaction.getAmount();

            masterBalance += transaction.getFee();

            // Add transaction amount to receiver account
            receiverBalance += transaction.getAmount();

            // Debit and credit accounts
            payerAccount.setBalance(payerBalance);

        }

        masterAccount.setBalance(masterBalance);
        receiverAccount.setBalance(receiverBalance);



        return transaction.getTransactionId();
    }


    // Return balance for given account
    public Integer getAccountBalance(String address) throws LedgerException {

        // Check if blockMap is null or empty or doesn't contain the account
        if (blockMap == null ||
                blockMap.isEmpty() ||
                !blockMap.get(blockMap.size()).getAccountBalanceMap().containsKey(address)) {
            throw new LedgerException("The specified account has not been committed to a block.", "Please either create the account if it does not exist or wait until at " +
                    "least 10 transactions to occur before trying again.");
        }

        // Return balance from account
        return blockMap.get(blockMap.size()).getAccountBalanceMap().get(address).getBalance();
    }


    // Return a map of account names and balances if no account is specified
    /*public Map<String, String> getAccountBalances() {

    }*/

    // Return block for the given block number. Include the list of transactions,
    // and also a map of the account balances stored with the block
    public Block getBlock(Integer blockNumber) throws LedgerException {

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
    /*public Transaction getTransaction(String transactionId) {

    }*/


    public void validate() {

    }

    private Block incrementCurrentBlock(Block currentBlock) {

        List<Node> hashList = new ArrayList<>();

        String blockHashString;

        // Create a list of all hashes in our block
        for (int i = 0; i < currentBlock.getTransactionList().size(); i++) {
            hashList.add(i, new Node(currentBlock.getTransactionList().get(i).getTransactionHash()));
        }

        // Set the merkle root node for this block by creating the tree
        currentBlock.setMerkleTree(merkleTree.createTree(hashList));

        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("SHA-256 algorithm not found.");
        }

        // Generate hash for block
        // Case - Genesis block thus no previous hash
        if (currentBlock.getPreviousHash() == null) {
            blockHashString = currentBlock.getMerkleTree().getRoot().getHash() + currentBlock.getAccountBalanceMap().toString() + seed;
        }
        // Case - We have a previous hash
        else {
            blockHashString = currentBlock.getMerkleTree().getRoot().getHash() + currentBlock.getPreviousHash() + currentBlock.getAccountBalanceMap().toString() + seed;
        }

        byte[] hash = digest.digest(blockHashString.getBytes(StandardCharsets.UTF_8));

        currentBlock.setHash(new String(hash, StandardCharsets.UTF_8));

        // Clone current block to new block
        Block block = (Block)currentBlock.clone();

        // Add it to our blockmap
        blockMap.put(block.getBlockNumber(), block);



        // Create new block and assign it to currentBlock and accountBalanceMap
        return new Block(currentBlock.getBlockNumber()+1, currentBlock.getAccountBalanceMap());

    }

}
