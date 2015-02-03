package com.toopher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides information about the status of a pairing request
 * 
 */
public class Pairing extends ApiResponseObject {
    /**
     * The ToopherAPI associated with this pairing
     */
    public ToopherAPI api;

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

    public Pairing(JSONObject json, ToopherAPI toopherAPI) throws JSONException {
        super(json);

        this.api = toopherAPI;
        this.id = json.getString("id");
        this.enabled = json.getBoolean("enabled");
        this.pending = json.getBoolean("pending");
        this.user = new User(json.getJSONObject("user"), toopherAPI);
    }

    public void refreshFromServer() throws RequestError {
        String endpoint = "pairings/{0}".format(id);
        JSONObject result = api.advanced.raw.get(endpoint);
        update(result);
    }

    private void update(JSONObject jsonResponse) {
        this.enabled = jsonResponse.getBoolean("enabled");
        this.pending = jsonResponse.getBoolean("pending");
        this.user.update(jsonResponse.getJSONObject("user"));
    }

    public String getResetLink() throws RequestError {
        Map<String, String> extras = new HashMap<String, String>();
        return getResetLink(extras);
    }

    public String getResetLink(Map<String, String> extras) throws RequestError {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        for (Map.Entry<String, String> entry : extras.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        String endpoint = "pairings/{0}/generate_reset_link".format(id);
        JSONObject result = api.advanced.raw.post(endpoint, params);
        return result.getString("url");
    }

    public void emailResetLink(String email) throws RequestError {
        emailResetLink(email, null);
    }

    public void emailResetLink(String email, Map<String, String> extras) throws RequestError {
        String endpoint = "pairings/{0}/send_reset_link".format(id);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("reset_email", email));
        api.advanced.raw.post(endpoint, params, extras);
    }
}
