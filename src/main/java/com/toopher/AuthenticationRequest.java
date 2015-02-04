package com.toopher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provide information about the status of an authentication request
 * 
 */
public class AuthenticationRequest extends ApiResponseObject {
    /**
     * The ToopherAPI associated with this request
     */
    public ToopherAPI api;

	/**
     * The unique id for the authentication request
     */
    public String id;

    /**
     * Indicates if the request is still pending
     */
    public boolean pending;

    /**
     * Indicates if the request was granted
     */
    public boolean granted;

    /**
     * Indicates if the request was automated
     */
    public boolean automated;

    /**
     * Indicates the reason (if any) for the request's outcome
     */
    public String reason;

    /**
     * Indicates the code associated with the reason for the request's outcome
     */
    public int reasonCode;

    /**
     * Contains the unique id and descriptive name for the terminal
     * associated with the request
     */
    public UserTerminal terminal;

    /**
     * Contains the unique id and descriptive name for the user
     * associated with the request
     */
    public User user;

    /**
     * Contains the unique id and descriptive name for the action
     * associated with the request
     */
    public Action action;

    public AuthenticationRequest(JSONObject json, ToopherAPI toopherAPI) throws JSONException{
		super(json);

        this.api = toopherAPI;
		this.id = json.getString("id");
        this.pending = json.getBoolean("pending");
        this.granted = json.getBoolean("granted");
        this.automated = json.getBoolean("automated");
        this.reason = json.getString("reason");
        this.reasonCode = json.getInt("reason_code");
        this.terminal = new UserTerminal(json.getJSONObject("terminal"), toopherAPI);
        this.action = new Action(json.getJSONObject("action"));
        this.user = terminal.user;
	}

    @Override
    public String toString() {
        return String.format("[AuthenticationRequest: id=%s; pending=%b; granted=%b; automated=%b; reason=%s; terminalId=%s; terminalName=%s]",
                             id, pending, granted, automated, reason, terminal.id, terminal.name);
    }

    public void grantWithOtp(String otp) throws RequestError {
        String endpoint = "authentication_requests/{0}/otp_auth".format(id);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("otp", otp));

        JSONObject json = api.advanced.raw.post(endpoint, params, null);
        try {
            update(json);
        } catch (Exception e) {
            throw new RequestError(e);
        }
    }

    public void refreshFromServer() throws RequestError {
        String endpoint = "authentication_requests{0}".format(id);
        JSONObject result = api.advanced.raw.get(endpoint);
        update(result);
    }

    private void update(JSONObject jsonResponse) {
        this.pending = jsonResponse.getBoolean("pending");
        this.granted = jsonResponse.getBoolean("granted");
        this.automated = jsonResponse.getBoolean("automated");
        this.reason = jsonResponse.getString("reason");
        this.reasonCode = jsonResponse.getInt("reason_code");
        this.terminal.update(jsonResponse.getJSONObject("terminal"));
        this.action.update(jsonResponse.getJSONObject("action"));
        this.user.update(jsonResponse.getJSONObject("user"));
    }

}
