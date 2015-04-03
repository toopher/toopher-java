package com.toopher;

public class ResponseMock {
    private int statusCode;
    private String jsonResponse;

    public ResponseMock(int statusCode, String jsonResponse) {
        this.statusCode = statusCode;
        this.jsonResponse = jsonResponse;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return jsonResponse;
    }
}
