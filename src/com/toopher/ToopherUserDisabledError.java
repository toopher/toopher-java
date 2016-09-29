package com.toopher;

/**
 * Error thrown when requester tries to authenticate a user who has disabled Toopher authentication
 * 
 */
public class ToopherUserDisabledError extends ToopherClientError {
    static public final int ERROR_CODE = 704;

    public ToopherUserDisabledError(String message) {
        super(ERROR_CODE, message);
    }

    private static final long serialVersionUID = 1L;
}
