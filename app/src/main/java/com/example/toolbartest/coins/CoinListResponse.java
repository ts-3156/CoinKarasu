package com.example.toolbartest.coins;

import android.app.Activity;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.toolbartest.utils.SnackbarHelper;
import com.example.toolbartest.utils.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

public interface CoinListResponse {
    JSONObject getResponse();

    JSONObject getData();

    boolean isSuccess();

    boolean saveToFile(Context context);
//    static boolean restoreFromFile(Context context);
//    static boolean cacheExists(Context context);
//    static Date lastModified(Context context);
}
