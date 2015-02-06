package com.toopher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

public class TestToopherAPI {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1/";

    private static String id;
    private static JSONObject user;
    private static String userId;
    private static String userName;
    private static JSONObject action;
    private static String actionId;
    private static String actionName;
    private static JSONObject terminal;
    private static String terminalId;
    private static String terminalName;
    private static String terminalNameExtra;

    private static JSONObject pairingJsonResponse;
    private static JSONObject authenticationJsonResponse;
    private static JSONObject userJsonResponse;
    private static JSONObject userTerminalJsonResponse;

    @BeforeClass
    public static void setUpBeforeClass() {
        id = UUID.randomUUID().toString();

        user = new JSONObject();
        userId = UUID.randomUUID().toString();
        userName = "userName";
        user.put("id", userId);
        user.put("name", userName);

        action = new JSONObject();
        actionId = UUID.randomUUID().toString();
        actionName = "log in";
        action.put("id", actionId);
        action.put("name", actionName);

        terminal = new JSONObject();
        terminalId = UUID.randomUUID().toString();
        terminalName = "terminalName";
        terminalNameExtra = "terminalNameExtra";
        terminal.put("id", terminalId);
        terminal.put("name", terminalName);
        terminal.put("name_extra", terminalNameExtra);
        terminal.put("user", user);
    }

    @Before
    public void setUp() {
        this.pairingJsonResponse = new JSONObject();
        pairingJsonResponse.put("id", id);
        pairingJsonResponse.put("enabled", true);
        pairingJsonResponse.put("pending", false);
        pairingJsonResponse.put("user", user);

        this.authenticationJsonResponse = new JSONObject();
        authenticationJsonResponse.put("id", id);
        authenticationJsonResponse.put("pending", false);
        authenticationJsonResponse.put("granted", false);
        authenticationJsonResponse.put("automated", false);
        authenticationJsonResponse.put("reason", "it is a test");
        authenticationJsonResponse.put("reason_code", 111);
        authenticationJsonResponse.put("terminal", terminal);
        authenticationJsonResponse.put("action", action);
        authenticationJsonResponse.put("user", user);

        this.userJsonResponse = new JSONObject();
        userJsonResponse.put("id", userId);
        userJsonResponse.put("name", userName);
        userJsonResponse.put("disable_toopher_auth", false);

        this.userTerminalJsonResponse = new JSONObject();
        userTerminalJsonResponse.put("id", terminalId);
        userTerminalJsonResponse.put("name", terminalName);
        userTerminalJsonResponse.put("name_extra", terminalNameExtra);
        userTerminalJsonResponse.put("user", user);
    }

    @Test
    public void testCreatePairingWithPairingPhrase() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, pairingJsonResponse.toString());
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = toopherApi.pair(userName, "awkward turtle");

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("pairings/create", httpClient.getLastCalledEndpoint());
        assertEquals("awkward turtle", httpClient.getLastCalledData("pairing_phrase"));
        assertEquals(userName, httpClient.getLastCalledData("user_name"));
        assertEquals(id, pairing.id);
        assertEquals(userId, pairing.user.id);
    }

    @Test
    public void testCreateQrPairing() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, pairingJsonResponse.toString());
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = toopherApi.pair(userName);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("pairings/create/qr", httpClient.getLastCalledEndpoint());
        assertEquals(userName, httpClient.getLastCalledData("user_name"));
        assertEquals(id, pairing.id);
        assertEquals(userId, pairing.user.id);
    }

    @Test
    public void testCreateSmsPairing() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, pairingJsonResponse.toString());

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = toopherApi.pair(userName, "123456");

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("pairings/create/sms", httpClient.getLastCalledEndpoint());
        assertEquals("123456", httpClient.getLastCalledData("phone_number"));
        assertEquals(userName, httpClient.getLastCalledData("user_name"));
        assertEquals(id, pairing.id);
        assertEquals(userId, pairing.user.id);
    }

    @Test
    public void testAdvancedPairingsGetById() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, pairingJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = toopherAPI.advanced.pairings.getById(id);

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(String.format("pairings/%s", pairing.id), httpClient.getLastCalledEndpoint());
        assertEquals(id, pairing.id);
        assertEquals(userId, pairing.user.id);
    }

    @Test
    public void testCreateAuthenticationRequestWithPairingId() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, authenticationJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        AuthenticationRequest authenticationRequest = toopherAPI.authenticate(id, terminalName, actionName);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("authentication_requests/initiate", httpClient.getLastCalledEndpoint());
        assertEquals(id, httpClient.getLastCalledData("pairing_id"));
        assertEquals(terminalName, httpClient.getLastCalledData("terminal_name"));
        assertEquals(id, authenticationRequest.id);
        assertEquals(terminalId, authenticationRequest.terminal.id);
        assertEquals(actionId, authenticationRequest.action.id);
        assertEquals(userId, authenticationRequest.user.id);
    }

    @Test
    public void testCreateAuthenticationRequestWithUsername() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, authenticationJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        AuthenticationRequest authenticationRequest = toopherAPI.authenticate(userName, terminalNameExtra, actionName);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("authentication_requests/initiate", httpClient.getLastCalledEndpoint());
        assertEquals(userName, httpClient.getLastCalledData("user_name"));
        assertEquals(terminalNameExtra, httpClient.getLastCalledData("terminal_name_extra"));
        assertEquals(id, authenticationRequest.id);
        assertEquals(terminalId, authenticationRequest.terminal.id);
        assertEquals(actionId, authenticationRequest.action.id);
        assertEquals(userId, authenticationRequest.user.id);
    }

    @Test
    public void testAdvancedAuthenticationRequestsGetById() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, authenticationJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        AuthenticationRequest authenticationRequest = toopherAPI.advanced.authenticationRequests.getById(id);

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(String.format("authentication_requests/%s", authenticationRequest.id), httpClient.getLastCalledEndpoint());
        assertEquals(id, authenticationRequest.id);
        assertEquals(terminalId, authenticationRequest.terminal.id);
        assertEquals(actionId, authenticationRequest.action.id);
        assertEquals(userId, authenticationRequest.terminal.user.id);
    }

    @Test
    public void testAdvancedUsersCreate() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, userJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        User user = toopherAPI.advanced.users.create(userName);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("users/create", httpClient.getLastCalledEndpoint());
        assertEquals(userName, httpClient.getLastCalledData("name"));
        assertEquals(userId, user.id);
        assertEquals(userName, user.name);
        assertTrue("Toopher authentication should be enabled for user.", user.toopherAuthenticationEnabled);
    }

    @Test
    public void testAdvancedUsersCreateWithExtras() throws InterruptedException, RequestError {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("foo", "bar");

        HttpClientMock httpClient = new HttpClientMock(200, userJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        User user = toopherAPI.advanced.users.create(userName, extras);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("users/create", httpClient.getLastCalledEndpoint());
        assertEquals("bar", httpClient.getLastCalledData("foo"));
        assertEquals(userId, user.id);
        assertEquals(userName, user.name);
        assertTrue("Toopher authentication should be enabled for user.", user.toopherAuthenticationEnabled);
    }

    @Test
    public void testAdvancedUsersGetById() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, userJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        User user = toopherAPI.advanced.users.getById(userId);

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(String.format("users/%s", userId), httpClient.getLastCalledEndpoint());
        assertEquals(userId, user.id);
        assertEquals(userName, user.name);
        assertTrue("Toopher authentication should be enabled for user.", user.toopherAuthenticationEnabled);
    }

    @Test
    public void testAdvancedUsersGetByName() throws InterruptedException, RequestError {
        JSONArray usersJsonArray = new JSONArray();
        usersJsonArray.put(userJsonResponse);

        Map<URI, ResponseMock> expectedUriResponses = new HashMap<URI, ResponseMock>();
        expectedUriResponses.put(createURI(String.format("https://api.toopher.test/v1/users?name=%s", userName)), new ResponseMock(200, usersJsonArray.toString()));
        expectedUriResponses.put(createURI(String.format("https://api.toopher.test/v1/users/%s", userId)), new ResponseMock(200, userJsonResponse.toString()));

        HttpClientMock httpClient = new HttpClientMock(expectedUriResponses);
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        User user = toopherAPI.advanced.users.getByName(userName);

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(String.format("users/%s", userId), httpClient.getLastCalledEndpoint());
        assertEquals(userId, user.id);
        assertEquals(userName, user.name);
        assertTrue("Toopher authentication should be enabled for user.", user.toopherAuthenticationEnabled);
    }

    @Test
    public void testAdvancedUserTerminalsCreate() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, userTerminalJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        UserTerminal userTerminal = toopherAPI.advanced.userTerminals.create(userName, terminalName, terminalNameExtra);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("user_terminals/create", httpClient.getLastCalledEndpoint());
        assertEquals(userName, httpClient.getLastCalledData("user_name"));
        assertEquals(terminalName, httpClient.getLastCalledData("name"));
        assertEquals(terminalNameExtra, httpClient.getLastCalledData("name_extra"));
        assertEquals(terminalId, userTerminal.id);
    }

    @Test
    public void testAdvancedUserTerminalsCreateWithExtras() throws InterruptedException, RequestError {
        Map <String, String> extras = new HashMap<String, String>();
        extras.put("foo", "bar");

        HttpClientMock httpClient = new HttpClientMock(200, userTerminalJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        UserTerminal userTerminal = toopherAPI.advanced.userTerminals.create(userName, terminalName, terminalNameExtra, extras);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("user_terminals/create", httpClient.getLastCalledEndpoint());
        assertEquals("bar", httpClient.getLastCalledData("foo"));
        assertEquals(userName, httpClient.getLastCalledData("user_name"));
        assertEquals(terminalName, httpClient.getLastCalledData("name"));
        assertEquals(terminalNameExtra, httpClient.getLastCalledData("name_extra"));
        assertEquals(terminalId, userTerminal.id);
    }

    @Test
    public void testAdvancedUserTerminalsGetById() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, userTerminalJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        UserTerminal userTerminal = toopherAPI.advanced.userTerminals.getById(terminalId);

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(String.format("user_terminals/%s", terminalId), httpClient.getLastCalledEndpoint());
        assertEquals(terminalId, userTerminal.id);
        assertEquals(userId, userTerminal.user.id);
    }

    @Test
    public void testRawGet() throws InterruptedException, RequestError {
        String endpoint = String.format("pairings/%s", id);

        HttpClientMock httpClient = new HttpClientMock(200, pairingJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        toopherAPI.advanced.raw.get(endpoint);

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(endpoint, httpClient.getLastCalledEndpoint());
        assertEquals(pairingJsonResponse.toString(), httpClient.getExpectedResponse());
    }

    @Test
    public void testRawPost() throws InterruptedException, RequestError {
        String endpoint = "users/create";
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", userName));

        HttpClientMock httpClient = new HttpClientMock(200, userJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                createURI(DEFAULT_BASE_URL), httpClient);
        toopherAPI.advanced.raw.post(endpoint, params);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals(endpoint, httpClient.getLastCalledEndpoint());
        assertEquals(userJsonResponse.toString(), httpClient.getExpectedResponse());
    }

    @Test
    public void testBaseURL() {
        boolean isValidURL = createURI((ToopherAPI.getBaseURL())) != null;

        assertNotNull("Base URL is null.", ToopherAPI.getBaseURL());
        assertTrue("Base URL is not valid.", isValidURL);
    }

    @Test
    public void testVersion() {
        assertNotNull("Version is not null.", ToopherAPI.VERSION);
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
