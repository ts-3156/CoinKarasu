package com.coinkarasu.utils.safetynet;

import android.content.Context;
import android.text.TextUtils;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.api.coinkarasu.Client;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKDateUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.CryptoUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CKSafetyNet {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CKSafetyNet";
    private static final String API_KEY = BuildConfig.SAFETYNET_API_KEY;

    public static boolean verifyDevice(Context context, String uuid) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
            if (DEBUG) CKLog.w(TAG, "The CKSafetyNet Attestation API is NOT available.");
            return false;
        }

        byte[] nonce = CryptoUtils.HMAC_SHA256Encode(uuid + CKDateUtils.now()).getBytes();
        Task<SafetyNetApi.AttestationResponse> task = SafetyNet.getClient(context).attest(nonce, API_KEY);
        String jwsResult = null;

        try {
            SafetyNetApi.AttestationResponse response = Tasks.await(task, 5000, TimeUnit.MILLISECONDS);
            jwsResult = response.getJwsResult();
        } catch (ExecutionException e) {
            CKLog.e(TAG, e);
            if (e.getCause() instanceof ApiException) {
                ApiException apiException = (ApiException) e.getCause();
                if (DEBUG) CKLog.w(TAG, "Error: "
                        + CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": " + apiException.getMessage());
            }
        } catch (InterruptedException e) {
            CKLog.e(TAG, e);
        } catch (TimeoutException e) {
            CKLog.e(TAG, e);
        }

        if (TextUtils.isEmpty(jwsResult)) {
            return false;
        }

        Client client = new Client(context, ApiKeyUtils.getValidToken(context));
        if (!client.verifyJwt(new String(nonce), jwsResult)) {
            if (DEBUG) CKLog.w(TAG, "Failure: The cryptographic signature of the attestation statement couldn't be verified.");
            return false;
        }

        byte[] data = extractJwsData(jwsResult);
        if (data == null) {
            return false;
        }
        if (DEBUG) CKLog.d(TAG, "CKSafetyNet extracted json: " + new String(data));

        AttestationStatement statement = null;
        try {
            statement = new AttestationStatement(new JSONObject(new String(data)));
        } catch (JSONException e) {
            CKLog.e(TAG, e);
        }

        if (statement == null) {
            return false;
        }

        if (!statement.isCtsProfileMatch()
                || !Arrays.equals(statement.getNonce(), nonce)
                || statement.getTimestampMs() < CKDateUtils.now() - TimeUnit.SECONDS.toMillis(60)
                || !statement.getApkPackageName().equals(BuildConfig.APPLICATION_ID)) {
            return false;
        }

        return true;
    }

    private static byte[] extractJwsData(String jws) {
        String[] parts = jws.split("[.]");
        if (parts.length != 3) {
            return null;
        }
        return Base64.decodeBase64(parts[1]);
    }
}
