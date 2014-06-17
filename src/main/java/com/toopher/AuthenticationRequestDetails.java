package com.toopher;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthenticationRequestDetails extends ApiRequestDetails {
    public AuthenticationRequestDetails(List<NameValuePair> params, Map<String, String> extras) {
        super(params, extras);
    }

    public static class Builder {
        private List<NameValuePair> params = new ArrayList<NameValuePair>();
        private Map<String, String> extras = new HashMap<String, String>();

        public Builder() {
            this(null);
        }

        public Builder(Map<String, String> extras) {
            this.extras = extras == null ? new HashMap<String, String>() : extras;
        }

        public Builder addExtra(String key, String value) {
            extras.put(key, value);
            return this;
        }

        public Builder setActionName(String actionName) {
            if (actionName != null && actionName.length() > 0) {
                params.add(new BasicNameValuePair("action_name", actionName));
            }
            return this;
        }

        public Builder setPairingId(String pairingId) {
            if (pairingId != null) {
                params.add(new BasicNameValuePair("pairing_id", pairingId));
            }
            return this;
        }

        public Builder setTerminalName(String terminalName) {
            if (terminalName != null) {
                params.add(new BasicNameValuePair("terminal_name", terminalName));
            }
            return this;
        }

        public Builder setTerminalNameExtra(String terminalNameExtra) {
            return addExtra("terminal_name_extra", terminalNameExtra);
        }

        public Builder setUserName(String userName) {
            return addExtra("user_name", userName);
        }

        public AuthenticationRequestDetails build() {
            return new AuthenticationRequestDetails(params, extras);
        }
    }
}
