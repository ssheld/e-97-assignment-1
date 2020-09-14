package com.cscie97.ledger;

/**
 * Author: Stephen Sheldon
 **/
public class LedgerException extends Exception {

    private String reason;

    private String action;

    public LedgerException(String reason, String action) {
        this.reason = reason;
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public String getAction() {
        return action;
    }
}
