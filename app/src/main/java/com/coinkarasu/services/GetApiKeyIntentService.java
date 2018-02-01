package com.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.api.coinkarasu.Client;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.IntentServiceIntervalChecker;
import com.coinkarasu.utils.PrefHelper;
import com.coinkarasu.utils.Token;
import com.coinkarasu.utils.UuidUtils;
import com.coinkarasu.utils.safetynet.CKSafetyNet;

import java.util.concurrent.TimeUnit;

public class GetApiKeyIntentService extends IntentService {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "GetApiKeyIntentService";
    private static final long ONE_HOUR = TimeUnit.MINUTES.toMillis(60);

    public GetApiKeyIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (PrefHelper.isAirplaneModeOn(this)) {
            return;
        }

        if (ApiKeyUtils.exists(this)) {
            return;
        }

        if (!IntentServiceIntervalChecker.shouldRun(this, TAG, ONE_HOUR)) {
            return;
        }
        IntentServiceIntervalChecker.onStart(this, TAG);

        try {
            String uuid = UuidUtils.getOrGenerateIfBlank(this);
            if (CKSafetyNet.verifyDevice(this, uuid)) {
                Token token = new Client(this, ApiKeyUtils.getValidToken(this)).requestApiKey(uuid);

                if (token != null) {
                    ApiKeyUtils.save(this, token);
                }
            }
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }
    }

    public static void start(Context context) {
        context.startService(new Intent(context, GetApiKeyIntentService.class));
    }
}
