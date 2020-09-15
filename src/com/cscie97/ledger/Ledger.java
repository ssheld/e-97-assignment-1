package com.cscie97.ledger;

import java.lang.reflect.Array;
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
        Block genesisBlock = new Block(1);

        // Set currentBlock reference to genesis block
        currentBlock = genesisBlock;

        // Create master account
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
            throw new LedgerException("The account you are trying to create already exist.", "Please use a unique account address when creating a new account.");
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
            throw new LedgerException("The specified receiver does not have an account in the ledger.", "Please enter a valid receiver.");
        } else if (!currentBlock.getAccountBalanceMap().containsKey(payer)) {
            throw new LedgerException("The specified payer does not have an account in the ledger.", "Please enter a valid payer.");
        }

        // Check to make sure transaction amount and fee are unsigned integers
        if (amount <= 0 || amount > Integer.MAX_VALUE || fee < 10) {
            throw new LedgerException("The transaction amount cannot be zero or negative and the fee cannot be less than 10.", "Please use positive transaction amounts and a fee amount of 10 or greater.");
        }

        // Make sure that the length of our note is under 1024 characters, otherwise throw an exception.
        if (note.length() > 1024) {
            throw new LedgerException("The transaction note is over 1024 characters in length.", "Please shorten the length of the transaction note.");
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
            throw new LedgerException("The payer has insufficient funds for the transaction.", "Please change the transaction amount.");
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

    /**
     *
     * @param blockNumber
     * @return
     * @throws LedgerException
     */
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
            throw new LedgerException("No transaction found with the given transaction ID.", "Please enter a valid transaction ID.");
        }

        return transactionList.stream().filter(t -> t.getTransactionId().equals(transactionId)).findFirst().get();
    }


    public void validate() {

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
        // Case - Genesis block thus no previous hash.
        if (currentBlock.getPreviousHash() == null) {
            blockHashString = currentBlock.getMerkleTree().getRoot().getHash() + currentBlock.getAccountBalanceMap().toString() + seed;
        }
        // Case - We have a previous hash.
        else {
            blockHashString = currentBlock.getMerkleTree().getRoot().getHash() + currentBlock.getPreviousHash() + currentBlock.getAccountBalanceMap().toString() + seed;
        }

        byte[] hash = digest.digest(blockHashString.getBytes(StandardCharsets.UTF_8));

        currentBlock.setHash(new String(hash, StandardCharsets.UTF_8));

        // Clone current block to new block
        Block block = (Block)currentBlock.clone();

        // Add it to our block map
        blockMap.put(block.getBlockNumber(), block);

        // Create new block and assign it to currentBlock and accountBalanceMap
        return new Block(currentBlock.getBlockNumber()+1, currentBlock.getAccountBalanceMap());
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
