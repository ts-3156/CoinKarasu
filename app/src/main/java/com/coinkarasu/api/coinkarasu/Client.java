package com.coinkarasu.api.coinkarasu;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.coinkarasu.BuildConfig;
import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.api.cryptocompare.request.BlockingRequest;
import com.coinkarasu.utils.CKDateUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.CryptoUtils;
import com.coinkarasu.utils.PrefHelper;
import com.coinkarasu.utils.Token;
import com.coinkarasu.utils.volley.RequestQueueWrapper;
import com.coinkarasu.utils.volley.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Client {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "Client";
    private static final String HOST = BuildConfig.CK_HOST;
    private static final long ONE_DAY = 60 * 60 * 24;

    private RequestQueueWrapper requestQueue;
    private String apiKey;
    private String apiSecret;
    private String host;

    public Client(Context context) {
        this(context, "", "");
    }

    public Client(Context context, String apiKey, String apiSecret) {
        this.requestQueue = VolleyHelper.getInstance(context).getWrappedRequestQueue();
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.host = BuildConfig.DEBUG ? PrefHelper.getCkHost(context, HOST) : HOST;
    }

    public Rate getSalesRate(String fromSymbol, String toSymbol) {
        String url = host + "/coincheck/sales_rates?"
                + "time=" + ((CKDateUtils.now() / 1000) - ONE_DAY)
                + "&from_symbol=" + fromSymbol
                + "&to_symbol=" + toSymbol;

        JSONObject response = new BlockingRequest(requestQueue, url, createHeader(url)).perform(Request.Method.GET);
        if (response == null) {
            return null;
        }

        Rate rate = new Rate(fromSymbol, toSymbol);
        try {
            rate.value = response.getDouble("rate");
        } catch (JSONException e) {
            CKLog.e(TAG, url + " " + response.toString(), e);
        }

        return rate;
    }

    public Rate getTradingRate(String fromSymbol, String toSymbol) {
        String url = host + "/coincheck/trading_rates?"
                + "time=" + ((CKDateUtils.now() / 1000) - ONE_DAY)
                + "&from_symbol=" + fromSymbol
                + "&to_symbol=" + toSymbol;

        JSONObject response = new BlockingRequest(requestQueue, url, createHeader(url)).perform(Request.Method.GET);
        if (response == null) {
            return null;
        }

        Rate rate = new Rate(fromSymbol, toSymbol);
        try {
            rate.value = response.getDouble("rate");
        } catch (JSONException e) {
            CKLog.e(TAG, url + " " + response.toString(), e);
        }

        return rate;
    }

    public Token requestApiKey(String uuid) {
        String url = host + "/apps?uuid=" + uuid;
        JSONObject response = new BlockingRequest(requestQueue, url).perform(Request.Method.POST);
        if (response == null) {
            if (DEBUG) CKLog.w(TAG, "requestApiKey() response is null " + uuid);
            return null;
        }

        if (!response.has("key") || !response.has("secret")) {
            return null;
        }

        String key = null, secret = null;
        try {
            key = response.getString("key");
            secret = response.getString("secret");
        } catch (JSONException e) {
            CKLog.e(TAG, url, e);
        }

        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(secret)) {
            if (DEBUG) CKLog.w(TAG, "requestApiKey() key or secret is null " + response.toString());
            return null;
        }

        Token token = new Token(key, secret);
        if (DEBUG) CKLog.d(TAG, "requestApiKey() " + uuid + " " + token.toString());

        return token;
    }

    public boolean verifyJwt(String nonce, String jwt) {
        String url = host + "/json_web_tokens/verify";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("nonce", nonce);
            requestBody.put("token", jwt);
        } catch (JSONException e) {
            CKLog.e(TAG, e);
            return false;
        }

        JSONObject response = new BlockingRequest(requestQueue, url).perform(Request.Method.POST, requestBody);
        try {
            return response != null && response.has("is_valid_signature") && response.getBoolean("is_valid_signature");
        } catch (JSONException e) {
            CKLog.e(TAG, e);
            return false;
        }
    }

    private Map<String, String> createHeader(String url) {
        Map<String, String> map = new HashMap<>();
        String nonce = String.valueOf(CKDateUtils.now() / 1000L);
        map.put("Access-Key", apiKey);
        map.put("Access-Nonce", nonce);
        map.put("Access-Signature", createSignature(apiSecret, nonce, url));
        return map;
    }

    private String createSignature(String apiSecret, String nonce, String url) {
        String message = nonce + url;
        return CryptoUtils.HMAC_SHA256Encode(apiSecret, message);
    }
}
