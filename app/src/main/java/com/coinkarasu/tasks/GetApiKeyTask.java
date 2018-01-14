package com.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.coinkarasu.api.coinkarasu.Client;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.Token;
import com.coinkarasu.utils.UuidUtils;

public class GetApiKeyTask extends AsyncTask<Integer, Integer, Integer> {
    private static final boolean DEBUG = true;
    private static final String TAG = "GetApiKeyTask";

    private Context context;

    public GetApiKeyTask(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        if (DEBUG) CKLog.d(TAG, "doInBackground()");
        String uuid = UuidUtils.getOrGenerateIfBlank(context);
        Token token = new Client(context).requestApiKey(uuid);
        if (token != null) {
            ApiKeyUtils.save(context, token);
        }
        context = null;

        return 200;
    }
}