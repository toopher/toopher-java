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
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1";

    private String id;
    private String name;
    private JSONObject user;
    private String userId;
    private String userName;
    private JSONObject action;
    private String actionId;
    private String actionName;
    private String reason;
    private JSONObject terminal;
    private String terminalId;
    private String terminalName;

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
        this.terminal = new JSONObject();
        this.terminal.put("id", UUID.randomUUID().toString());
        this.terminal.put("name", "terminalName");
        this.terminal.put("user", this.user);
        this.terminalId = this.terminal.getString("id");
        this.terminalName = this.terminal.getString("name");
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
                createURI("https://api.toopher.test/v1"), httpClient);
        Pairing pairing = toopherApi.pair("awkward turtle", "some user");

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
                createURI("https://api.toopher.test/v1"), httpClient);
        Pairing pairing = toopherApi.pairWithQrCode(userName);

        assertEquals(httpClient.getLastCalledMethod(), "POST");
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
                createURI("https://api.toopher.test/v1"), httpClient);
        Pairing pairing = toopherAPI.advanced.pairings.getById(id);

        assertEquals(httpClient.getLastCalledMethod(), "GET");
        assertEquals(pairing.user.id, userId);
        assertEquals(pairing.user.name, userName);
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    @Test
    public void testAdvancedAuthenticationRequestsGetById() throws InterruptedException, RequestError {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("pending", true);
        jsonResponse.put("granted", true);
        jsonResponse.put("automated", false);
        jsonResponse.put("reason", reason);
        jsonResponse.put("terminal", terminal);

        HttpClientMock httpClient = new HttpClientMock(200, jsonResponse.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        AuthenticationRequest authenticationRequest = toopherAPI.advanced.authenticationRequests.getById(id);

        assertEquals(httpClient.getLastCalledMethod(), "GET");
        assertEquals(authenticationRequest.id, id);
        assertEquals(authenticationRequest.reason, reason);
        assertEquals(authenticationRequest.terminal.id, terminalId);
        assertEquals(authenticationRequest.terminal.name, terminalName);
        assertEquals(authenticationRequest.terminal.user.id, userId);
        assertEquals(authenticationRequest.terminal.user.name, userName);
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
        jsonResponse.put("enabled", false);
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("disable_toopher_auth", "false");

        HttpClientMock httpClient = new HttpClientMock(200, jsonResponse.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        User user = toopherAPI.advanced.users.create(name, extras);

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(user.id, id);
        assertEquals(user.name, name);
        assertFalse(user.enabled);
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
        assertFalse(user.enabled);
    }

//    TODO: Need to attach 2 JSON response objects
//    @Test
//    public void testAdvancedUsersGetByName() throws InterruptedException, RequestError {
//        JSONObject jsonResponse = new JSONObject();
//        jsonResponse.put("id", id);
//        jsonResponse.put("name", name);
//        jsonResponse.put("enabled", true);
//        jsonResponse.put("disable_toopher_auth", false);
//        JSONArray jsonArray = new JSONArray();
//        jsonArray.put(jsonResponse);
//
//        HttpClientMock httpClient = new HttpClientMock(200, jsonArray.toString());
//
//        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
//                createURI(DEFAULT_BASE_URL), httpClient);
//        User user = toopherAPI.advanced.users.getByName(name);
//
//        assertEquals(httpClient.getLastCalledMethod(), "GET");
//        assertEquals(user.id, id);
//        assertEquals(user.name, name);
//        assertTrue(user.enabled);
//    }

    @Test
    public void testAdvancedUserTerminalsGetById() throws InterruptedException, RequestError {
        JSONObject response = new JSONObject();
        response.put("id", id);
        response.put("name", name);
        response.put("name_extra", "nameExtra");
        response.put("user", user);

        HttpClientMock httpClient = new HttpClientMock(200, response.toString());

        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        UserTerminal userTerminal = toopherAPI.advanced.userTerminals.getById(id);

        assertEquals(httpClient.getLastCalledMethod(), "GET");
        assertEquals(userTerminal.id, id);
        assertEquals(userTerminal.name, name);
        assertEquals(userTerminal.requesterSpecifiedId, "nameExtra");
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
