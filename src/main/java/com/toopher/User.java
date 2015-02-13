package com.toopher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides information about the status of a user
 */
public class User extends ApiResponseObject {
    /**
     * The {@link com.toopher.ToopherApi} associated with this user
     */
    public ToopherApi api;

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

    public User(JSONObject json, ToopherApi toopherApi) throws JSONException {
        super(json);

        this.api = toopherApi;
        this.id = json.getString("id");
        this.name = json.getString("name");
        this.toopherAuthenticationEnabled = json.getBoolean("toopher_authentication_enabled");
    }

    @Override
    public String toString() {
        return String.format("[User: id=%s, name=%s, toopherAuthenticationEnabled=%b]",
                id, name, toopherAuthenticationEnabled);
    }

    /**
     * Update the User object with JSON response from the API
     *
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public void refreshFromServer() throws RequestError, JSONException {
        String endpoint = String.format("users/%s", id);
        JSONObject result = api.advanced.raw.get(endpoint);
        update(result);
    }

    /**
     * Enable Toopher authentication for the user
     *
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public void enableToopherAuthentication() throws RequestError, JSONException {
        String endpoint = String.format("users/%s", id);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("toopher_authentication_enabled", "true"));
        JSONObject result = api.advanced.raw.post(endpoint, params);
        update(result);
    }

    /**
     * Disable Toopher authentication for the user
     *
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public void disableToopherAuthentication() throws RequestError, JSONException {
        String endpoint = String.format("users/%s", id);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("toopher_authentication_enabled", "false"));
        JSONObject result = api.advanced.raw.post(endpoint, params);
        update(result);
    }

    /**
     * Remove all pairings for the user
     *
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public void reset() throws RequestError, JSONException {
        String endpoint = "users/reset";
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", name));
        api.advanced.raw.post(endpoint, params);
    }

    /**
     * Update the User object with JSON response
     *
     * @param jsonResponse The JSON response from the API
     */
    public void update(JSONObject jsonResponse) throws JSONException {
        this.name = jsonResponse.getString("name");
        this.toopherAuthenticationEnabled = jsonResponse.getBoolean("toopher_authentication_enabled");
        this.updateRawResponse(jsonResponse);
    }
}
