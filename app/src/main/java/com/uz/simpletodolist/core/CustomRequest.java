package com.uz.simpletodolist.core;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by UltimateZero on 12/26/2016.
 */
public class CustomRequest extends Request<JsonResponse> {
    private final ApiListener listener;
    private final Map<String, String> headers;
    private final Map<String, String> params;
    private final JSONObject jsonObject;
    public CustomRequest(int method, String url, Map<String, String> headers,
                         Map<String, String> params, final ApiListener listener) {

        super(method, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error);
            }
        });
        this.listener = listener;
        this.headers = headers;
        this.params = params;
        jsonObject = null;
    }

    public CustomRequest(int method, String url, Map<String, String> headers,
                         JSONObject body, final ApiListener listener) {
        super(method, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error);
            }
        });

        this.listener = listener;
        this.headers = headers;
        params = null;
        jsonObject = body;
    }

    public CustomRequest(int method, String url, Map<String, String> headers,
                         final ApiListener listener) {
        super(method, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error);
            }
        });

        this.listener = listener;
        this.headers = headers;
        params = null;
        jsonObject = null;
    }


    @Override
    protected Response<JsonResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            JSONArray result = null;
            if(!json.trim().isEmpty()) {


                //try to parse as an array, or create a new array and add the object to it
                try {
                    result = new JSONArray(json);
                } catch (JSONException e) {
                    result = new JSONArray();
                    result.put(new JSONObject(json));
                }
            }

            return Response.success(
                    new JsonResponse(result, response.headers),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }

    }

    @Override
    public Map<String, String> getParams() throws AuthFailureError {
        return params != null ? params : super.getParams();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if(jsonObject == null)
            return super.getBody();
        return jsonObject.toString().getBytes();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return params == null ?
                 "application/json" : "application/x-www-form-urlencoded; charset=UTF-8";
    }

    @Override
    protected void deliverResponse(JsonResponse response) {
        listener.onResponse(response);
    }
}
