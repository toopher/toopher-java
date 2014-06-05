package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticationStatusFactoryMock extends AuthenticationStatusFactory {
    @Override
    public AuthenticationStatus create(JSONObject object) throws JSONException {
        return new AuthenticationStatus(null, false, false, false, null, null, null, null);
    }
}
