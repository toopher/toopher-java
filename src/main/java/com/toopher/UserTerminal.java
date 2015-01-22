package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by graceyim on 1/22/15.
 */
public class UserTerminal extends ApiResponseObject {
    public String id;
    public String name;
    public String requesterSpecifiedId;
    public User user;
    public JSONObject raw;

    public UserTerminal (JSONObject json) throws JSONException {
        super(json);

        this.id = json.getString("id");
        this.name = json.getString("name");
        if (json.has("name_extra")) {
            this.requesterSpecifiedId = json.getString("name_extra");
        }
        this.user = new User(json.getJSONObject("user"));
        this.raw = json;
    }
}
