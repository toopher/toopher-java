package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides information about the status of a user terminal
 */
public class UserTerminal extends ApiResponseObject {
    /**
     * The {@link com.toopher.ToopherApi} associated with this user terminal
     */
    public ToopherApi api;

    /**
     * The unique id for the user terminal
     */
    public String id;

    /**
     * The name of the user terminal
     */
    public String name;

    /**
     * The unique id specified by the requester
     */
    public String requesterSpecifiedId;

    /**
     * The {@link com.toopher.User} object associated with the user terminal
     */
    public User user;

    public UserTerminal(JSONObject json, ToopherApi toopherApi) throws JSONException {
        super(json);

        this.api = toopherApi;
        this.id = json.getString("id");
        this.name = json.getString("name");
        this.requesterSpecifiedId = json.getString("requester_specified_id");
        this.user = new User(json.getJSONObject("user"), toopherApi);
    }

    @Override
    public String toString() {
        return String.format("[UserTerminal: id=%s; name=%s; requesterSpecifiedId=%s; userName=%s; userId=%s; userToopherAuthenticationEnabled=%b]",
                id, name, requesterSpecifiedId, user.name, user.id, user.toopherAuthenticationEnabled);
    }

    /**
     * Update the UserTerminal object with JSON response from the server
     *
     * @throws RequestError Thrown when an exceptional condition is encountered
     */
    public void refreshFromServer() throws RequestError, JSONException {
        String endpoint = String.format("user_terminals/%s", id);
        JSONObject result = api.advanced.raw.get(endpoint);
        update(result);
    }

    /**
     * Update the UserTerminal object with JSON response
     *
     * @param jsonResponse The JSON response received from the server
     */
    public void update(JSONObject jsonResponse) throws JSONException {
        this.name = jsonResponse.getString("name");
        this.requesterSpecifiedId = jsonResponse.getString("requester_specified_id");
        this.user.update(jsonResponse.getJSONObject("user"));
        this.updateRawResponse(jsonResponse);
    }
}
