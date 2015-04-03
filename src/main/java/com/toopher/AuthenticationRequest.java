package com.toopher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide information about the status of an authentication request
 */
public class AuthenticationRequest extends ApiResponseObject {
    /**
     * The {@link com.toopher.ToopherApi} associated with the authentication request
     */
    public ToopherApi api;

    /**
     * The unique id for the authentication request
     */
    public String id;

    /**
     * Indicates if the user has responded to the authentication request
     */
    public boolean pending;

    /**
     * Indicates if the authentication request was granted
     */
    public boolean granted;

    /**
     * Indicates if the authentication request was automated
     */
    public boolean automated;

    /**
     * Indicates the reason (if any) for the authentication request's outcome
     */
    public String reason;

    /**
     * Indicates the code associated with the reason for the authentication request's outcome
     */
    public int reasonCode;

    /**
     * The {@link com.toopher.UserTerminal} associated with the authentication request
     */
    public UserTerminal terminal;

    /**
     * The {@link com.toopher.Action} associated with the authentication request
     */
    public Action action;

    /**
     * The {@link com.toopher.User} associated with the authentication request
     */
    public User user;

    public AuthenticationRequest(JSONObject json, ToopherApi toopherApi) throws JSONException {
        super(json);

        this.api = toopherApi;
        this.id = json.getString("id");
        this.pending = json.getBoolean("pending");
        this.granted = json.getBoolean("granted");
        this.automated = json.getBoolean("automated");
        this.reason = json.getString("reason");
        this.reasonCode = json.getInt("reason_code");
        this.terminal = new UserTerminal(json.getJSONObject("terminal"), toopherApi);
        this.action = new Action(json.getJSONObject("action"));
        this.user = new User(json.getJSONObject("user"), toopherApi);
    }

    @Override
    public String toString() {
        return String.format("[AuthenticationRequest: id=%s; pending=%b; granted=%b; automated=%b; reason=%s; reasonCode=%d; terminalId=%s, terminalName=%s, terminalRequesterSpecifiedId=%s, actionId=%s, actionName=%s, userId=%s, userName=%s, userToopherAuthenticationEnabled=%b]",
                id, pending, granted, automated, reason, reasonCode, terminal.id, terminal.name, terminal.requesterSpecifiedId, action.id, action.name, user.id, user.name, user.toopherAuthenticationEnabled);
    }

    /**
     * Update the AuthenticationRequest object with JSON response from the API
     *
     * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
     * @throws org.json.JSONException Thrown by the JSON.org classes when an exceptional condition is encountered
     */
    public void refreshFromServer() throws RequestError, JSONException {
        String endpoint = String.format("authentication_requests/%s", id);
        JSONObject result = api.advanced.raw.get(endpoint);
        update(result);
    }

    /**
     * Grant authentication request with OTP
     *
     * @param otp One-time password for authentication request
     * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
     * @throws org.json.JSONException Thrown by the JSON.org classes when an exceptional condition is encountered
     */
    public void grantWithOtp(String otp) throws RequestError, JSONException {
        String endpoint = String.format("authentication_requests/%s/otp_auth", id);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("otp", otp));
        JSONObject result = api.advanced.raw.post(endpoint, params);
        update(result);
    }

    /**
     * Update the AuthenticationRequest object with JSON response
     *
     * @param jsonResponse The JSON response from the API
     * @throws org.json.JSONException Thrown by the JSON.org classes when an exceptional condition is encountered
     */
    private void update(JSONObject jsonResponse) throws JSONException {
        this.pending = jsonResponse.getBoolean("pending");
        this.granted = jsonResponse.getBoolean("granted");
        this.automated = jsonResponse.getBoolean("automated");
        this.reason = jsonResponse.getString("reason");
        this.reasonCode = jsonResponse.getInt("reason_code");
        this.terminal.update(jsonResponse.getJSONObject("terminal"));
        this.action.update(jsonResponse.getJSONObject("action"));
        this.user.update(jsonResponse.getJSONObject("user"));
        this.updateRawResponse(jsonResponse);
    }
}
