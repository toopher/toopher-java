package com.toopher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides information about the status of a user
 *
 */
public class User extends ApiResponseObject {
    /**
     * The ToopherAPI object associated with this user
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
     * The Toopher-enabled status of the user
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

    @Override
    public String toString() {
        return String.format("[User: id=%s, name=%s, toopherAuthenticationEnabled=%b]",
                id, name, toopherAuthenticationEnabled);
    }

    /**
     * Update the User object with JSON response from the API
     *
     * @throws RequestError
     *          Thrown when an exceptional condition is encountered
     */
    public void refreshFromServer() throws RequestError {
        String endpoint = String.format("users/%s", id);
        JSONObject result = api.advanced.raw.get(endpoint);
        update(result);
    }

    /**
     * Enable Toopher authentication for the user
     *
     * @throws RequestError
     *          Thrown when an exceptional condition is encountered
     */
    public void enableToopherAuthentication() throws RequestError {
        String endpoint = String.format("users/%s", id);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("disable_toopher_auth", "false"));
        JSONObject result = api.advanced.raw.post(endpoint, params);
        update(result);
    }

    /**
     * Disable Toopher authentication for the user
     *
     * @throws RequestError
     *          Thrown when an exceptional condition is encountered
     */
    public void disableToopherAuthentication() throws RequestError {
        String endpoint = String.format("users/%s", id);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("disable_toopher_auth", "true"));
        JSONObject result = api.advanced.raw.post(endpoint, params);
        update(result);
    }

    /**
     * Remove all pairings for the user
     *
     * @throws RequestError
     *          Thrown when an exceptional condition is encountered
     */
    public void reset() throws RequestError {
        String endpoint = "users/reset";
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", name));
        api.advanced.raw.post(endpoint, params);
    }

    /**
     * Update the User object with JSON response
     *
     * @param jsonResponse
     *          The JSON response from the API
     */
    public void update(JSONObject jsonResponse) {
        this.name = jsonResponse.getString("name");
        if (jsonResponse.has("disable_toopher_auth")) {
            this.toopherAuthenticationEnabled = !jsonResponse.getBoolean("disable_toopher_auth");
        } else {
            this.toopherAuthenticationEnabled = true;
        }
        this.updateRawResponse(jsonResponse);
    }
}
