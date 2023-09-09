package com.example.echoserver.validation;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonValidator {

    public static boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }
}
