package com.toopher;

/**
 * Error thrown when requester tries to authenticate a user who does not have an active pairing with Toopher
 */
public class ToopherUnknownUserError extends ToopherClientError {
    static public final int ERROR_CODE = 705;

    public ToopherUnknownUserError(String message) {
        super(ERROR_CODE, message);
    }

    private static final long serialVersionUID = 1L;
}
