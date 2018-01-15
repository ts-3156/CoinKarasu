package com.coinkarasu.api.cryptocompare.response;

import com.coinkarasu.api.cryptocompare.data.History;
import com.coinkarasu.api.cryptocompare.data.HistoryImpl;
import com.coinkarasu.utils.CKLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HistoryResponseImpl implements HistoryResponse {
    private static final boolean DEBUG = true;
    private static final String TAG = "HistoryResponseImpl";

    public enum HistoryKind {
        minute(TimeUnit.MINUTES.toMillis(1)),
        hour(TimeUnit.HOURS.toMillis(1)),
        day(TimeUnit.DAYS.toMillis(1));

        public long expires;

        HistoryKind(long expires) {
            this.expires = expires;
        }
    }

    private JSONObject response;

    private String fromSymbol;
    private String toSymbol;

    public HistoryResponseImpl(JSONObject response, String fromSymbol, String toSymbol) {
        this.response = response;

        this.fromSymbol = fromSymbol;
        this.toSymbol = toSymbol;
    }

    @Override
    public JSONArray getData() {
        if (response == null) {
            if (DEBUG) CKLog.w(TAG, "getData() response is null.");
            return null;
        }

        JSONArray data = null;

        try {
            data = response.getJSONArray("Data");
        } catch (JSONException e) {
            CKLog.e(TAG, response.toString(), e);
        }

        return data;
    }

    @Override
    public List<History> getHistories() {
        JSONArray data = getData();
        if (data == null) {
            if (DEBUG) CKLog.w(TAG, "getHistories() data is null");
            return null;
        }

        List<History> histories = new ArrayList<>();

        try {
            for (int i = 0; i < data.length(); i++) {
                histories.add(new HistoryImpl(data.getJSONObject(i), fromSymbol, toSymbol));
            }
        } catch (JSONException e) {
            CKLog.e(TAG, data.toString(), e);
            histories = null;
        }

        return histories;
    }
}
