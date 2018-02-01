package com.coinkarasu.utils.safetynet;

import com.coinkarasu.utils.CKLog;
import com.google.api.client.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AttestationStatement {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "AttestationStatement";

    private byte[] nonce;
    private long timestampMs;
    private String apkPackageName;
    private byte[][] apkCertificateDigestSha256;
    private byte[] apkDigestSha256;
    private boolean ctsProfileMatch;
    private boolean basicIntegrity;

    public AttestationStatement(JSONObject json) {
        if (json == null) {
            return;
        }

        try {
            nonce = Base64.decodeBase64(json.getString("nonce"));
            timestampMs = json.getLong("timestampMs");
            apkPackageName = json.getString("apkPackageName");

            JSONArray certsArray = json.getJSONArray("apkCertificateDigestSha256");
            apkCertificateDigestSha256 = new byte[certsArray.length()][];
            for (int i = 0; i < certsArray.length(); i++) {
                apkCertificateDigestSha256[i] = Base64.decodeBase64(certsArray.getString(i));
            }

            apkDigestSha256 = Base64.decodeBase64(json.getString("apkDigestSha256"));
            ctsProfileMatch = json.getBoolean("ctsProfileMatch");
            basicIntegrity = json.getBoolean("basicIntegrity");
        } catch (JSONException e) {
            CKLog.e(TAG, json.toString(), e);
        }
    }

    public byte[] getNonce() {
        return nonce;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public String getApkPackageName() {
        return apkPackageName;
    }

    public byte[] getApkDigestSha256() {
        return apkDigestSha256;
    }

    public byte[][] getApkCertificateDigestSha256() {
        return apkCertificateDigestSha256;
    }

    public boolean isCtsProfileMatch() {
        return ctsProfileMatch;
    }

    public boolean hasBasicIntegrity() {
        return basicIntegrity;
    }
}
