package com.toopher;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;

import static org.junit.Assert.*;


/**
 * Created by graceyim on 2/4/15.
 */
public class TestUser {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1";

    private String id;
    private String name;
    private boolean disableToopherAuthentication;
    private JSONObject json;

    @Before
    public void setUp() {
        this.id = UUID.randomUUID().toString();
        this.name = "userName";
        this.disableToopherAuthentication = false;
        this.json = new JSONObject();
        this.json.put("id", id);
        this.json.put("name", name);
        this.json.put("disable_toopher_auth", disableToopherAuthentication);
    }

    @Test
    public void testRefreshFromServer() throws InterruptedException, RequestError {
        JSONObject newJson = new JSONObject();
        newJson.put("name", "userNameChanged");
        newJson.put("disable_toopher_auth", "true");

        HttpClientMock httpClient = new HttpClientMock(200, newJson.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        User user = new User(json, toopherAPI);
        assertEquals(user.id, id);
        assertEquals(user.name, name);
        assertTrue(user.toopherAuthenticationEnabled);

        user.refreshFromServer();

        assertEquals(user.id, id);
        assertEquals(user.name, "userNameChanged");
        assertFalse(user.toopherAuthenticationEnabled);
    }
}
