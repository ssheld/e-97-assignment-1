package com.cscie97.ledger;

/**
 * Author: Stephen Sheldon
 **/
public class LedgerException extends Exception {

    /**
     * Action that was performed.
     */
    private String reason;

    /**
     * Reason for the exception.
     */
    private String action;

    /**
     * Constructor method.
     * @param reason Reason for the exception.
     * @param action Action that was performed.
     */
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
