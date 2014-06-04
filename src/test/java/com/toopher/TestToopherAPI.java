package com.toopher;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestToopherAPI {

    private static String AUTH_REQUEST_JSON = "{'id':'1', 'granted':true, 'pending':true,'automated':true, 'reason':'', 'terminal':{'id':'1','name':'some user'}}".replace("'", "\"");

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
                createURI("https://api.toopher.test/v1"), httpClient);
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
                createURI("https://api.toopher.test/v1"), httpClient);
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
        HttpClientMock httpClient = new HttpClientMock(200, null);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1"), httpClient);
        try {
            toopherApi.authenticate(pairingId, terminalName);
        } catch(RequestError re){}
        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("pairing_id"), pairingId);
        assertEquals(httpClient.getLastCalledData("terminal_name"), terminalName);
    }

    @Test
    public void testAuthenticateWithTerminalName() throws InterruptedException, RequestError {
        String pairingId = "pairing ID";
        String terminalName = "my computer";
        HttpClientMock httpClient = new HttpClientMock(200, AUTH_REQUEST_JSON);
        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
            createURI("https://api.toopher.test/v1"), httpClient);
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
                createURI("https://api.toopher.test/v1"), httpClient);
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
            createURI("https://api.toopher.test/v1"), httpClient);
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
                createURI("https://api.toopher.test/v1"), httpClient);
            toopherApi.authenticateByUserName(userName, extraTerminalName, actionName, extras);
        assertEquals(httpClient.getLastCalledData("user_name"), userName);
        assertEquals(httpClient.getLastCalledData("terminal_name_extra"), extraTerminalName);
        assertEquals(httpClient.getLastCalledData("action_name"), actionName);
    }

    @Test
    public void testCreateQrPairing() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200,
                "{'id':'1','enabled':true,'pending':true,'user':{'id':'1','name':'some user'}}".replace("'", "\""));

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1"), httpClient);
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
}
