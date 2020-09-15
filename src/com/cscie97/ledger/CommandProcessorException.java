package com.cscie97.ledger;

/**
 * Author: Stephen Sheldon
 **/
public class CommandProcessorException extends Throwable {

    /**
     * Command that was performed
     */
    private String command;

    /**
     * Reason for the exception
     */
    private String reason;

    /**
     * The line number of the command in the input file
     */
    private int lineNumber;

    /**
     * CommandProcessor Exception is returned from the commandProcessor methods in response
     * to an error conditions.
     * @param command     Command that was performed
     * @param reason      Reason for the exception
     * @param lineNumber  The line number of the command in the input file
     */
    public CommandProcessorException(String command, String reason, int lineNumber) {
        this.command = command;
        this.reason = reason;
        this.lineNumber = lineNumber;
    }

    public String getCommand() {
        return command;
    }

    public String getReason() {
        return reason;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
