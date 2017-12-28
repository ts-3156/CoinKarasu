package com.example.coinkarasu.api.bitflyer;

import android.content.Context;

import com.example.coinkarasu.api.bitflyer.data.Board;
import com.example.coinkarasu.api.cryptocompare.request.BlockingRequest;

import org.json.JSONObject;

public class Client {
    private Context context;

    public Client(Context context) {
        this.context = context;
    }

    public Board getBoard() {
        String url = "https://api.bitflyer.jp/v1/getboard";
        JSONObject response = new BlockingRequest(context, url).perform();
        return new Board(response);
    }
}
