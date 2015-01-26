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

    public UserTerminal (JSONObject json) throws JSONException {
        super(json);

        this.id = json.getString("id");
        this.name = json.getString("name");
        if (json.has("name_extra")) {
            this.requesterSpecifiedId = json.getString("name_extra");
        }
        this.user = new User(json.getJSONObject("user"));
    }

    @Override
    public String toString() {
        return String.format("[UserTerminal: id=%s; name=%s; requesterSpecifiedId=%s; userName=%s; userId=%s]",
                id, name, requesterSpecifiedId, user.name, user.id);
    }

    public void update(JSONObject jsonResponse) {
        this.name = jsonResponse.getString("name");
        if (jsonResponse.has("name_extra")) {
            this.requesterSpecifiedId = jsonResponse.getString("name_extra");
        }
        this.user.update(jsonResponse.getJSONObject("user"));
    }
}
