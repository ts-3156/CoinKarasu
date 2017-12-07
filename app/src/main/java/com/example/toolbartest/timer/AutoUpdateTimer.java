package com.example.toolbartest.timer;

import java.util.Timer;

public class AutoUpdateTimer extends Timer {
    String tag;

    public AutoUpdateTimer(String tag) {
        super();
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
