package com.cscie97.ledger;

import java.io.File;
import java.util.Scanner;

/**
 * Author: Stephen Sheldon
 **/
public class CommandProcessor {

    private static Ledger ledger = null;

    // NOTE - justify passing String array versus the specified string in documentation
    public static void processCommand(String[] command) {


        try {
            switch (command[0].toLowerCase()) {
                case "create-ledger":
                    ledger = new Ledger(command[1], command[3], command[5]);
                    break;
                case "create-account":
                    if (ledger == null) {
                        throw new LedgerException("You're trying to create an account without having a ledger,", "Please create a ledger.");
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
//                    System.out.println(ledger.get)
                    break;
                case "get-block":
                    System.out.println(ledger.getBlock(Integer.valueOf(command[1])).toString());
                    break;
                case "get-transaction":
                    System.out.println(ledger.getTransaction(command[1]).toString());
                    break;
                case "validate":
                    break;
                case "in-order":
                    ledger.getBlock(1).getMerkleTree().inOrder();
                    break;
            }
        } catch (LedgerException e) {
            System.out.println(e.getReason() + " " + e.getAction());
        }
    }

    public static void processCommandFile(String file) {


        Scanner sc;

        String input;

        String[] words;

        // Check if file exists
        try {

            sc = new Scanner(new File(file));

            while (sc.hasNextLine()) {

                // Regex to split on space unless we hit quotations marks.
                words = sc.nextLine().split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                // Remove residual quotations marks from strings
                for (int i = 0; i < words.length; i++) {
                    words[i] = words[i].replaceAll("\"", "");
                }

                // Check if this is a comment in the script file
                if (!words[0].equals("#")) {
                    processCommand(words);
                }
            }

        } catch (Exception e) {
            // File not found
            System.out.println("ERROR");
            System.out.println(e.toString());
        }

    }

}
