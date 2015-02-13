package com.toopher;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestAuthenticationRequest {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1/";

    private static String id;
    private static String reason;
    private static JSONObject jsonResponse = new JSONObject();
    private static String userName;
    private static String terminalName;
    private static String terminalNameExtra;

    @BeforeClass
    public static void setUp() {
        id = UUID.randomUUID().toString();
        reason = "it is a test";

        JSONObject user = new JSONObject();
        user.put("id", UUID.randomUUID().toString());
        user.put("name", "userName");
        user.put("toopher_authentication_enabled", true);
        userName = user.getString("name");

        JSONObject terminal = new JSONObject();
        terminal.put("id", UUID.randomUUID().toString());
        terminal.put("name", "terminalName");
        terminal.put("name_extra", "terminalNameExtra");
        terminal.put("user", user);
        terminalName = terminal.getString("name");
        terminalNameExtra = terminal.getString("name_extra");

        JSONObject action = new JSONObject();
        action.put("id", UUID.randomUUID().toString());
        action.put("name", "log in");

        jsonResponse.put("id", id);
        jsonResponse.put("pending", true);
        jsonResponse.put("granted", false);
        jsonResponse.put("automated", false);
        jsonResponse.put("reason", reason);
        jsonResponse.put("reason_code", 200);
        jsonResponse.put("user", user);
        jsonResponse.put("terminal", terminal);
        jsonResponse.put("action", action);
    }

    @Test
    public void testGrantWithOtp() throws InterruptedException, RequestError {
        JSONObject newJsonResponse = new JSONObject(jsonResponse, JSONObject.getNames(jsonResponse));
        newJsonResponse.remove("pending");
        newJsonResponse.remove("granted");
        newJsonResponse.put("pending", false);
        newJsonResponse.put("granted", true);


        HttpClientMock httpClient = new HttpClientMock(200, newJsonResponse.toString());
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(jsonResponse, toopherApi);

        assertEquals(id, authenticationRequest.id);
        assertTrue("Authentication request pending should be True.", authenticationRequest.pending);
        assertFalse("Authentication request granted should be False.", authenticationRequest.granted);

        authenticationRequest.grantWithOtp("123456");

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals(String.format("authentication_requests/%s/otp_auth", authenticationRequest.id), httpClient.getLastCalledEndpoint());
        assertEquals(id, authenticationRequest.id);
        assertFalse("Authentication request pending should be False.", authenticationRequest.pending);
        assertTrue("Authentication request granted should be True.", authenticationRequest.granted);
    }

    @Test
    public void testRefreshFromServer() throws InterruptedException, RequestError {
        JSONObject newJsonResponse = new JSONObject();
        newJsonResponse.put("pending", true);
        newJsonResponse.put("granted", false);
        newJsonResponse.put("automated", false);
        newJsonResponse.put("reason", "it is a test CHANGED");
        newJsonResponse.put("reason_code", 200);
        JSONObject newUser = new JSONObject();
        newUser.put("name", "userNameChanged");
        newUser.put("toopher_authentication_enabled", true);
        newJsonResponse.put("user", newUser);
        JSONObject newTerminal = new JSONObject();
        newTerminal.put("name", "terminalNameChanged");
        newTerminal.put("name_extra", "terminalNameExtraChanged");
        newTerminal.put("user", newUser);
        newJsonResponse.put("terminal", newTerminal);
        JSONObject newAction = new JSONObject();
        newAction.put("name", "log in");
        newJsonResponse.put("action", newAction);

        HttpClientMock httpClient = new HttpClientMock(200, newJsonResponse.toString());
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(jsonResponse, toopherApi);

        assertEquals(userName, authenticationRequest.user.name);
        assertEquals(reason, authenticationRequest.reason);
        assertEquals(terminalName, authenticationRequest.terminal.name);
        assertEquals(terminalNameExtra, authenticationRequest.terminal.requesterSpecifiedId);

        authenticationRequest.refreshFromServer();

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(String.format("authentication_requests/%s", authenticationRequest.id), httpClient.getLastCalledEndpoint());
        assertEquals(id, authenticationRequest.id);
        assertEquals("it is a test CHANGED", authenticationRequest.reason);
        assertEquals("userNameChanged", authenticationRequest.user.name);
        assertEquals("terminalNameChanged", authenticationRequest.terminal.name);
        assertEquals("terminalNameExtraChanged", authenticationRequest.terminal.requesterSpecifiedId);
    }
}
