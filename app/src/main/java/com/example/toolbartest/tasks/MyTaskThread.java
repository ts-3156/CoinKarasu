package com.example.toolbartest.tasks;

import android.util.Log;

import java.util.concurrent.CountDownLatch;

public class MyTaskThread extends Thread {
    private ApiResult result;
    private CountDownLatch latch;

    public MyTaskThread(ApiResult result, CountDownLatch latch) {
        this.result = result;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            Log.d("URL", result.getUrl());
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        latch.countDown();
    }
}