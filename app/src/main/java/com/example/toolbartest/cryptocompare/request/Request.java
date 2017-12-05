package com.example.toolbartest.cryptocompare.request;

import org.json.JSONObject;

public interface Request {

    JSONObject perform();

    void perform(Listener listener);

    interface Listener {
        void finished(JSONObject response);
    }
}
