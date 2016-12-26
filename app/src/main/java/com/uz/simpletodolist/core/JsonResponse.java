package com.uz.simpletodolist.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by UltimateZero on 12/26/2016.
 */
public class JsonResponse {
    public final JSONArray jsonArray;
    public final Map<String, String> headers;

    public JsonResponse(JSONArray jsonArray, Map<String, String> headers) {
        this.jsonArray = jsonArray;
        this.headers = headers;
    }
}
