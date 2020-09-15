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

    /**
     * Name of the ledger.
     */
    private String name;

    /**
     * Ledger description.
     */
    private String description;

    /**
     * The seed that is used as input to the hashing algorithm.
     */
    private String seed;

    /**
     * A map of block numbers and the associated block.
     */
    private final Map<Integer, Block> blockMap;

    /**
     * The initial block of the blockchain.
      */
    private Block genesisBlock;

    /**
     * The current block of the blockchain that HAS NOT been committed to
     * our blockMap yet.
     */
    private Block currentBlock;

    /**
     * The Merkle tree that will be used for computing the transaction hashes of
     * our current block once our current block reaches 10 transactions.
     */
    private MerkleTree merkleTree;

    /**
     * Constructor for ledger class
     * @param name         Name of our ledger.
     * @param description  Ledger description.
     * @param seed         The seed that is used as input to the hashing algorithm.
     * @throws LedgerException Exception generated from various methods.
     */
    public Ledger(String name, String description, String seed) throws LedgerException {

        this.name = name;
        this.description = description;
        this.seed = seed;

        // Create genesis block with default blocker number of 1
        genesisBlock = new Block(1);

        // Set currentBlock reference to genesis block
        currentBlock = genesisBlock;

        // Create master account
        Account account = createAccount("master");

        // Assign master account max currency of 2147483647
        account.setBalance(Integer.MAX_VALUE);

        // Add master account to block
        genesisBlock.addAccount(account);

        // Create our block map
        blockMap = new TreeMap<>();

        // Create our merkle tree
        merkleTree = new MerkleTree();
    }

    /**
     * Create a new account, assign it a unique ID using given address and set balance to 0.
     * @param address The account ID specified by user.
     * @return        The newly created account object.
     * @throws LedgerException If an account already exists with the given ID then throw an exception.
     */
    public Account createAccount(String address) throws LedgerException {

        // Check to see if the given account ID is already present in our accountBalanceMap
        if (currentBlock != null && currentBlock.getAccountBalanceMap().containsKey(address)) {
            // If it does already contain this account then throw an exception
            throw new LedgerException("create-account.", "The account you are trying to create already exist.");
        }

        currentBlock.getAccountBalanceMap().put(address, new Account(address));

        return currentBlock.getAccountBalanceMap().get(address);
    }

    /**
     * This is a helper function for processing transactions in our commandProcessor class. It takes the given
     * transaction parameters from the user and constructs a transaction object for further processing.
     * @param transactionId Unique ID for a given transaction.
     * @param amount        The transaction amount to be deducted from the payer account and added to the receiver's account.
     * @param fee           The fee is taken from the payer account and added to the master account.
     * @param note          An arbitrary string that may be up to 1024 characters in length.
     * @param payer         The account issuing the transaction.
     * @param receiver      The account receiving the amount from the transaction.
     * @return              The newly creation transaction object
     * @throws LedgerException  Throw an exception if either the payer or receiver accounts don't exist in the ledger.
     *                          Throw an exception if the fee is below 10 units, if the amount isn't in the unsigned int range
     *                          or if the note's character limit exceed 1024 characters.
     */
    public Transaction createTransaction(String transactionId, Integer amount, Integer fee, String note, String payer, String receiver) throws LedgerException {

        // Verify that both the receiver and payer accounts are in our account balance map
        if (!currentBlock.getAccountBalanceMap().containsKey(receiver)) {
            throw new LedgerException("create-transaction", "The specified receiver does not have an account in the ledger.");
        } else if (!currentBlock.getAccountBalanceMap().containsKey(payer)) {
            throw new LedgerException("create-transaction", "The specified payer does not have an account in the ledger.");
        }

        // Check to make sure transaction amount and fee are unsigned integers
        if (amount <= 0 || amount > Integer.MAX_VALUE || fee < 10) {
            throw new LedgerException("create-transaction", "The transaction amount cannot be zero or negative and the fee cannot be less than 10.");
        }

        // Make sure that the length of our note is under 1024 characters, otherwise throw an exception.
        if (note.length() > 1024) {
            throw new LedgerException("create-transaction", "The transaction note is over 1024 characters in length.");
        }

        return new Transaction(transactionId, amount, fee, note, currentBlock.getAccountBalanceMap().get(receiver), currentBlock.getAccountBalanceMap().get(payer));
    }

    /**
     * Process a transaction.  Finish validating the transaction and if valid, add it to our current block.
     * @param transaction      The transaction object for the given transaction.
     * @return                 Return the assigned transaction ID.
     * @throws LedgerException Throw an exception if the payer doesn't have sufficient funds.
     */
    public String processTransaction(Transaction transaction) throws LedgerException {

        int payerBalance, receiverBalance, masterBalance;
        Account payerAccount, receiverAccount, masterAccount;

        payerBalance = currentBlock.getAccountBalanceMap().get(transaction.getPayer().getAddress()).getBalance();
        receiverBalance = currentBlock.getAccountBalanceMap().get(transaction.getReceiver().getAddress()).getBalance();
        masterBalance = currentBlock.getAccountBalanceMap().get("master").getBalance();

        // Transactions should only be accepted if the paying account has a sufficient
        // balance to cover the amount and the associated transaction fee.
        if (payerBalance < (transaction.getFee() + transaction.getAmount())) {
            throw new LedgerException("process-transaction", "The payer has insufficient funds for the transaction.");
        }

        payerAccount = currentBlock.getAccountBalanceMap().get(transaction.getPayer().getAddress());
        receiverAccount = currentBlock.getAccountBalanceMap().get(transaction.getReceiver().getAddress());
        masterAccount = currentBlock.getAccountBalanceMap().get("master");

        // Case - If we currently have 9 transactions in our list then we need to
        // add this transaction to the list and commit this block
        if (currentBlock.getTransactionList().size() == 9) {
            currentBlock.getTransactionList().add(transaction);
            currentBlock = incrementCurrentBlock(currentBlock);
        }
        // Case - we have less than 9 transactions so just add it to the current block
        else {
            currentBlock.getTransactionList().add(transaction);
        }

        // Special case where payer account is same as master account
        if (transaction.getPayer().getAddress().equals("master")) {

            masterBalance -= transaction.getAmount();
            receiverBalance += transaction.getAmount();

        } else {

            // Deduct fee from payer account.
            payerBalance -= transaction.getFee();

            // Deduct transaction amount from payer account.
            payerBalance -= transaction.getAmount();

            // Add fee to master account.
            masterBalance += transaction.getFee();

            // Add transaction amount to receiver account.
            receiverBalance += transaction.getAmount();

            // Write the new balances to payer account.
            payerAccount.setBalance(payerBalance);
        }

        // Write the new balances to master and receiver accounts
        masterAccount.setBalance(masterBalance);
        receiverAccount.setBalance(receiverBalance);

        return transaction.getTransactionId();
    }

    /**
     * Return the account balance for the account with a given address based on the most recently completed block.
     * @param address The address of the account number we wish to retrieve the balance of.
     * @return        The balance of the account with the given address.
     * @throws LedgerException  Throw an exception if our blockmap hasn't been initialized, is empty or doesn't contain
     *                          the account with the specified address.
     */
    public Integer getAccountBalance(String address) throws LedgerException {

        // Check if blockMap is null or empty or doesn't contain the account
        if (blockMap == null ||
                blockMap.isEmpty() ||
                !blockMap.get(blockMap.size()).getAccountBalanceMap().containsKey(address)) {
            throw new LedgerException("get-account-balance", "The specified account has not been committed to a block.");
        }

        // Return balance from account
        return blockMap.get(blockMap.size()).getAccountBalanceMap().get(address).getBalance();
    }

    /**
     * Return the account balance map for the most recently completed block.
     * @return account balance map for most recently completed block.
     * @throws LedgerException Throw an exception if the block map is currently empty.
     */
    public Map<String, Account> getAccountBalances() throws LedgerException {

        // If block map is empty throw an exception
        if (blockMap.isEmpty()) {
            throw new LedgerException("get-account-balances", "There are no accounts in our blockchain.");
        }

        // Return the account balance map of the last block written to block map
        return blockMap.get(blockMap.size()).getAccountBalanceMap();
    }

    /**
     * Returns the block for the given block number.
     * @param blockNumber   Block number of the block we wish to retrieve.
     * @return              Block object with corresponding block number.
     * @throws LedgerException Throw an exception if the block number is a negative value or if our block map
     *                         doesn't contain a block with that block number.
     */
    public Block getBlock(Integer blockNumber) throws LedgerException {

        // Verify block number given is greater than zero
        if (blockNumber < 0) {
            throw new LedgerException("get-block", "The block number you entered isn't positive.");
        }
        // Verify that our block map contains the block number specified
        else if (!blockMap.containsKey(blockNumber)) {
            throw new LedgerException("get-block", "The ledger doesn't contain a block with the specified block number.");
        }
        else {
            return blockMap.get(blockNumber);
        }
    }

    /**
     * Look up a transaction with a given transaction ID and return it to the user.
     * @param transactionId     The transaction ID of the transaction we wish to find.
     * @return                  The transaction object with the specified transaction ID.
     * @throws LedgerException  If the current block doesn't contain the transaction ID then throw an exception.
     */
    public Transaction getTransaction(String transactionId) throws LedgerException {

        ArrayList<Transaction> transactionList = compileTransactionList(blockMap);

        // Check our transactionList to see if we have a transaction with the specified ID
        if (transactionList.stream().filter(t -> t.getTransactionId().equals(transactionId)).findFirst().isEmpty()) {
            throw new LedgerException("get-transaction", "No transaction found with the given transaction ID.");
        }

        return transactionList.stream().filter(t -> t.getTransactionId().equals(transactionId)).findFirst().get();
    }

    /**
     * Validate the current state of the blockchain. For each block, check
     * the hash of the previous hash, make sure that the account balances total to the max value.
     * Verify that each completed block has exactly 10 transactions.
     * @throws LedgerException Throw an exception for hash mismatches, total account balance greater than
     *                         Integer.MAX_VALUE or blocks with transaction list sizes that aren't equal to 10.
     */
    public void validate() throws LedgerException {

        // Compare the stored previous hash block with the actual hash of the previous block.
        // Start at a value of 2 because the genesis block doesn't have a previous hash to compare to.
        for (int i = 2; i <= blockMap.size(); i++) {

            // Compare hashes
            if (!blockMap.get(i).getPreviousHash().equals(blockMap.get(i - 1).getHash())) {
                throw new LedgerException("validate", "Hash mismatch.");
            }
        }

        Integer accountsTotal = 0;

        // Sum all account balances
        for (Map.Entry<String, Account> entry : getAccountBalances().entrySet()) {
            accountsTotal += entry.getValue().getBalance();
        }

        // Verify that it's equal to Integer.MAX_VALUE
        if (accountsTotal != Integer.MAX_VALUE) {
            throw new LedgerException("validate", "The sum of all account balances does not equal Integer.MAX_VALUE.");
        }

        for (Map.Entry<Integer, Block> entry : blockMap.entrySet()) {
            if (entry.getValue().getTransactionList().size() != 10) {
                throw new LedgerException("validate", "There exists a block that does not have exactly 10 transactions.");
            }
        }

        System.out.println("Our blockchain is valid.");

    }

    /**
     * Helper method to commit our current block to the ledger once it fills up with transactions and
     * create a new block. Merkle tree creation and necessary hashing takes place here.
     * @param currentBlock The current block that now has 10 transactions.
     * @return             A new block containing our account balance map.
     */
    private Block incrementCurrentBlock(Block currentBlock) {

        List<Node> hashList = new ArrayList<>();

        String blockHashString;

        // Create a list of all hashes in our block.
        for (int i = 0; i < currentBlock.getTransactionList().size(); i++) {
            hashList.add(i, new Node(currentBlock.getTransactionList().get(i).getTransactionHash()));
        }

        // Set the merkle root node for this block by creating the tree.
        currentBlock.setMerkleTree(merkleTree.createTree(hashList));

        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("SHA-256 algorithm not found.");
        }

        // Generate hash for block
        // Case - Genesis block thus no previous hash or previous block.
        if (currentBlock.getPreviousHash() == null) {
            blockHashString = currentBlock.getMerkleTree().getRoot().getHash() + currentBlock.getBlockNumber().toString() +
                    currentBlock.getTransactionList().toString() + currentBlock.getAccountBalanceMap().toString() + seed;
        }
        // Case - We have a previous hash and previous block so include it in our hash string.
        else {
            blockHashString = currentBlock.getMerkleTree().getRoot().getHash() + currentBlock.getBlockNumber().toString() +
                    currentBlock.getPreviousHash() + currentBlock.getTransactionList().toString() + currentBlock.getAccountBalanceMap().toString() +
                    currentBlock.getPreviousBlock().toString() + seed;
        }

        byte[] hash = digest.digest(blockHashString.getBytes(StandardCharsets.UTF_8));

        currentBlock.setHash(new String(hash, StandardCharsets.UTF_8));

        // Clone current block to new block
        Block block = (Block)currentBlock.clone();

        // Add it to our block map
        blockMap.put(block.getBlockNumber(), block);

        // Do a deep copy of our account balance map
        Map<String, Account> abMap = new TreeMap<>();
        for (Map.Entry<String, Account> entry : currentBlock.getAccountBalanceMap().entrySet()) {
            abMap.put(entry.getKey(), (Account)entry.getValue().clone());
        }

        // Create new block
        return new Block(currentBlock.getBlockNumber()+1, abMap, currentBlock, currentBlock.getHash());
    }

    /**
     * Helper method for getTransaction method. This method takes all blocks that have been
     * committed to our ledger's blockmap and concatenates them. This will make it easier to
     * search through our transaction by using the transaction ID.
     * @param bMap The blockMap of our ledger.
     * @return     An ArrayList of all transactions in our blockMap.
     */
    private ArrayList<Transaction> compileTransactionList(Map<Integer, Block> bMap) {

        ArrayList<Transaction> transactionList = new ArrayList<>();

        // Concatenate all transaction in our block map
        for (Map.Entry<Integer, Block> entry : bMap.entrySet()) {
            transactionList.addAll(entry.getValue().getTransactionList());
        }

        return transactionList;
    }
}
