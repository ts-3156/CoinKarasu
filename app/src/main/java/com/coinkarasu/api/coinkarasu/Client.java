package com.coinkarasu.api.coinkarasu;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import com.android.volley.Request;
import com.coinkarasu.BuildConfig;
import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.api.cryptocompare.request.BlockingRequest;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.volley.RequestQueueWrapper;
import com.coinkarasu.utils.volley.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    private static final boolean DEBUG = true;
    private static final String TAG = "Client";
    private static final String HOST = BuildConfig.CK_HOST;
    private static final long ONE_DAY = 60 * 60 * 24;

    private RequestQueueWrapper requestQueue;
    private String apiKey;
    private String apiSecret;

    public Client(Context context) {
        this(VolleyHelper.getInstance(context).getWrappedRequestQueue(), "", "");
    }

    public Client(Context context, String apiKey, String apiSecret) {
        this(VolleyHelper.getInstance(context).getWrappedRequestQueue(), apiKey, apiSecret);
    }

    public Client(RequestQueueWrapper queue, String apiKey, String apiSecret) {
        this.requestQueue = queue;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public Rate getSalesRate(String fromSymbol, String toSymbol) {
        String url = HOST + "/coincheck/sales_rates?"
                + "time=" + ((System.currentTimeMillis() / 1000) - ONE_DAY)
                + "&from_symbol=" + fromSymbol
                + "&to_symbol=" + toSymbol;

        JSONObject response = requestByUrlWithHeader(url, Request.Method.GET, createHeader(url));
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
        String url = HOST + "/coincheck/trading_rates?"
                + "time=" + ((System.currentTimeMillis() / 1000) - ONE_DAY)
                + "&from_symbol=" + fromSymbol
                + "&to_symbol=" + toSymbol;

        JSONObject response = requestByUrlWithHeader(url, Request.Method.GET, createHeader(url));
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

    public Pair<String, String> requestApiKey(String uuid) {
        String url = HOST + "/apps?uuid=" + uuid;
        JSONObject response = requestByUrl(url, Request.Method.POST);
        if (response == null) {
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
            return null;
        }

        return Pair.create(key, secret);
    }

    private Map<String, String> createHeader(String url) {
        Map<String, String> map = new HashMap<>();
        String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
        map.put("Access-Key", apiKey);
        map.put("Access-Nonce", nonce);
        map.put("Access-Signature", createSignature(apiSecret, nonce, url));
        return map;
    }

    private String createSignature(String apiSecret, String nonce, String url) {
        String message = nonce + url;
        return HMAC_SHA256Encode(apiSecret, message);
    }

    private JSONObject requestByUrlWithHeader(String url, int method, final Map<String, String> headers) {
        return new BlockingRequest(requestQueue, url, headers).perform(method);
    }

    private JSONObject requestByUrl(String url, int method) {
        return requestByUrlWithHeader(url, method, Collections.<String, String>emptyMap());
    }

    private static String HMAC_SHA256Encode(String secretKey, String message) {

        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "hmacSHA256");

        Mac mac;
        try {
            mac = Mac.getInstance("hmacSHA256");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException e) {
            CKLog.e(TAG, e);
            return "";
        } catch (InvalidKeyException e) {
            CKLog.e(TAG, e);
            return "";
        }
        byte[] rawHmac = mac.doFinal(message.getBytes());

        return new String(encodeHex(rawHmac));
    }

    private static final char[] DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    // org.apache.commons.codec.binary.Hex.encodeHex
    private static char[] encodeHex(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }

        return out;
    }
}
