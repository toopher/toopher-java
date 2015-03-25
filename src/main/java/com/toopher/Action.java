package com.toopher;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provide information about the status of an action
 */
public class Action extends ApiResponseObject {
    /**
     * The unique id for the action
     */
    public String id;

    /**
     * The name of the action
     */
    public String name;

    public Action(JSONObject json) throws JSONException {
        super(json);

        this.id = json.getString("id");
        this.name = json.getString("name");
    }

    /**
     * Update the Action object with JSON response
     *
     * @param jsonResponse The JSON response from the API
     * @throws org.json.JSONException Thrown by the JSON.org classes when an exceptional condition is encountered
     */
    public void update(JSONObject jsonResponse) throws JSONException {
        this.name = jsonResponse.getString("name");
        this.updateRawResponse(jsonResponse);
    }
}
