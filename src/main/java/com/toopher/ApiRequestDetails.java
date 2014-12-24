package com.toopher;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Map;

public abstract class ApiRequestDetails {
    private List<NameValuePair> params;
    private Map<String, String> extras;

    public ApiRequestDetails(List<NameValuePair> params, Map<String, String> extras) {
        this.params = params;
        this.extras = extras;
    }

    public List<NameValuePair> getParams() { return params; }
    public Map<String, String> getExtras() { return extras; }
}
