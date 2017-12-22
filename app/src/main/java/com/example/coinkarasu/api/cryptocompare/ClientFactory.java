package com.example.coinkarasu.api.cryptocompare;

import android.content.Context;

public class ClientFactory {
    public static Client  getInstance(Context context) {
        return new ClientImpl(context);
    }
}
