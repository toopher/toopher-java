package com.toopher;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;

import static org.junit.Assert.*;


public class TestUser {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1/";

    private static String id;
    private static String name;
    private static JSONObject json;

    @BeforeClass
    public static void setUp() {
        id = UUID.randomUUID().toString();
        name = "userName";

        json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("disable_toopher_auth", false);
    }

    @Test
    public void testRefreshFromServer() throws InterruptedException, RequestError {
        JSONObject newJson = new JSONObject();
        newJson.put("name", "userNameChanged");
        newJson.put("disable_toopher_auth", true);

        HttpClientMock httpClient = new HttpClientMock(200, newJson.toString());
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        User user = new User(json, toopherApi);

        assertEquals(id, user.id);
        assertEquals(name, user.name);
        assertTrue("User should be Toopher authentication enabled.", user.toopherAuthenticationEnabled);

        user.refreshFromServer();

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(String.format("users/%s", id), httpClient.getLastCalledEndpoint());
        assertEquals(id, user.id);
        assertEquals("userNameChanged", user.name);
        assertFalse("User should not be Toopher authentication enabled.", user.toopherAuthenticationEnabled);
    }

    @Test
    public void testEnableToopherAuthentication() throws InterruptedException, RequestError {
        JSONObject newJson = new JSONObject();
        newJson.put("id", id);
        newJson.put("name", name);
        newJson.put("disable_toopher_auth", false);

        HttpClientMock httpClient = new HttpClientMock(200, newJson.toString());
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        User user = new User(json, toopherApi);
        user.enableToopherAuthentication();

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals(String.format("users/%s", id), httpClient.getLastCalledEndpoint());
        assertEquals("false", httpClient.getLastCalledData("disable_toopher_auth"));
        assertEquals(id, user.id);
        assertEquals(name, user.name);
        assertTrue("User should be Toopher authentication enabled.", user.toopherAuthenticationEnabled);
    }

    @Test
    public void testDisableToopherAuthentication() throws InterruptedException, RequestError {
        JSONObject newJson = new JSONObject();
        newJson.put("id", id);
        newJson.put("name", name);
        newJson.put("disable_toopher_auth", "true");

        HttpClientMock httpClient = new HttpClientMock(200, newJson.toString());
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        User user = new User(json, toopherApi);
        user.disableToopherAuthentication();

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals(String.format("users/%s", id), httpClient.getLastCalledEndpoint());
        assertEquals("true", httpClient.getLastCalledData("disable_toopher_auth"));
        assertEquals(id, user.id);
        assertEquals(name, user.name);
        assertFalse("User should not be Toopher authentication enabled.", user.toopherAuthenticationEnabled);
    }

    @Test
    public void testReset() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200, "{}");
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        User user = new User(json, toopherApi);
        user.reset();

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("users/reset", httpClient.getLastCalledEndpoint());
        assertEquals(name, httpClient.getLastCalledData("name"));
    }

    @Test
    public void testUpdate() throws InterruptedException {
        JSONObject updatedJson = new JSONObject();
        updatedJson.put("name", "userNameChanged");
        updatedJson.put("disable_toopher_auth", true);

        HttpClientMock httpClient = new HttpClientMock(200, "{}");
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        User user = new User(json, toopherApi);

        assertEquals(name, user.name);
        assertTrue("User should be Toopher authentication enabled.", user.toopherAuthenticationEnabled);

        user.update(updatedJson);

        assertEquals("userNameChanged", user.name);
        assertFalse("User should not be Toopher authentication enabled.", user.toopherAuthenticationEnabled);
    }
}
