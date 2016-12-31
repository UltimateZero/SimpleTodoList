package com.uz.simpletodolist.core;

import com.android.volley.VolleyError;

/**
 * Created by UltimateZero on 12/31/2016.
 */

public abstract class AsyncVolleyRequest<T> {
    public abstract void onResponse(T response);
    public abstract void onError(VolleyError error);
}
