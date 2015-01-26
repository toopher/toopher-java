package com.toopher;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;

import static org.junit.Assert.*;


/**
 * Created by graceyim on 1/26/15.
 */
public class TestUserTerminal {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1";

    private String id;
    private String name;
    private String requesterSpecifiedId;
    public JSONObject user;
    public String userName;

    @Before
    public void setUp() {
        this.id = UUID.randomUUID().toString();
        this.name = "terminalName";
        this.requesterSpecifiedId = "terminalNameExtra";
        this.user = new JSONObject();
        this.user.put("id", UUID.randomUUID().toString());
        this.user.put("name", "userName");
        this.userName = this.user.getString("name");
    }

    @Test
    public void testRefreshFromServer() throws InterruptedException, RequestError {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("name", name);
        jsonResponse.put("name_extra", requesterSpecifiedId);
        jsonResponse.put("user", user);
        UserTerminal terminal = new UserTerminal(jsonResponse);

        JSONObject newJsonResponse = jsonResponse;
        newJsonResponse.remove("name");
        jsonResponse.put("name", "terminalNameChanged");
        newJsonResponse.remove("name_extra");
        jsonResponse.put("name_extra", "terminalNameExtraChanged");
        JSONObject newUser = newJsonResponse.getJSONObject("user");
        newUser.remove("name");
        newUser.put("name", "userNameChanged");

        HttpClientMock httpClient = new HttpClientMock(200, newJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        assertEquals(terminal.id, id);
        assertEquals(terminal.name, name);
        assertEquals(terminal.requesterSpecifiedId, requesterSpecifiedId);
        assertEquals(terminal.user.name, userName);

        terminal.refresh_from_server(toopherAPI);

        assertEquals(terminal.id, id);
        assertEquals(terminal.name, "terminalNameChanged");
        assertEquals(terminal.requesterSpecifiedId, "terminalNameExtraChanged");
        assertEquals(terminal.user.name, "userNameChanged");
    }
}
