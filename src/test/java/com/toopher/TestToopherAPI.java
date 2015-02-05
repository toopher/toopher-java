package com.toopher;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestToopherAPI {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1/";

    private String id;
    private String name;
    private JSONObject user;
    private String userId;
    private String userName;
    private JSONObject action;
    private String actionId;
    private String actionName;
    private String reason;
    private int reasonCode;
    private JSONObject terminal;
    private String terminalId;
    private String terminalName;
    private String terminalNameExtra;

    @Before
    public void setUp() {
        this.id = UUID.randomUUID().toString();
        this.name = "name";
        this.user = new JSONObject();
        this.user.put("id", UUID.randomUUID().toString());
        this.user.put("name", "userName");
        this.userId = this.user.getString("id");
        this.userName = this.user.getString("name");
        this.action = new JSONObject();
        this.action.put("id", UUID.randomUUID().toString());
        this.action.put("name", "log in");
        this.actionId = this.action.getString("id");
        this.actionName = this.action.getString("name");
        this.reason = "it is a test";
        this.reasonCode = 111;
        this.terminal = new JSONObject();
        this.terminal.put("id", UUID.randomUUID().toString());
        this.terminal.put("name", "terminalName");
        this.terminal.put("name_extra", "terminalNameExtra");
        this.terminal.put("user", this.user);
        this.terminalId = this.terminal.getString("id");
        this.terminalName = this.terminal.getString("name");
        this.terminalNameExtra = this.terminal.getString("name_extra");
    }

    @Test
    public void testCreatePairing() throws InterruptedException, RequestError {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("enabled", true);
        jsonResponse.put("pending", true);
        jsonResponse.put("user", user);
        HttpClientMock httpClient = new HttpClientMock(200, jsonResponse.toString());

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = toopherApi.pair("some user", "awkward turtle");

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("pairing_phrase"), "awkward turtle");
        
        assertEquals(pairing.user.id, userId);
        assertEquals(pairing.user.name, userName);
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    @Test
    public void testCreateQrPairing() throws InterruptedException, RequestError {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("enabled", true);
        jsonResponse.put("pending", true);
        jsonResponse.put("user", user);

        HttpClientMock httpClient = new HttpClientMock(200, jsonResponse.toString());

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = toopherApi.pair(userName);

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(pairing.user.id, userId);
        assertEquals(pairing.user.name, userName);
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    @Test
    public void testCreateSmsPairing() throws InterruptedException, RequestError {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("enabled", true);
        jsonResponse.put("pending", true);
        jsonResponse.put("user", user);

        HttpClientMock httpClient = new HttpClientMock(200, jsonResponse.toString());

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = toopherApi.pair(userName, "123456");

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("phone_number"), "123456");

        assertEquals(pairing.user.id, userId);
        assertEquals(pairing.user.name, userName);
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    @Test
    public void testAdvancedPairingsGetById() throws InterruptedException, RequestError {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("enabled", true);
        jsonResponse.put("pending", true);
        jsonResponse.put("user", user);

        HttpClientMock httpClient = new HttpClientMock(200, jsonResponse.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = toopherAPI.advanced.pairings.getById(id);

        assertEquals(httpClient.getLastCalledMethod(), "GET");
        assertEquals(pairing.user.id, userId);
        assertEquals(pairing.user.name, userName);
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    @Test
    public void testCreateAuthenticationRequestWithPairingId() throws InterruptedException, RequestError {
        JSONObject response = new JSONObject();
        response.put("id", id);
        response.put("pending", true);
        response.put("granted", true);
        response.put("automated", false);
        response.put("reason", reason);
        response.put("reason_code", reasonCode);
        response.put("terminal", terminal);
        response.put("action", action);

        HttpClientMock httpClient = new HttpClientMock(200, response.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        AuthenticationRequest authenticationRequest = toopherAPI.authenticate(id, terminalName, actionName);

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(authenticationRequest.id, id);
        assertTrue(authenticationRequest.pending);
        assertTrue(authenticationRequest.granted);
        assertFalse(authenticationRequest.automated);
        assertEquals(authenticationRequest.reason, reason);
        assertEquals(authenticationRequest.reasonCode, reasonCode);
        assertEquals(authenticationRequest.terminal.id, terminalId);
        assertEquals(authenticationRequest.terminal.name, terminalName);
        assertEquals(authenticationRequest.action.id, actionId);
        assertEquals(authenticationRequest.action.name, actionName);
    }

    @Test
    public void testCreateAuthenticationRequestWithUsername() throws InterruptedException, RequestError {
        JSONObject response = new JSONObject();
        response.put("id", id);
        response.put("pending", true);
        response.put("granted", true);
        response.put("automated", false);
        response.put("reason", reason);
        response.put("reason_code", reasonCode);
        response.put("terminal", terminal);
        response.put("action", action);

        HttpClientMock httpClient = new HttpClientMock(200, response.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        AuthenticationRequest authenticationRequest = toopherAPI.authenticate(userName, terminalNameExtra, actionName);

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(authenticationRequest.id, id);
        assertTrue(authenticationRequest.pending);
        assertTrue(authenticationRequest.granted);
        assertFalse(authenticationRequest.automated);
        assertEquals(authenticationRequest.reason, reason);
        assertEquals(authenticationRequest.reasonCode, reasonCode);
        assertEquals(authenticationRequest.terminal.id, terminalId);
        assertEquals(authenticationRequest.terminal.name, terminalName);
        assertEquals(authenticationRequest.action.id, actionId);
        assertEquals(authenticationRequest.action.name, actionName);
    }



    @Test
    public void testAdvancedAuthenticationRequestsGetById() throws InterruptedException, RequestError {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("pending", true);
        jsonResponse.put("granted", true);
        jsonResponse.put("automated", false);
        jsonResponse.put("reason", reason);
        jsonResponse.put("reason_code", reasonCode);
        jsonResponse.put("action", action);
        jsonResponse.put("terminal", terminal);

        HttpClientMock httpClient = new HttpClientMock(200, jsonResponse.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        AuthenticationRequest authenticationRequest = toopherAPI.advanced.authenticationRequests.getById(id);

        assertEquals(httpClient.getLastCalledMethod(), "GET");
        assertEquals(authenticationRequest.id, id);
        assertEquals(authenticationRequest.reason, reason);
        assertEquals(authenticationRequest.reasonCode, reasonCode);
        assertEquals(authenticationRequest.terminal.id, terminalId);
        assertEquals(authenticationRequest.terminal.name, terminalName);
        assertEquals(authenticationRequest.terminal.user.id, userId);
        assertEquals(authenticationRequest.terminal.user.name, userName);
        assertEquals(authenticationRequest.action.id, actionId);
        assertEquals(authenticationRequest.action.name, actionName);
        assertTrue(authenticationRequest.pending);
        assertTrue(authenticationRequest.granted);
        assertFalse(authenticationRequest.automated);
    }

    @Test
    public void testAdvancedUsersCreate() throws InterruptedException, RequestError {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("name", name);

        HttpClientMock httpClient = new HttpClientMock(200, jsonResponse.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        User user = toopherAPI.advanced.users.create(name);

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(user.id, id);
        assertEquals(user.name, name);
    }

    @Test
    public void testAdvancedUsersCreateWithExtras() throws InterruptedException, RequestError {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("name", name);
        jsonResponse.put("disable_toopher_auth", "false");
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("foo", "bar");

        HttpClientMock httpClient = new HttpClientMock(200, jsonResponse.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        User user = toopherAPI.advanced.users.create(name, extras);

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals("bar", httpClient.getLastCalledData("foo"));
        assertEquals(user.id, id);
        assertEquals(user.name, name);
        assertTrue(user.toopherAuthenticationEnabled);
    }

    @Test
    public void testAdvancedUsersGetById() throws InterruptedException, RequestError {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("name", name);
        jsonResponse.put("enabled", true);
        jsonResponse.put("disable_toopher_auth", true);

        HttpClientMock httpClient = new HttpClientMock(200, jsonResponse.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        User user = toopherAPI.advanced.users.getById(id);

        assertEquals(httpClient.getLastCalledMethod(), "GET");
        assertEquals(user.id, id);
        assertEquals(user.name, name);
        assertFalse(user.toopherAuthenticationEnabled);
    }

    @Test
    public void testAdvancedUsersGetByName() throws InterruptedException, RequestError {
        JSONObject usersJsonResponse = new JSONObject();
        usersJsonResponse.put("id", id);
        usersJsonResponse.put("name", name);
        usersJsonResponse.put("enabled", true);
        usersJsonResponse.put("disable_toopher_auth", false);
        JSONArray usersJsonArray = new JSONArray();
        usersJsonArray.put(usersJsonResponse);

        Map<URI, ResponseMock> expectedUriResponses = new HashMap<URI, ResponseMock>();
        expectedUriResponses.put(createURI(String.format("https://api.toopher.test/v1/users/%s", id)), new ResponseMock(200, usersJsonResponse.toString()));
        expectedUriResponses.put(createURI("https://api.toopher.test/v1/users?name=name"), new ResponseMock(200, usersJsonArray.toString()));

        HttpClientMock httpClient = new HttpClientMock(expectedUriResponses);
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        User user = toopherAPI.advanced.users.getByName(name);
        String actualResponse = httpClient.getExpectedResponse();
        String expectedResponse = usersJsonResponse.toString();

        assertEquals(expectedResponse, actualResponse);
        assertEquals(user.id, id);
        assertEquals(user.name, name);
        assertTrue(user.toopherAuthenticationEnabled);
    }

    @Test
    public void testAdvancedUserTerminalsGetById() throws InterruptedException, RequestError {
        JSONObject response = new JSONObject();
        response.put("id", id);
        response.put("name", name);
        response.put("name_extra", terminalNameExtra);
        response.put("user", user);

        HttpClientMock httpClient = new HttpClientMock(200, response.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        UserTerminal userTerminal = toopherAPI.advanced.userTerminals.getById(id);

        assertEquals(httpClient.getLastCalledMethod(), "GET");
        assertEquals(userTerminal.id, id);
        assertEquals(userTerminal.name, name);
        assertEquals(userTerminal.requesterSpecifiedId, terminalNameExtra);
        assertEquals(userTerminal.user.id, userId);
        assertEquals(userTerminal.user.name, userName);
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

    private boolean isValidURL(String url) {
        return createURI(url) != null;
    }

    @Test
    public void testBaseURL() {
        assertNotNull("Base URL is null.", ToopherAPI.getBaseURL());
        assertTrue("Base URL is not valid.", isValidURL(ToopherAPI.getBaseURL()));
    }

    @Test
    public void testVersion() {
        assertNotNull("Version is not null.", ToopherAPI.VERSION);
    }
}
