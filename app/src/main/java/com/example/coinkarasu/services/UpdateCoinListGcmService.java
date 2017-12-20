package com.example.coinkarasu.services;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.coinkarasu.cryptocompare.data.CoinList;
import com.example.coinkarasu.cryptocompare.data.CoinListImpl;
import com.example.coinkarasu.cryptocompare.request.BlockingRequest;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import org.json.JSONObject;

public class UpdateCoinListGcmService extends GcmTaskService {

    private static final String TAG = UpdateCoinListGcmService.class.getSimpleName();

    public UpdateCoinListGcmService() {
    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Bundle extras = taskParams.getExtras();

        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                String url = "https://www.cryptocompare.com/api/data/coinlist/";
                JSONObject response = new BlockingRequest(UpdateCoinListGcmService.this, url).perform();
                CoinList coinList = CoinListImpl.buildByResponse(response);

            }
        });

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    public static void schedule(Context context) {
        Bundle data = new Bundle();
        data.putString("some key", "some budle data");

        try {
            PeriodicTask periodic = new PeriodicTask.Builder()
                    .setService(UpdateCoinListGcmService.class)
                    .setPeriod(60)
                    .setFlex(30)
                    .setTag(TAG)
                    .setPersisted(true)
                    .setUpdateCurrent(true)
                    .setRequiredNetwork(Task.NETWORK_STATE_ANY)
                    .setRequiresCharging(false)
                    .setExtras(data)
                    .build();
            GcmNetworkManager.getInstance(context).schedule(periodic);
            Log.e(TAG, "repeating task scheduled");
        } catch (Exception e) {
            Log.e(TAG, "scheduling failed");
            Log.e(TAG, e.getMessage());
        }
    }

    public static void cancel(Context context, String tag) {
        GcmNetworkManager.getInstance(context).cancelTask(tag, UpdateCoinListGcmService.class);
    }

    public static void cancelAll(Context context) {
        GcmNetworkManager.getInstance(context).cancelAllTasks(UpdateCoinListGcmService.class);
    }
}