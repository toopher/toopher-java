package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by graceyim on 1/22/15.
 */
public class User extends ApiResponseObject {
    /**
     * The ToopherAPI associated with this user
     */
    public ToopherAPI api;

    /**
     * The unique id for the user
     */
    public String id;

    /**
     * The name of the user
     */
    public String name;

    /**
     * Whether or not the user is Toopher-enabled
     */
    public boolean toopherAuthenticationEnabled;

    public User (JSONObject json, ToopherAPI toopherAPI) throws JSONException {
        super(json);

        this.api = toopherAPI;
        this.id = json.getString("id");
        this.name = json.getString("name");
        if (json.has("disable_toopher_auth")) {
            this.toopherAuthenticationEnabled = !json.getBoolean("disable_toopher_auth");
        } else {
            this.toopherAuthenticationEnabled = true;
        }
    }

    public void update(JSONObject jsonResponse) {
        this.name = jsonResponse.getString("name");
        if (jsonResponse.has("disable_toopher_auth")) {
            this.toopherAuthenticationEnabled = !jsonResponse.getBoolean("disable_toopher_auth");
        } else {
            this.toopherAuthenticationEnabled = true;
        }
    }
}
