package com.coinkarasu.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import com.coinkarasu.api.coinkarasu.Client;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.UuidUtils;

public class GetApiKeyTask extends AsyncTask<Integer, Integer, Integer> {
    private Context context;

    public GetApiKeyTask(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        String uuid = UuidUtils.get(context);
        Pair<String, String> result = new Client().requestApiKey(uuid);
        if (result != null) {
            ApiKeyUtils.save(context, result.first, result.second);
        }
        context = null;

        return 200;
    }
}