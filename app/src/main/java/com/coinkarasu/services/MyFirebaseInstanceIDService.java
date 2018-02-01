package com.coinkarasu.services;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.api.coinkarasu.Client;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.UuidUtils;
import com.coinkarasu.utils.safetynet.CKSafetyNet;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        String uuid = UuidUtils.getOrGenerateIfBlank(this);

        if (DEBUG) CKLog.d(TAG, "onTokenRefresh() Refreshed token: " + refreshedToken);
        if (DEBUG) CKLog.d(TAG, "onTokenRefresh() uuid: " + uuid);

        try {
            if (BuildConfig.DEBUG || CKSafetyNet.verifyDevice(this, uuid)) {
                new Client(this, ApiKeyUtils.getValidToken(this)).sendNotificationToken(uuid, refreshedToken);
            }
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }
    }
}