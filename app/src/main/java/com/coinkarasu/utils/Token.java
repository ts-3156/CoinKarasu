package com.coinkarasu.utils;

import android.util.Pair;

public class Token {
    private Pair<String, String> pair;

    public Token(String key, String secret) {
        pair = Pair.create(key, secret);
    }

    public String getKey() {
        return pair.first;
    }

    public String getSecret() {
        return pair.second;
    }

    public String toString() {
        return "key=" + pair.first + " secret=" + pair.second;
    }
}
