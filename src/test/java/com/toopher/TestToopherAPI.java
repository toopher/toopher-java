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
    private final String CONSUMER_KEY = "key";
    private final String CONSUMER_SECRET = "secret";
    private final String PAIRING_ID = "pairing_id";
    private final String TERMINAL_NAME = "terminal_name";
    private final String ACTION_NAME = "action_name";
    private final String USER_NAME = "user_name";

    @Test
    public void testCreatePairing() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200,
                "{'id':'1','enabled':true,'pending':true,'user':{'id':'1','name':'some user'}}".replace("'", "\""));

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
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
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        toopherApi.authenticate(PAIRING_ID, TERMINAL_NAME, ACTION_NAME);
        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("pairing_id"), PAIRING_ID);
        assertEquals(httpClient.getLastCalledData("terminal_name"), TERMINAL_NAME);
        assertEquals(httpClient.getLastCalledData("action_name"), ACTION_NAME);
    }

    @Test
    public void testAuthenticateThrowingRequestError() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, "{'id':'1'}");
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        try {
            toopherApi.authenticate(PAIRING_ID, TERMINAL_NAME);
            fail();
        } catch (RequestError re) {
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
        try {
            toopherApi.getAuthenticationStatus("1");
            fail("My method didn't throw when I expected it to");
        } catch (RequestError re) {
        }
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
        try {
            toopherApi.getAuthenticationStatusWithOTP("1", "ImAPassword");
        } catch (RequestError re) {
        }
    }

    @Test
    public void testAssignUserFriendlyNameToTerminal() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        final String extraTerminalName = "my extra computer";
        toopherApi.assignUserFriendlyNameToTerminal(USER_NAME, TERMINAL_NAME, extraTerminalName);
        assertEquals(httpClient.getLastCalledData("user_name"), USER_NAME);
        assertEquals(httpClient.getLastCalledData("name"), TERMINAL_NAME);
        assertEquals(httpClient.getLastCalledData("name_extra"), extraTerminalName);
    }

    @Test
    public void testEnableToopherForUser() throws InterruptedException, RequestError {
        Map<URI, ResponseMock> expectedUriResponses = new HashMap<URI, ResponseMock>();
        expectedUriResponses.put(createURI("https://api.toopher.test/v1/users?name=1"), new ResponseMock(200, "[{'id': '1'}]"));
        expectedUriResponses.put(createURI("https://api.toopher.test/v1/users/1"), new ResponseMock(200, "{}"));

        HttpClientMock httpClient = new HttpClientMock(expectedUriResponses);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        toopherApi.setToopherEnabledForUser("1", false);
        String actualResponse = httpClient.getExpectedResponse();
        String expectedResponse = expectedUriResponses.get(httpClient.getLastCalledEndpoint()).getResponseBody();
        assertEquals(actualResponse, expectedResponse);
        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("disable_toopher_auth"), "true");
    }

    @Test
    public void testEnableToopherForUserWhenUserNameIsNotPresent() throws InterruptedException, RequestError {
        Map<URI, ResponseMock> expectedUriResponses = new HashMap<URI, ResponseMock>();
        expectedUriResponses.put(createURI("https://api.toopher.test/v1/users?name=1"), new ResponseMock(200, "[]"));

        HttpClientMock httpClient = new HttpClientMock(expectedUriResponses);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        try {
            toopherApi.setToopherEnabledForUser("1", true);
            fail();
        } catch (RequestError re) {
        }
    }

    @Test
    public void testEnableToopherForUserWithDuplicateUserName() throws InterruptedException, RequestError {
        Map<URI, ResponseMock> expectedUriResponses = new HashMap<URI, ResponseMock>();
        expectedUriResponses.put(createURI("https://api.toopher.test/v1/users?name=1"), new ResponseMock(200, "[{'id': '1'}, {'id': '1'}]"));

        HttpClientMock httpClient = new HttpClientMock(expectedUriResponses);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1/"), httpClient);
        try {
            toopherApi.setToopherEnabledForUser("1", true);
            fail();
        } catch (RequestError re) {
        }
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

    @Test
    public void testAuthenticationStatusFactoryBuildsValidJSON() throws InterruptedException, URISyntaxException {
        HttpClientMock clientMock = new HttpClientMock(200, "{}");
        AuthenticationStatusFactoryMock factoryMock = new AuthenticationStatusFactoryMock();

        ToopherAPI api = new ToopherAPI.Builder(CONSUMER_KEY, CONSUMER_SECRET)
                .setBaseUri(BASE_URI_STRING)
                .setHttpClient(clientMock)
                .setAuthenticationStatusFactory(factoryMock)
                .build();

        try {
            api.authenticate(PAIRING_ID, TERMINAL_NAME);
        } catch (RequestError re) {
            fail();
        }
    }

    @Test
    public void testParseRequestErrorUserDisabled() throws InterruptedException, URISyntaxException, RequestError {
        Map<URI, ResponseMock> expectedStatusResponse = new HashMap<URI, ResponseMock>();
        expectedStatusResponse.put(createURI("https://api.toopher.test/v1/authentication_requests/initiate"), new ResponseMock(409, "{'error_code' : '704', 'error_message' : 'Toopher User Disabled Error'}"));

        HttpClientMock httpClient = new HttpClientMock(expectedStatusResponse);
        AuthenticationStatusFactoryMock factoryMock = new AuthenticationStatusFactoryMock();

        ToopherAPI toopherApi = new ToopherAPI.Builder(CONSUMER_KEY, CONSUMER_SECRET)
                .setBaseUri(BASE_URI_STRING)
                .setHttpClient(httpClient)
                .setAuthenticationStatusFactory(factoryMock)
                .build();

        try {
            toopherApi.authenticateByUserName(USER_NAME, TERMINAL_NAME, ACTION_NAME, null);
            fail();
        } catch (ToopherUserDisabledError toopherUserDisabledError) {
        }

    }

    @Test
    public void testParseRequestErrorUnknownUser() throws InterruptedException, URISyntaxException, RequestError {
        Map<URI, ResponseMock> expectedStatusResponse = new HashMap<URI, ResponseMock>();
        expectedStatusResponse.put(createURI("https://api.toopher.test/v1/authentication_requests/initiate"), new ResponseMock(409, "{'error_code' : '705', 'error_message' : 'Toopher Unknown User Error'}"));

        HttpClientMock httpClient = new HttpClientMock(expectedStatusResponse);
        AuthenticationStatusFactoryMock factoryMock = new AuthenticationStatusFactoryMock();

        ToopherAPI toopherApi = new ToopherAPI.Builder(CONSUMER_KEY, CONSUMER_SECRET)
                .setBaseUri(BASE_URI_STRING)
                .setHttpClient(httpClient)
                .setAuthenticationStatusFactory(factoryMock)
                .build();

        try {
            toopherApi.authenticateByUserName(USER_NAME, TERMINAL_NAME, ACTION_NAME, null);
            fail();
        } catch (ToopherUnknownUserError toopherUnknownUserError) {
        }

    }

    @Test
    public void testParseRequestErrorUnknownTerminal() throws InterruptedException, URISyntaxException, RequestError {
        Map<URI, ResponseMock> expectedStatusResponse = new HashMap<URI, ResponseMock>();
        expectedStatusResponse.put(createURI("https://api.toopher.test/v1/authentication_requests/initiate"), new ResponseMock(409, "{'error_code' : '706', 'error_message' : 'Toopher Unknown Terminal Error'}"));

        HttpClientMock httpClient = new HttpClientMock(expectedStatusResponse);
        AuthenticationStatusFactoryMock factoryMock = new AuthenticationStatusFactoryMock();

        ToopherAPI toopherApi = new ToopherAPI.Builder(CONSUMER_KEY, CONSUMER_SECRET)
                .setBaseUri(BASE_URI_STRING)
                .setHttpClient(httpClient)
                .setAuthenticationStatusFactory(factoryMock)
                .build();

        try {
            toopherApi.authenticateByUserName(USER_NAME, TERMINAL_NAME, ACTION_NAME, null);
            fail();
        } catch (ToopherUnknownTerminalError toopherUnknownTerminalError) {
        }

    }

    @Test
    public void testParseRequestErrorClient() throws InterruptedException, URISyntaxException, RequestError {
        Map<URI, ResponseMock> expectedStatusResponse = new HashMap<URI, ResponseMock>();
        expectedStatusResponse.put(createURI("https://api.toopher.test/v1/authentication_requests/initiate"), new ResponseMock(409, "{'error_code' : '707', 'error_message' : 'Toopher Client Error'}"));

        HttpClientMock httpClient = new HttpClientMock(expectedStatusResponse);
        AuthenticationStatusFactoryMock factoryMock = new AuthenticationStatusFactoryMock();

        ToopherAPI toopherApi = new ToopherAPI.Builder(CONSUMER_KEY, CONSUMER_SECRET)
                .setBaseUri(BASE_URI_STRING)
                .setHttpClient(httpClient)
                .setAuthenticationStatusFactory(factoryMock)
                .build();

        try {
            toopherApi.authenticateByUserName(USER_NAME, TERMINAL_NAME, ACTION_NAME, null);
            fail();
        } catch (ToopherClientError toopherClientError) {
        }

    }

    @Test
    public void testParseRequestErrorNotJSON() throws InterruptedException, URISyntaxException, RequestError {
        Map<URI, ResponseMock> expectedStatusResponse = new HashMap<URI, ResponseMock>();
        expectedStatusResponse.put(createURI("https://api.toopher.test/v1/authentication_requests/initiate"), new ResponseMock(409, "{'error_code' : '707', 'I'm not valid JSON body text'}"));

        HttpClientMock httpClient = new HttpClientMock(expectedStatusResponse);
        AuthenticationStatusFactoryMock factoryMock = new AuthenticationStatusFactoryMock();

        ToopherAPI toopherApi = new ToopherAPI.Builder(CONSUMER_KEY, CONSUMER_SECRET)
                .setBaseUri(BASE_URI_STRING)
                .setHttpClient(httpClient)
                .setAuthenticationStatusFactory(factoryMock)
                .build();

        try {
            toopherApi.authenticateByUserName(USER_NAME, TERMINAL_NAME, ACTION_NAME, null);
            fail();
        } catch (RequestError re) {
        }

    }

}
