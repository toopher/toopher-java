package com.toopher;

/**
 * Created by lindsey on 6/10/14.
 */
public class ResponseMock {

    private int statusCode;
    private String jsonAuthCode;

    public ResponseMock(int statusCode, String authCode){
        this.statusCode = statusCode;
        jsonAuthCode = authCode;
    }

    public int getStatusCode(){
        return statusCode;
    }

    public String getResponseBody(){
        return jsonAuthCode;
    }
}
