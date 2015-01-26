package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by graceyim on 1/26/15.
 */
public class Action extends ApiResponseObject {
    /**
     * The unique id for the authentication request
     */
    public String id;

    /**
     * The name of the action
     */
    public String name;

    public Action (JSONObject json) throws JSONException {
        super(json);

        this.id = json.getString("id");
        this.name = json.getString("name");
    }

    public void update(JSONObject jsonResponse) {
        this.name = jsonResponse.getString("name");
    }
}
