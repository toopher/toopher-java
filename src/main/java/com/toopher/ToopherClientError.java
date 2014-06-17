package com.toopher;

import org.apache.http.client.ClientProtocolException;

/**
 * Wrapper for Toopher extended API error data
 * 
 */
public class ToopherClientError extends RequestError {
    final int toopherErrorCode;
    public int getToopherErrorCode() {
        return toopherErrorCode;
    }

    public ToopherClientError(int toopherErrorCode, String toopherErrorMessage) {
        super(toopherErrorMessage);
        this.toopherErrorCode = toopherErrorCode;
    }

    public ToopherClientError(int toopherErrorCode, String toopherErrorMessage, ClientProtocolException e) {
        super(toopherErrorMessage, e);
        this.toopherErrorCode = toopherErrorCode;
    }

    private static final long serialVersionUID = 1L;
}
