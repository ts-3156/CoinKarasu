package com.coinkarasu.chart;

import android.text.TextUtils;

import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

public class Entry {
    private static final boolean DEBUG = true;
    private static final String TAG = "Entry";

    public float value;
    public String label;

    public Entry(double value, String label) {
        this.value = (float) value;
        this.label = label;
    }

    public static Entry buildBy(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }

        Entry entry = null;

        try {
            JSONObject json = new JSONObject(str);
            entry = new Entry(json.getDouble("value"), json.getString("label"));
        } catch (JSONException e) {
            CKLog.e(TAG, str, e);
        }

        return entry;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("value", value);
            json.put("label", label);
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }
        return json.toString();
    }
}
