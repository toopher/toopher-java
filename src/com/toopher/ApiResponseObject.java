package com.toopher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiResponseObject {
    /**
     * A map of the raw API response data
     */
    public Map raw;
    
    public ApiResponseObject(JSONObject json) throws JSONException {
    	this.raw = jsonToMap(json);
    }

    private Map<String, Object> jsonToMap(JSONObject json) throws JSONException{
    	Map<String,Object> result = new HashMap<String,Object>();
    	
    	for (Iterator<String> i = json.keys(); i.hasNext(); ) {
    		String key = i.next();
    		Object o = json.get(key);
    		result.put(key, o);
    	}
    	
    	return result;
    }

}
