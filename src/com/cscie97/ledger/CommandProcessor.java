package com.cscie97.ledger;

import java.io.*;
import java.util.Map;

/**
 * Author: Stephen Sheldon
 **/
public class CommandProcessor {

    private static Ledger ledger = null;

    // NOTE - justify passing String array versus the specified string in documentation

    /**
     * Process a single command. The output of the command is formatted and displayed to stdout.
     * @param command Command to process
     * @param lineNumber The lineNumber in the text file of this command
     * @throws CommandProcessorException Throws on error in Ledger
     */
    public static void processCommand(String[] command, int lineNumber) throws CommandProcessorException {


        try {
            switch (command[0].toLowerCase()) {
                case "create-ledger":
                    ledger = new Ledger(command[1], command[3], command[5]);
                    break;
                case "create-account":
                    if (ledger == null) {
                        throw new LedgerException("create-account", "No ledger has been created.");
                    }
                    ledger.createAccount(command[1]);
                    break;
                case "process-transaction":
                    Transaction transaction = ledger.createTransaction(command[1], Integer.valueOf(command[3]), Integer.valueOf(command[5]), command[7], command[9], command[11]);
                    ledger.processTransaction(transaction);
                    break;
                case "get-account-balance":
                    System.out.println(ledger.getAccountBalance(command[1]));
                    break;
                case "get-account-balances":
                    for (Map.Entry<String, Account> entry : ledger.getAccountBalances().entrySet()) {
                        System.out.println(entry.getValue().toString());
                    }
                    break;
                case "get-block":
                    System.out.println(ledger.getBlock(Integer.valueOf(command[1])).toString());
                    break;
                case "get-transaction":
                    System.out.println(ledger.getTransaction(command[1]).toString());
                    break;
                case "validate":
                    ledger.validate();
                    break;
                case "in-order":
                    ledger.getBlock(1).getMerkleTree().inOrder();
                    break;
            }
        } catch (LedgerException e) {
            throw new CommandProcessorException(e.getAction(), e.getReason(), lineNumber);
        }
    }

    public static void processCommandFile(String file) {

        LineNumberReader lineNumberReader;
        String[] words;

        try {
            lineNumberReader = new LineNumberReader(new FileReader(file));

            String line;

            while ((line = lineNumberReader.readLine()) != null) {
                // Regex to split on space unless we hit quotations marks.
                words = line.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                // Remove residual quotations marks from strings
                for (int i = 0; i < words.length; i++) {
                    words[i] = words[i].replaceAll("\"", "");
                }

                // Check if this is a comment in the script file
                try {
                    if (!words[0].equals("#")) {
                        processCommand(words, lineNumberReader.getLineNumber());
                    }
                } catch (CommandProcessorException c) {
                    System.out.println(c.getCommand() + " " + c.getReason() + " Line number: " + c.getLineNumber());
                }
            }
        } catch (FileNotFoundException f) {
            System.out.println("File not found exception.");
        } catch (IOException i) {
            System.out.println("IO exception");
        }
    }
}
