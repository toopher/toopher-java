package com.toopher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

public class TestToopherApi {
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

    private static JSONObject pairingJsonResponse = new JSONObject();
    private static JSONObject authenticationJsonResponse = new JSONObject();
    private static JSONObject userJsonResponse = new JSONObject();
    private static JSONObject userTerminalJsonResponse = new JSONObject();

    public ToopherApi getToopherApi(HttpClientMock httpClient) {
        return new ToopherApi("key", "secret", createURI(DEFAULT_BASE_URL), httpClient);
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        id = UUID.randomUUID().toString();

        user = new JSONObject();
        userId = UUID.randomUUID().toString();
        userName = "userName";
        user.put("id", userId);
        user.put("name", userName);
        user.put("toopher_authentication_enabled", true);

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
        terminal.put("requester_specified_id", terminalNameExtra);
        terminal.put("user", user);

        pairingJsonResponse.put("id", id);
        pairingJsonResponse.put("enabled", true);
        pairingJsonResponse.put("pending", false);
        pairingJsonResponse.put("user", user);

        authenticationJsonResponse.put("id", id);
        authenticationJsonResponse.put("pending", false);
        authenticationJsonResponse.put("granted", false);
        authenticationJsonResponse.put("automated", false);
        authenticationJsonResponse.put("reason", "it is a test");
        authenticationJsonResponse.put("reason_code", 111);
        authenticationJsonResponse.put("terminal", terminal);
        authenticationJsonResponse.put("action", action);
        authenticationJsonResponse.put("user", user);

        userJsonResponse.put("id", userId);
        userJsonResponse.put("name", userName);
        userJsonResponse.put("toopher_authentication_enabled", true);

        userTerminalJsonResponse.put("id", terminalId);
        userTerminalJsonResponse.put("name", terminalName);
        userTerminalJsonResponse.put("requester_specified_id", terminalNameExtra);
        userTerminalJsonResponse.put("user", user);
    }

    @Test
    public void testCreatePairingWithPairingPhrase() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, pairingJsonResponse.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
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
        ToopherApi toopherApi = getToopherApi(httpClient);
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

        ToopherApi toopherApi = getToopherApi(httpClient);
        Pairing pairing = toopherApi.pair(userName, "555-555-5555");

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("pairings/create/sms", httpClient.getLastCalledEndpoint());
        assertEquals("555-555-5555", httpClient.getLastCalledData("phone_number"));
        assertEquals(userName, httpClient.getLastCalledData("user_name"));
        assertEquals(id, pairing.id);
        assertEquals(userId, pairing.user.id);
    }

    @Test
    public void testAdvancedPairingsGetById() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, pairingJsonResponse.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        Pairing pairing = toopherApi.advanced.pairings.getById(id);

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(String.format("pairings/%s", pairing.id), httpClient.getLastCalledEndpoint());
        assertEquals(id, pairing.id);
        assertEquals(userId, pairing.user.id);
    }

    @Test
    public void testCreateAuthenticationRequestWithPairingId() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, authenticationJsonResponse.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        AuthenticationRequest authenticationRequest = toopherApi.authenticate(id, terminalName, null, actionName);

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
        ToopherApi toopherApi = getToopherApi(httpClient);
        AuthenticationRequest authenticationRequest = toopherApi.authenticate(userName, null, terminalNameExtra, actionName);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("authentication_requests/initiate", httpClient.getLastCalledEndpoint());
        assertEquals(userName, httpClient.getLastCalledData("user_name"));
        assertEquals(terminalNameExtra, httpClient.getLastCalledData("requester_specified_terminal_id"));
        assertEquals(id, authenticationRequest.id);
        assertEquals(terminalId, authenticationRequest.terminal.id);
        assertEquals(actionId, authenticationRequest.action.id);
        assertEquals(userId, authenticationRequest.user.id);
    }

    @Test
    public void testAdvancedAuthenticationRequestsGetById() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, authenticationJsonResponse.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        AuthenticationRequest authenticationRequest = toopherApi.advanced.authenticationRequests.getById(id);

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
        ToopherApi toopherApi = getToopherApi(httpClient);
        User user = toopherApi.advanced.users.create(userName);

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
        ToopherApi toopherApi = getToopherApi(httpClient);
        User user = toopherApi.advanced.users.create(userName, extras);

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
        ToopherApi toopherApi = getToopherApi(httpClient);
        User user = toopherApi.advanced.users.getById(userId);

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

        HttpClientMock httpClient = new HttpClientMock(200, usersJsonArray.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        User user = toopherApi.advanced.users.getByName(userName);

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals("users", httpClient.getLastCalledEndpoint());
        assertEquals(userId, user.id);
        assertEquals(userName, user.name);
        assertTrue("Toopher authentication should be enabled for user.", user.toopherAuthenticationEnabled);
    }

    @Test
    public void testAdvancedUserTerminalsCreate() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, userTerminalJsonResponse.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        UserTerminal userTerminal = toopherApi.advanced.userTerminals.create(userName, terminalName, terminalNameExtra);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("user_terminals/create", httpClient.getLastCalledEndpoint());
        assertEquals(userName, httpClient.getLastCalledData("user_name"));
        assertEquals(terminalName, httpClient.getLastCalledData("name"));
        assertEquals(terminalNameExtra, httpClient.getLastCalledData("requester_specified_id"));
        assertEquals(terminalId, userTerminal.id);
    }

    @Test
    public void testAdvancedUserTerminalsCreateWithExtras() throws InterruptedException, RequestError {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("foo", "bar");

        HttpClientMock httpClient = new HttpClientMock(200, userTerminalJsonResponse.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        UserTerminal userTerminal = toopherApi.advanced.userTerminals.create(userName, terminalName, terminalNameExtra, extras);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("user_terminals/create", httpClient.getLastCalledEndpoint());
        assertEquals("bar", httpClient.getLastCalledData("foo"));
        assertEquals(userName, httpClient.getLastCalledData("user_name"));
        assertEquals(terminalName, httpClient.getLastCalledData("name"));
        assertEquals(terminalNameExtra, httpClient.getLastCalledData("requester_specified_id"));
        assertEquals(terminalId, userTerminal.id);
    }

    @Test
    public void testAdvancedUserTerminalsGetById() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, userTerminalJsonResponse.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        UserTerminal userTerminal = toopherApi.advanced.userTerminals.getById(terminalId);

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(String.format("user_terminals/%s", terminalId), httpClient.getLastCalledEndpoint());
        assertEquals(terminalId, userTerminal.id);
        assertEquals(userId, userTerminal.user.id);
    }

    @Test
    public void testRawGet() throws InterruptedException, RequestError {
        String endpoint = String.format("pairings/%s", id);

        HttpClientMock httpClient = new HttpClientMock(200, pairingJsonResponse.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        toopherApi.advanced.raw.get(endpoint);

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
        ToopherApi toopherApi = getToopherApi(httpClient);
        toopherApi.advanced.raw.post(endpoint, params);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals(endpoint, httpClient.getLastCalledEndpoint());
        assertEquals(userJsonResponse.toString(), httpClient.getExpectedResponse());
    }

    @Test
    public void testDeactivatedPairingRaisesCorrectError() throws InterruptedException, RequestError {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error_code", "707");
        errorJson.put("error_message", "Not allowed: This pairing has been deactivated.");
        HttpClientMock httpClient = new HttpClientMock(409, errorJson.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        try {
            toopherApi.advanced.raw.get("pairings/1");
        } catch (ToopherPairingDeactivatedError e) {
            assertEquals("Not allowed: This pairing has been deactivated.", e.getMessage());
        }
    }

    @Test
    public void testUnknownTerminalRaisesCorrectError() throws InterruptedException, RequestError {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error_code", "706");
        errorJson.put("error_message", "No matching terminal exists");
        HttpClientMock httpClient = new HttpClientMock(409, errorJson.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        try {
            toopherApi.advanced.raw.get("pairings/1");
        } catch (ToopherUnknownTerminalError e) {
            assertEquals("No matching terminal exists", e.getMessage());
        }
    }

    @Test
    public void testUnknownUserRaisesCorrectError() throws InterruptedException, RequestError {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error_code", "705");
        errorJson.put("error_message", "No matching user exists");
        HttpClientMock httpClient = new HttpClientMock(409, errorJson.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        try {
            toopherApi.advanced.raw.get("pairings/1");
        } catch (ToopherUnknownUserError e) {
            assertEquals("No matching user exists", e.getMessage());
        }
    }

    @Test
    public void testUserDisabledRaisesCorrectError() throws InterruptedException, RequestError {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error_code", "704");
        errorJson.put("error_message", "The specified user has disabled Toopher authentication.");
        HttpClientMock httpClient = new HttpClientMock(409, errorJson.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        try {
            toopherApi.advanced.raw.get("pairings/1");
        } catch (ToopherUserDisabledError e) {
            assertEquals("The specified user has disabled Toopher authentication.", e.getMessage());
        }
    }

    @Test
    public void testClientErrorRaisedForOtherErrorCodes() throws InterruptedException, RequestError {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error_code", "708");
        errorJson.put("error_message", "User requires OTP authentication");
        HttpClientMock httpClient = new HttpClientMock(409, errorJson.toString());
        ToopherApi toopherApi = getToopherApi(httpClient);
        try {
            toopherApi.advanced.raw.get("pairings/1");
        } catch (ToopherClientError e) {
            assertEquals("User requires OTP authentication", e.getMessage());
        }
    }

    @Test
    public void testBaseURL() {
        boolean isValidURL = createURI((ToopherApi.getBaseURL())) != null;

        assertNotNull("Base URL is null.", ToopherApi.getBaseURL());
        assertTrue("Base URL is not valid.", isValidURL);
    }

    @Test
    public void testVersion() {
        assertNotNull("Version is not null.", ToopherApi.VERSION);
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
