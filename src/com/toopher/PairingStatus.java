package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides information about the status of a pairing request
 * 
 */
public class PairingStatus extends ApiResponseObject {
    /**
     * The unique id for the pairing request
     */
    public String id;

    /**
     * The unique id for the user associated with the pairing request
     */
    public String userId;

    /**
     * The descriptive name for the user associated with the pairing request
     */
    public String userName;

    /**
     * Indicates if the pairing has been enabled by the user
     */
    public boolean enabled;

    @Override
    public String toString() {
        return String.format("[PairingStatus: id=%s; userId=%s; userName=%s, enabled=%b]", id,
                             userId, userName, enabled);
    }

    public PairingStatus (JSONObject json) throws JSONException {
    	super(json);
        
    	this.id = json.getString("id");

        JSONObject user = json.getJSONObject("user");
        this.userId = user.getString("id");
        this.userName = user.getString("name");

        this.enabled = json.getBoolean("enabled");
    }
}
