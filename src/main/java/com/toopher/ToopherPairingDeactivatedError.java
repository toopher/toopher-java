package com.toopher;

/**
 * Error thrown when requester tries to authenticate a pairing that has been deactivated.
 */
public class ToopherPairingDeactivatedError extends ToopherClientError {
    static public final int ERROR_CODE = 707;

    public ToopherPairingDeactivatedError(String message) {
        super(ERROR_CODE, message);
    }

    private static final long serialVersionUID = 1L;
}
