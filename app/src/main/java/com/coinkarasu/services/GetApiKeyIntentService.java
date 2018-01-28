package com.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.api.coinkarasu.Client;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.CryptoUtils;
import com.coinkarasu.utils.PrefHelper;
import com.coinkarasu.utils.Token;
import com.coinkarasu.utils.UuidUtils;
import com.coinkarasu.utils.safetynet.AttestationStatement;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GetApiKeyIntentService extends IntentService {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "GetApiKeyIntentService";
    private static final long ONE_HOUR = TimeUnit.MINUTES.toMillis(60);
    private static final String API_KEY = BuildConfig.SAFETYNET_API_KEY;

    public GetApiKeyIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (PrefHelper.isAirplaneModeOn(this)) {
            return;
        }

        try {
            String uuid = UuidUtils.getOrGenerateIfBlank(this);
            sendSafetyNetRequest(uuid);
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }
    }

    private void sendSafetyNetRequest(String uuid) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            if (DEBUG) CKLog.w(TAG, "The SafetyNet Attestation API is NOT available.");
            return;
        }

        byte[] nonce = CryptoUtils.HMAC_SHA256Encode(uuid + System.currentTimeMillis()).getBytes();
        Task<SafetyNetApi.AttestationResponse> task = SafetyNet.getClient(this).attest(nonce, API_KEY);
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
            return;
        }

        Client client = new Client(this);
        if (!client.verifyJwt(new String(nonce), jwsResult)) {
            if (DEBUG) CKLog.w(TAG, "Failure: The cryptographic signature of the attestation statement couldn't be verified.");
            return;
        }

        byte[] data = extractJwsData(jwsResult);
        AttestationStatement statement = null;

        try {
            statement = new JacksonFactory().fromInputStream(new ByteArrayInputStream(data), AttestationStatement.class);
        } catch (IOException e) {
            CKLog.e(TAG, e);
        }

        if (statement == null) {
            return;
        }

        if (DEBUG) CKLog.d(TAG, "SafetyNet extracted json: " + statement.toString());

        if (!statement.isCtsProfileMatch()
                || !Arrays.equals(statement.getNonce(), nonce)
                || statement.getTimestampMs() < System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60)
                || !statement.getApkPackageName().equals(BuildConfig.APPLICATION_ID)) {
            return;
        }

        Token token = client.requestApiKey(uuid);

        if (token != null) {
            ApiKeyUtils.save(this, token);
        }
    }

    private static byte[] extractJwsData(String jws) {
        String[] parts = jws.split("[.]");
        if (parts.length != 3) {
            return null;
        }
        return Base64.decodeBase64(parts[1]);
    }

    private String logFile() {
        return TAG + ".log";
    }

    public static void start(Context context) {
        context.startService(new Intent(context, GetApiKeyIntentService.class));
    }
}
