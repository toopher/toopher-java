package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticationStatusFactory {
    public AuthenticationStatusFactory() {}

    public AuthenticationStatus create(JSONObject json) throws JSONException {
        final JSONObject terminal = json.getJSONObject("terminal");
        return new AuthenticationStatus(
            json.getString("id"),
            json.getBoolean("pending"),
            json.getBoolean("granted"),
            json.getBoolean("automated"),
            json.getString("reason"),
            terminal.getString("id"),
            terminal.getString("name"),
            json);
    }
}
