package com.coinkarasu.api.bitflyer;

import com.coinkarasu.utils.CKDateUtils;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class BitflyerPrivateApi {

    private static final String endpoint = "https://api.bitflyer.jp";

    private String apiKey;
    private String apiSecret;

    public BitflyerPrivateApi(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public String getExecutions(long maxId) {
        String path = "/v1/me/getexecutions";
        String query = "?product_code=BTC_JPY&count=100";
        if (maxId != -1) {
            query += "&after=" + maxId;
        }
        String jsonString = requestByUrlWithHeader(endpoint + path + query, createHeader(path + query, "GET"));
        return jsonString;
    }

    public String getBalance() {
        String path = "/v1/me/getbalance";
        String jsonString = requestByUrlWithHeader(endpoint + path, createHeader(path, "GET"));
        return jsonString;
    }

    private Map<String, String> createHeader(String path, String method) {
        Map<String, String> map = new HashMap<>();
        String timestamp = String.valueOf(CKDateUtils.now() / 1000L);
        map.put("ACCESS-KEY", apiKey);
        map.put("ACCESS-TIMESTAMP", timestamp);
        map.put("ACCESS-SIGN", createSignature(apiSecret, timestamp, method, path, ""));
        return map;
    }

    private String createSignature(String apiSecret, String timestamp, String method, String path, String body) {
        String message = timestamp + method + path + body;
        return HMAC_SHA256Encode(apiSecret, message);
    }

    private String requestByUrlWithHeader(String url, final Map<String, String> headers) {
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
            HttpRequest request = factory.buildGetRequest(new GenericUrl(url));
            HttpResponse response = request.execute();
            jsonString = response.parseAsString();
        } catch (IOException e) {
            e.printStackTrace();
            jsonString = null;
        }
        return jsonString;
    }


    public static String HMAC_SHA256Encode(String secretKey, String message) {

        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "hmacSHA256");

        Mac mac = null;
        try {
            mac = Mac.getInstance("hmacSHA256");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] rawHmac = mac.doFinal(message.getBytes());
//        return Hex.encodeHexString(rawHmac);
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
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4 ];
            out[j++] = DIGITS[ 0x0F & data[i] ];
        }

        return out;
    }
}
