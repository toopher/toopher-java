package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

/**
 * Provide information about the status of an authentication request
 * 
 */
public class AuthenticationStatus extends ApiResponseObject {

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
     * The unique id for the terminal associated with the request
     */
    public String terminalId;

    /**
     * The descriptive name for the terminal associated with the request
     */
    public String terminalName;
    
    
    public AuthenticationStatus(JSONObject json) throws JSONException{
		super(json);
		
		this.id = json.getString("id");
        this.pending = json.getBoolean("pending");
        this.granted = json.getBoolean("granted");
        this.automated = json.getBoolean("automated");
        this.reason = json.getString("reason");

        JSONObject terminal = json.getJSONObject("terminal");
        this.terminalId = terminal.getString("id");
        this.terminalName = terminal.getString("name");

	}

    @Override
    public String toString() {
        return String.format("[AuthenticationStatus: id=%s; pending=%b; granted=%b; automated=%b; reason=%s; terminalId=%s; terminalName=%s]",
                             id, pending, granted, automated, reason, terminalId, terminalName);
    }

}
