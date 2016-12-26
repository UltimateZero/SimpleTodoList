package com.uz.simpletodolist.core;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.uz.simpletodolist.core.Constants;
import com.uz.simpletodolist.model.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by UltimateZero on 12/26/2016.
 */
public class ConnectionManager  {
    private static ConnectionManager instance;
    private static Context context;
    private static String TAG = "ConnectionManager";
    public static synchronized ConnectionManager getInstance() {

        if(instance == null){
            instance = new ConnectionManager();
        }
        return  instance;
    }

    public static void setContext(Context ctx) {
        if(context == null)
             context = ctx.getApplicationContext();
    }



    RequestQueue queue;

    private String uid;
    private String client;
    private String accessToken;
    private Map<String, String> defaultHeaders;

    private ConnectionManager() {
        NukeSSLCerts.nuke();
        queue = Volley.newRequestQueue(context, new ProxiedHurlStack());

    }

    public void login(final String email, final String password,
                      final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {

        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        CustomRequest request = new CustomRequest(Request.Method.POST,
                Constants.BASE_URL + Constants.SIGN_IN,
                null, params,
                new Response.Listener<JsonResponse>() {
                    @Override
                    public void onResponse(JsonResponse response) {
                        Log.d(TAG, response.jsonArray.toString());
                        uid = response.headers.get("Uid");
                        accessToken = response.headers.get("Access-Token");
                        client = response.headers.get("Client");
                        defaultHeaders = new HashMap<>();
                        defaultHeaders.put("Uid", uid);
                        defaultHeaders.put("Client", client);
                        defaultHeaders.put("Access-Token", accessToken);

                        try {
                            JSONObject result = response.jsonArray.getJSONObject(0);
                            listener.onResponse(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorListener.onErrorResponse(error);
                    }
                }
        );

        queue.add(request);


    }


    public void register(String email, String password, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        params.put("confirmation-password", password);
        CustomRequest request = new CustomRequest(Request.Method.POST,
                Constants.BASE_URL + Constants.AUTH,
                null, params,
                new Response.Listener<JsonResponse>() {
                    @Override
                    public void onResponse(JsonResponse response) {
                        Log.d(TAG, response.jsonArray.toString());

                        try {
                            JSONObject result = response.jsonArray.getJSONObject(0);
                            listener.onResponse(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorListener.onErrorResponse(error);
                    }
                }
        );

        queue.add(request);
    }

    public void deleteTask(int taskId, final Response.Listener<String> listener, final Response.ErrorListener errorListener) {
        CustomRequest request = new CustomRequest(Request.Method.DELETE,
                Constants.BASE_URL + Constants.NOTES + "/" + taskId,
                defaultHeaders,
                new Response.Listener<JsonResponse>() {
                    @Override
                    public void onResponse(JsonResponse response) {
                        Log.d(TAG, ""+response.jsonArray);

                            listener.onResponse(""+response.jsonArray);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorListener.onErrorResponse(error);
                    }
                }
        );

        queue.add(request);
    }

    public void updateTask(Task task, final Response.Listener<Task> listener, final Response.ErrorListener errorListener) {
        JSONObject bodyObject = new JSONObject();
        try {
            bodyObject.put("title", task.getTitle());
            bodyObject.put("body", task.getBody());
            bodyObject.put("done", task.isDone());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomRequest request = new CustomRequest(Request.Method.PUT,
                Constants.BASE_URL + Constants.NOTES + "/" + task.getId(),
                defaultHeaders, bodyObject,
                new Response.Listener<JsonResponse>() {
                    @Override
                    public void onResponse(JsonResponse response) {
                        Log.d(TAG, response.jsonArray.toString());


                        try {
                            JSONObject jsonObject = response.jsonArray.getJSONObject(0);
                            Task task = new Task();
                            task.setId(jsonObject.getInt("id"));
                            task.setTitle(jsonObject.getString("title"));
                            task.setBody(jsonObject.getString("body"));
                            task.setDone(jsonObject.getBoolean("done"));
                            task.setCreatedAt(jsonObject.getString("created_at"));
                            listener.onResponse(task);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorListener.onErrorResponse(error);
                    }
                }
        );

        queue.add(request);
    }

    public void markTask(int taskId, final Response.Listener<Task> listener, final Response.ErrorListener errorListener) {
        JSONObject bodyObject = new JSONObject();
        try {
            bodyObject.put("done", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CustomRequest request = new CustomRequest(Request.Method.POST,
                Constants.BASE_URL + Constants.NOTES + "/" + taskId + "/done",
                defaultHeaders, bodyObject,
                new Response.Listener<JsonResponse>() {
                    @Override
                    public void onResponse(JsonResponse response) {
                        Log.d(TAG, response.jsonArray.toString());


                        try {
                            JSONObject jsonObject = response.jsonArray.getJSONObject(0);
                            Task task = new Task();
                            task.setId(jsonObject.getInt("id"));
                            task.setTitle(jsonObject.getString("title"));
                            task.setBody(jsonObject.getString("body"));
                            task.setDone(jsonObject.getBoolean("done"));
                            task.setCreatedAt(jsonObject.getString("created_at"));
                            listener.onResponse(task);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorListener.onErrorResponse(error);
                    }
                }
        );

        queue.add(request);
    }

    public void addTask(String taskTitle, String taskBody, final Response.Listener<Task> listener, final Response.ErrorListener errorListener) {

        JSONObject bodyObject = new JSONObject();
        try {
            bodyObject.put("title", taskTitle);
            bodyObject.put("body", taskBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomRequest request = new CustomRequest(Request.Method.POST,
                Constants.BASE_URL + Constants.NOTES,
                defaultHeaders, bodyObject,
                new Response.Listener<JsonResponse>() {
                    @Override
                    public void onResponse(JsonResponse response) {
                        Log.d(TAG, response.jsonArray.toString());


                        try {
                            JSONObject jsonObject = response.jsonArray.getJSONObject(0);
                            Task task = new Task();
                            task.setId(jsonObject.getInt("id"));
                            task.setTitle(jsonObject.getString("title"));
                            task.setBody(jsonObject.getString("body"));
                            task.setDone(jsonObject.getBoolean("done"));
                            task.setCreatedAt(jsonObject.getString("created_at"));
                            listener.onResponse(task);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorListener.onErrorResponse(error);
                    }
                }
        );

        queue.add(request);
    }

    public void getTasks(final Response.Listener<List<Task>> listener, final Response.ErrorListener errorListener) {


        CustomRequest request = new CustomRequest(Request.Method.GET,
                Constants.BASE_URL + Constants.NOTES,
                defaultHeaders,
                new Response.Listener<JsonResponse>() {
                    @Override
                    public void onResponse(JsonResponse response) {
                        Log.d(TAG, response.jsonArray.toString());
                        List<Task> tasks = new ArrayList<>();
                        for(int i = 0; i < response.jsonArray.length(); i++) {
                            try {
                                JSONObject jsonObject = response.jsonArray.getJSONObject(i);
                                Task task = new Task();
                                task.setId(jsonObject.getInt("id"));
                                task.setTitle(jsonObject.getString("title"));
                                task.setBody(jsonObject.getString("body"));
                                task.setDone(jsonObject.getBoolean("done"));
                                task.setCreatedAt(jsonObject.getString("created_at"));

                                tasks.add(task);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        listener.onResponse(tasks);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorListener.onErrorResponse(error);
                    }
                }
        );

        queue.add(request);

    }


}
  class NukeSSLCerts {
    protected static final String TAG = "NukeSSLCerts";

    public static void nuke() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }
}