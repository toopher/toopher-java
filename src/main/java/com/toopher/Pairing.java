package com.toopher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides information about the status of a pairing request
 */
public class Pairing extends ApiResponseObject {
    /**
     * The {@link com.toopher.ToopherApi} associated with this pairing
     */
    public ToopherApi api;

    /**
     * The unique id for the pairing request
     */
    public String id;

    /**
     * Indicates if the pairing was enabled by the user
     */
    public boolean enabled;

    /**
     * Indicates if the user has responded to the pairing request
     */
    public boolean pending;

    /**
     * The {@link com.toopher.User} associated with the pairing request
     */
    public User user;

    public Pairing(JSONObject json, ToopherApi toopherApi) throws JSONException {
        super(json);

        this.api = toopherApi;
        this.id = json.getString("id");
        this.enabled = json.getBoolean("enabled");
        this.pending = json.getBoolean("pending");
        this.user = new User(json.getJSONObject("user"), toopherApi);
    }

    @Override
    public String toString() {
        return String.format("[Pairing: id=%s; enabled=%b; pending=%b; userId=%s, userName=%s, userToopherAuthenticationEnabled=%b]",
                id, enabled, pending, user.id, user.name, user.toopherAuthenticationEnabled);
    }

    /**
     * Update the Pairing object with JSON response from the API
     *
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public void refreshFromServer() throws RequestError, JSONException {
        String endpoint = String.format("pairings/%s", id);
        JSONObject result = api.advanced.raw.get(endpoint);
        update(result);
    }

    /**
     * Retrieves QR code image for the pairing from the API
     *
     * @return QR code image stored in a byte[]
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public byte[] getQrCodeImage() throws RequestError, JSONException {
        String endpoint = String.format("qr/pairings/%s", id);
        return api.advanced.raw.get(endpoint);
    }

    /**
     * Retrieves link to allow user to reset the pairing
     *
     * @return Reset link stored as a String
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public String getResetLink() throws RequestError, JSONException {
        return getResetLink(null);
    }

    /**
     * Retrieves link to allow user to reset the pairing
     *
     * @param extras An optional Map of extra parameters to provide to the API
     * @return Reset link stored as a String
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public String getResetLink(Map<String, String> extras) throws RequestError, JSONException {
        String endpoint = String.format("pairings/%s/generate_reset_link", id);
        JSONObject result = api.advanced.raw.post(endpoint, extras);
        return result.getString("url");
    }

    /**
     * Sends reset link to user via email
     *
     * @param email The email address where the reset link is sent
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public void emailResetLink(String email) throws RequestError, JSONException {
        emailResetLink(email, null);
    }

    /**
     * Sends reset link to user via email
     *
     * @param email  The email address where the reset link is sent
     * @param extras An optional Map of extra parameters to provide to the API
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public void emailResetLink(String email, Map<String, String> extras) throws RequestError, JSONException {
        String endpoint = String.format("pairings/%s/send_reset_link", id);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("reset_email", email));
        api.advanced.raw.post(endpoint, params, extras);
    }

    /**
     * Update the Pairing object with JSON response
     *
     * @param jsonResponse The JSON response from the API
     */
    private void update(JSONObject jsonResponse) throws JSONException {
        this.enabled = jsonResponse.getBoolean("enabled");
        this.pending = jsonResponse.getBoolean("pending");
        this.user.update(jsonResponse.getJSONObject("user"));
        this.updateRawResponse(jsonResponse);
    }
}
