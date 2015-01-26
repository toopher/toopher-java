package com.toopher;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by graceyim on 1/26/15.
 */
public class TestAuthenticationRequest {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1";

    private String id;
    private String reason;
    private JSONObject terminal;
    private JSONObject action;
    private JSONObject user;
    private JSONObject jsonResponse;

    @Before
    public void setUp() {
        this.id = UUID.randomUUID().toString();
        this.reason = "it is a test";
        this.user = new JSONObject();
        this.user.put("id", UUID.randomUUID().toString());
        this.user.put("name", "userName");
        this.terminal = new JSONObject();
        this.terminal.put("id", UUID.randomUUID().toString());
        this.terminal.put("name", "terminalName");
        this.terminal.put("name_extra", "terminalNameExtra");
        this.terminal.put("user", this.user);
        this.action = new JSONObject();
        this.action.put("id", UUID.randomUUID().toString());
        this.action.put("name", "log in");
        this.jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("pending", true);
        jsonResponse.put("granted", true);
        jsonResponse.put("automated", true);
        jsonResponse.put("reason", reason);
        jsonResponse.put("user", user);
        jsonResponse.put("terminal", terminal);
        jsonResponse.put("action", action);
    }

    @Test
    public void testAuthenticateWithOtp() throws InterruptedException, RequestError {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(jsonResponse);

        JSONObject newJsonResponse = jsonResponse;
        newJsonResponse.remove("reason");
        newJsonResponse.put("reason", "it is a changed test");

        HttpClientMock httpClient = new HttpClientMock(200, newJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        authenticationRequest.authenticate_with_otp(toopherAPI, "testOtp");

        assertEquals(authenticationRequest.id, id);
        assertEquals(authenticationRequest.reason, "it is a changed test");
    }

    @Test
    public void testRefreshFromServer() throws InterruptedException, RequestError {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(jsonResponse);

        JSONObject newJsonResponse = jsonResponse;
        newJsonResponse.remove("pending");
        newJsonResponse.remove("granted");
        jsonResponse.put("pending", false);
        jsonResponse.put("granted", false);

        HttpClientMock httpClient = new HttpClientMock(200, newJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        authenticationRequest.refresh_from_server(toopherAPI);

        assertEquals(authenticationRequest.id, id);
        assertFalse(authenticationRequest.pending);
        assertFalse(authenticationRequest.granted);
    }

    private URI createURI(String url) {
        try {
            return new URL(url).toURI();
        } catch (MalformedURLException e) {
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
