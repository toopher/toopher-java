package com.toopher;

import oauth.signpost.http.HttpResponse;
import org.json.JSONObject;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestToopherAPI {

    private static String AUTH_REQUEST_JSON = "{'id':'1', 'granted':true, 'pending':true,'automated':true, 'reason':'test', 'terminal':{'id':'1','name':'some user'}}".replace("'", "\"");
    private final String BASE_URI_STRING = "https://api.toopher.test/v1/";
    private final String CONSUMER_KEY    = "key";
    private final String CONSUMER_SECRET = "secret";
    private final String PAIRING_ID      = "pairing_id";
    private final String TERMINAL_NAME   = "terminal_name";
    private final String ACTION_NAME     = "action_name";

    @Test
    public void testCreatePairing() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200,
                "{'id':'1','enabled':true,'pending':true,'user':{'id':'1','name':'some user'}}".replace("'", "\""));

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1"), httpClient);
        PairingStatus pairing = toopherApi.pair("awkward turtle", "some user");

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("pairing_phrase"), "awkward turtle");
        
        assertEquals(pairing.userId, "1");
        assertEquals(pairing.userName, "some user");
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    @Test
    public void testGetPairingStatus() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200,
                "{'id':'1','enabled':true,'pending':true,'user':{'id':'1','name':'some user'}}".replace("'", "\""));

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        PairingStatus pairing = toopherApi.getPairingStatus("1");

        assertEquals(httpClient.getLastCalledMethod(), "GET");

        assertEquals(pairing.userId, "1");
        assertEquals(pairing.userName, "some user");
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    @Test
    public void testAuthenticateWithActionName() throws InterruptedException, RequestError {
        String pairingId = "pairing ID";
        String terminalName = "my computer";
        String actionName = "action";
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        try{
            toopherApi.authenticate(pairingId, terminalName, actionName);
        } catch(RequestError re){}
        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("pairing_id"), pairingId);
        assertEquals(httpClient.getLastCalledData("terminal_name"), terminalName);
        assertEquals(httpClient.getLastCalledData("action_name"), actionName);
    }

    @Test
    public void testAuthenticateThrowingRequestError() throws InterruptedException, RequestError {
        String pairingId = "pairing ID";
        String terminalName = "my computer";
        HttpClientMock httpClient = new HttpClientMock(200, "{'id':'1'}");
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        try {
            toopherApi.authenticate(pairingId, terminalName);
        } catch(RequestError re){
            assertTrue(true);
        }
    }

    @Test
    public void testAuthenticateWithTerminalName() throws InterruptedException, RequestError {
        String pairingId = "pairing ID";
        String terminalName = "my computer";
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
            createURI("https://api.toopher.test/v1/"), httpClient);
        toopherApi.authenticate(pairingId, terminalName);
        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("pairing_id"), pairingId);
        assertEquals(httpClient.getLastCalledData("terminal_name"), terminalName);
    }

    @Test
    public void testAuthenticateWithExtras() throws InterruptedException, RequestError {
        String pairingId = "pairing ID";
        String terminalName = "my computer";
        String actionName = "action";
        String extraParameter = "extraParam";
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("extra_parameter", extraParameter);
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        toopherApi.authenticate(pairingId, terminalName, actionName, extras);
        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("pairing_id"), pairingId);
        assertEquals(httpClient.getLastCalledData("terminal_name"), terminalName);
        assertEquals(httpClient.getLastCalledData("extra_parameter"), extraParameter);
    }



    @Test
    public void testAuthenticateByUserNameNoExtras() throws InterruptedException, RequestError {
        String extraTerminalName = "terminalNameExtra";
        String userName = "userName";
        String actionName = "action";
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
            createURI("https://api.toopher.test/v1/"), httpClient);
            toopherApi.authenticateByUserName(userName, extraTerminalName, actionName, null);
        assertEquals(httpClient.getLastCalledData("user_name"), userName);
        assertEquals(httpClient.getLastCalledData("terminal_name_extra"), extraTerminalName);
        assertEquals(httpClient.getLastCalledData("action_name"), actionName);
    }

    @Test
    public void testAuthenticateByUserNameWithExtras() throws InterruptedException, RequestError {
        String extraTerminalName = "terminalNameExtra";
        String userName = "userName";
        String actionName = "action";
        Map<String, String> extras = new HashMap<String, String>();
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
            toopherApi.authenticateByUserName(userName, extraTerminalName, actionName, extras);
        assertEquals(httpClient.getLastCalledData("user_name"), userName);
        assertEquals(httpClient.getLastCalledData("terminal_name_extra"), extraTerminalName);
        assertEquals(httpClient.getLastCalledData("action_name"), actionName);
    }

    @Test
    public void testGetAuthenticationStatus() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        AuthenticationStatus authReq = toopherApi.getAuthenticationStatus("1");
        assertEquals(createURI("https://api.toopher.test/v1/authentication_requests/1"), httpClient.getLastCalledEndpoint());
        assertEquals(authReq.id, "1");
        assertTrue(authReq.pending);
        assertTrue(authReq.granted);
        assertTrue(authReq.automated);
        assertEquals(authReq.terminalName, "some user");
        assertEquals(authReq.terminalId, "1");
        assertEquals(authReq.reason, "test");
    }

    @Test
    public void testGetAuthenticationStatusThrowRequestError() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, "{'id':'1'}");
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        try{
            toopherApi.getAuthenticationStatus("1");
        } catch (RequestError re){}
    }

    @Test
    public void testGetAuthenticationStatusWithOTP() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        AuthenticationStatus authReq = toopherApi.getAuthenticationStatusWithOTP("1", "ImAPassword");
        assertEquals(createURI("https://api.toopher.test/v1/authentication_requests/1/otp_auth"), httpClient.getLastCalledEndpoint());
        assertEquals(authReq.id, "1");
        assertTrue(authReq.pending);
        assertTrue(authReq.granted);
        assertTrue(authReq.automated);
        assertEquals(authReq.terminalName, "some user");
        assertEquals(authReq.terminalId, "1");
        assertEquals(authReq.reason, "test");
    }

    @Test
    public void testGetAuthenticationStatusWithOTPThrowRequestError() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, "{'id':'1'}");
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        try{
            toopherApi.getAuthenticationStatusWithOTP("1", "ImAPassword");
        } catch (RequestError re){}
    }

    @Test
    public void testAssignUserFriendlyNameToTerminal() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        String userName = "some user";
        String terminalName = "my computer";
        String extraTerminalName = "my extra computer";
        toopherApi.assignUserFriendlyNameToTerminal(userName, terminalName, extraTerminalName);
        assertEquals(httpClient.getLastCalledData("user_name"), userName);
        assertEquals(httpClient.getLastCalledData("name"), terminalName);
        assertEquals(httpClient.getLastCalledData("name_extra"), extraTerminalName);
    }

    @Test
    public void testEnableToopherForUser() throws InterruptedException, RequestError {
        Map<URI, String> expectedUriResponses = new HashMap<URI, String>();
        expectedUriResponses.put(createURI("https://api.toopher.test/v1/users?name=1"), "[{'id': '1'}]");
        expectedUriResponses.put(createURI("https://api.toopher.test/v1/users/1"), "[{}]");

        HttpClientMock httpClient = new HttpClientMock(expectedUriResponses);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        toopherApi.setToopherEnabledForUser("1", false);
        String actualResponse = httpClient.getExpectedResponse();
        String expectedResponse = expectedUriResponses.get(httpClient.getLastCalledEndpoint());
        assertEquals(actualResponse, expectedResponse);
        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("disable_toopher_auth"), "true");
    }

    @Test
    public void testEnableToopherForUserWhenUserNameIsNotPresent() throws InterruptedException, RequestError {
        Map<URI, String> expectedUriResponses = new HashMap<URI, String>();
        expectedUriResponses.put(createURI("https://api.toopher.test/v1/users?name=1"), "[{'id': '1'}]");
        expectedUriResponses.put(createURI("https://api.toopher.test/v1/users/1"), "[{}]");

        HttpClientMock httpClient = new HttpClientMock(expectedUriResponses);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        try {
            toopherApi.setToopherEnabledForUser("", true);
        } catch (RequestError re) {}
    }

    @Test
    public void testCreateQrPairing() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200,
                "{'id':'1','enabled':true,'pending':true,'user':{'id':'1','name':'some user'}}".replace("'", "\""));

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        PairingStatus pairing = toopherApi.pairWithQrCode("some user");
        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(pairing.userId, "1");
        assertEquals(pairing.userName, "some user");
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    public URI createURI(String url) {
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

    @Test
    public void testAuthenticationStatusFactory() throws InterruptedException, URISyntaxException {
        HttpClientMock clientMock = new HttpClientMock(200, "{}");
        AuthenticationStatusFactoryMock factoryMock = new AuthenticationStatusFactoryMock();

        ToopherAPI api = new ToopherAPI.Builder(CONSUMER_KEY, CONSUMER_SECRET)
                .setBaseUri(BASE_URI_STRING)
                .setHttpClient(clientMock)
                .setAuthenticationStatusFactory(factoryMock)
                .build();

        try {
            api.authenticate(PAIRING_ID, TERMINAL_NAME);
        } catch (RequestError re) { fail(); }
    }
}
