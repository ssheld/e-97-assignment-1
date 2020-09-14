package com.cscie97.ledger.test;

import com.cscie97.ledger.CommandProcessor;

/**
 * Author: Stephen Sheldon
 **/
public class TestDriver {

    public static void main(String[] args) {

        // Pass our command process the specified file name
        CommandProcessor.processCommandFile(args[0]);

    }
}
