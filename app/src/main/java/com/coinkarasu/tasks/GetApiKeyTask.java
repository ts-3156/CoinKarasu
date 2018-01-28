package com.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.coinkarasu.api.coinkarasu.Client;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.Token;
import com.coinkarasu.utils.UuidUtils;

public class GetApiKeyTask extends AsyncTask<Context, Void, Void> {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "GetApiKeyTask";

    @Override
    protected Void doInBackground(Context... params) {
        Context context = params[0];
        String uuid = UuidUtils.getOrGenerateIfBlank(context);
        Token token = new Client(context).requestApiKey(uuid);

        if (token != null) {
            ApiKeyUtils.save(context, token);
        }

        return null;
    }
}
