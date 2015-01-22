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

public class TestToopherAPI {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1";

    private ToopherAPI toopherApi;
    private String id;
    private String name;
    private JSONObject user;
    private String userId;
    private String userName;
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
