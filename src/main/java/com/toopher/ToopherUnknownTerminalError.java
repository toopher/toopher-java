package com.toopher;

/**
 * Error thrown when requester references a terminal that is unknown to Toopher
 * 
 */
public class ToopherUnknownTerminalError extends ToopherClientError {
    static public final int ERROR_CODE = 706;

    public ToopherUnknownTerminalError(String message) {
        super(ERROR_CODE, message);
    }

    private static final long serialVersionUID = 1L;
}
