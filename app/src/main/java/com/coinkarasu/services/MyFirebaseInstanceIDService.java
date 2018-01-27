package com.coinkarasu.services;

import com.coinkarasu.utils.CKLog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (DEBUG) CKLog.d(TAG, "Refreshed token: " + refreshedToken);
    }
}