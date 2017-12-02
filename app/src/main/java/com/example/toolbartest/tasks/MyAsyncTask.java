package com.example.toolbartest.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class MyAsyncTask extends AsyncTask<Integer, Integer, Integer> {
    private ArrayList<ApiResult> results;
    private CountDownLatch latch;
    private Activity activity;
    private ProgressDialog progressDialog;

    public MyAsyncTask(String[] urls, Activity activity) {
        results = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
            results.add(new ApiResult(urls[i]));
        }
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        Snackbar.make(getViewGroup(), "START", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        CountDownLatch latch = new CountDownLatch(results.size());

        for(ApiResult r : results){
            MyTaskThread task = new MyTaskThread(r, latch);
            task.start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return 200;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected void onPostExecute(Integer result) {
        Snackbar.make(getViewGroup(), "FINISHED " + results.get(0).getUrl(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private ViewGroup getViewGroup() {
        return (ViewGroup) ((ViewGroup) activity
                .findViewById(android.R.id.content)).getChildAt(0);
    }
}