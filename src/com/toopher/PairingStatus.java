package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

public class PairingStatus {
    public String id;
    public String userId;
    public String userName;
    public boolean enabled;

    @Override
    public String toString() {
        return String.format("[PairingStatus: id=%s; userId=%s; userName=%s, enabled=%b]", id,
                             userId, userName, enabled);
    }

    static PairingStatus fromJSON(JSONObject json) throws JSONException {
        PairingStatus ps = new PairingStatus();
        ps.id = json.getString("id");

        JSONObject user = json.getJSONObject("user");
        ps.userId = user.getString("id");
        ps.userName = user.getString("name");

        ps.enabled = json.getBoolean("enabled");

        return ps;
    }
}
