package com.example.coinkarasu.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.example.coinkarasu.api.cryptocompare.request.BlockingRequest;

import org.json.JSONObject;

public class FetchCoinListTask extends AsyncTask<Integer, Integer, Integer> {
    private Listener listener;
    private JSONObject response;
    private Activity activity;

    public FetchCoinListTask(Activity activity) {
        this.activity = activity;
        listener = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        String url = "https://www.cryptocompare.com/api/data/coinlist/";
        response = new BlockingRequest(activity, url).perform();
        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            listener.finished(response);
        }
    }

    public FetchCoinListTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(JSONObject response);
    }
}