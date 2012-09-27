package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticationStatus {
    public String id;
    public boolean pending;
    public boolean granted;
    public boolean automated;
    public String reason;
    public String terminalId;
    public String terminalName;

    @Override
    public String toString() {
        return String.format("[AuthenticationStatus: id=%s; pending=%b; granted=%b; automated=%b; reason=%s; terminalId=%s; terminalName=%s]",
                             id, pending, granted, automated, reason, terminalId, terminalName);
    }

    static AuthenticationStatus fromJSON(JSONObject json) throws JSONException {
        AuthenticationStatus as = new AuthenticationStatus();
        as.id = json.getString("id");
        as.pending = json.getBoolean("pending");
        as.granted = json.getBoolean("granted");
        as.automated = json.getBoolean("automated");
        as.reason = json.getString("reason");

        JSONObject terminal = json.getJSONObject("terminal");
        as.terminalId = terminal.getString("id");
        as.terminalName = terminal.getString("name");

        return as;
    }
}
