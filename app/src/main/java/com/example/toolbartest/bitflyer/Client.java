package com.example.toolbartest.bitflyer;

import android.app.Activity;

import com.example.toolbartest.bitflyer.data.Board;
import com.example.toolbartest.cryptocompare.request.BlockingRequest;

import org.json.JSONObject;

public class Client {
    private Activity activity;

    public Client(Activity activity) {
        this.activity = activity;
    }

    public Board getBoard() {
        String url = "https://api.bitflyer.jp/v1/getboard";
        JSONObject response = new BlockingRequest(activity, url).perform();
        return new Board(response);
    }

}
