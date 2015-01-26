package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

/**
 * Provide information about the status of an authentication request
 * 
 */
public class AuthenticationRequest extends ApiResponseObject {

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

    public AuthenticationRequest(JSONObject json) throws JSONException{
		super(json);
		
		this.id = json.getString("id");
        this.pending = json.getBoolean("pending");
        this.granted = json.getBoolean("granted");
        this.automated = json.getBoolean("automated");
        this.reason = json.getString("reason");
        this.terminal = new UserTerminal(json.getJSONObject("terminal"));
        this.action = new Action(json.getJSONObject("action"));
        this.user = terminal.user;
	}

    @Override
    public String toString() {
        return String.format("[AuthenticationRequest: id=%s; pending=%b; granted=%b; automated=%b; reason=%s; terminalId=%s; terminalName=%s]",
                             id, pending, granted, automated, reason, terminal.id, terminal.name);
    }

}
