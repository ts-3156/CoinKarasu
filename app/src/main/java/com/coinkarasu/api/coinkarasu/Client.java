package com.coinkarasu.api.coinkarasu;

import android.text.TextUtils;
import android.util.Pair;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.api.coincheck.data.Rate;
import com.coinkarasu.utils.CKLog;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    private static final boolean DEBUG = true;
    private static final String TAG = "Client";
    private static final String HOST = BuildConfig.CK_HOST;
    private static final long ONE_DAY = 60 * 60 * 24;

    private String apiKey;
    private String apiSecret;

    public Client() {
        this.apiKey = "";
        this.apiSecret = "";
    }

    public Client(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public Rate getSalesRate(String fromSymbol, String toSymbol) {
        String url = HOST + "/coincheck/sales_rates?"
                + "time=" + ((System.currentTimeMillis() / 1000) - ONE_DAY)
                + "&from_symbol=" + fromSymbol
                + "&to_symbol=" + toSymbol;

        String jsonString = requestByUrlWithHeader(url, "GET", createHeader(url));
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }

        JSONObject response = null;
        try {
            response = new JSONObject(jsonString);
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }
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

        String jsonString = requestByUrlWithHeader(url, "GET", createHeader(url));
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }

        JSONObject response = null;
        try {
            response = new JSONObject(jsonString);
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }
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
        String jsonString = requestByUrl(url, "POST");
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }

        JSONObject response = null;
        String key = null, secret = null;
        try {
            response = new JSONObject(jsonString);
            key = response.getString("key");
            secret = response.getString("secret");
        } catch (JSONException e) {
            CKLog.e(TAG, url, e);
        }

        if (response == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(secret)) {
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

    private static String requestByUrlWithHeader(String url, String method, final Map<String, String> headers) {
        CKLog.d(TAG, url);

        ApacheHttpTransport transport = new ApacheHttpTransport();
        HttpRequestFactory factory = transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(final HttpRequest request) throws IOException {
                request.setConnectTimeout(0);
                request.setReadTimeout(0);
                request.setParser(new JacksonFactory().createJsonObjectParser());

                final HttpHeaders httpHeaders = new HttpHeaders();
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    httpHeaders.set(e.getKey(), e.getValue());
                }
                request.setHeaders(httpHeaders);
            }
        });

        String jsonString;
        try {
            HttpRequest request;
            if (method.equals("POST")) {
                request = factory.buildPostRequest(new GenericUrl(url), null);
            } else {
                request = factory.buildGetRequest(new GenericUrl(url));
            }

            HttpResponse response = request.execute();
            jsonString = response.parseAsString();
        } catch (IOException e) {
            CKLog.e(TAG, url, e);
            jsonString = null;
        }

        return jsonString;
    }

    private static String requestByUrl(String url, String method) {
        return requestByUrlWithHeader(url, method, new HashMap<String, String>());
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
