package com.uz.simpletodolist.core;

import com.android.volley.VolleyError;

/**
 * Created by UltimateZero on 12/31/2016.
 */

public abstract class ApiListener {

    public abstract void onResponse(JsonResponse response);
    public abstract void onError(VolleyError error);

}
