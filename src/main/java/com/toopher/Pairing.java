package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides information about the status of a pairing request
 * 
 */
public class Pairing extends ApiResponseObject {
    /**
     * The unique id for the pairing request
     */
    public String id;

    /**
     * Indicates if the pairing has been enabled by the user
     */
    public boolean enabled;

    /**
     * Indicates if the user has reacted to the pairing request
     */
    public boolean pending;

    /**
     * Contains the unique id and description name for the user associated
     * with the pairing request
     */
    public User user;

    @Override
    public String toString() {
        return String.format("[Pairing: id=%s; userId=%s, userName=%s, pending=%b, enabled=%b]", id,
                             user.id, user.name, pending, enabled);
    }

    public Pairing(JSONObject json) throws JSONException {
        super(json);

        this.id = json.getString("id");
        this.enabled = json.getBoolean("enabled");
        this.pending = json.getBoolean("pending");
        this.user = new User(json.getJSONObject("user"));
    }

    public void refreshFromServer(ToopherAPI api) throws RequestError {
        String endpoint = "pairings/{0}".format(id);
        JSONObject result = api.advanced.raw.get(endpoint);
        update(result);
    }

    private void update(JSONObject jsonResponse) {
        this.enabled = jsonResponse.getBoolean("enabled");
        this.pending = jsonResponse.getBoolean("pending");
        this.user.update(jsonResponse.getJSONObject("user"));
    }


}
