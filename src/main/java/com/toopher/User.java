package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by graceyim on 1/22/15.
 */
public class User extends ApiResponseObject {
    /**
     * The unique id for the user
     */
    public String id;

    /**
     * The name of the user
     */
    public String name;

    /**
     *
     */
    public boolean enabled;

    /**
     * Contains the raw JSONObject
     */
    public JSONObject raw;

    public User (JSONObject json) throws JSONException {
        super(json);

        this.id = json.getString("id");
        this.name = json.getString("name");
        if (json.has("disable_toopher_auth")) {
            this.enabled = (json.getBoolean("disable_toopher_auth") ? false : true);
        }
        this.raw = json;
    }
}
