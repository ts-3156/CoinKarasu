package com.coinkarasu.utils;

import com.coinkarasu.activities.CoinListFragment;
import com.coinkarasu.activities.etc.NavigationKind;

import java.util.Timer;
import java.util.TimerTask;

public class PeriodicalUpdater {
    private static final boolean DEBUG = true;
    private static final String TAG = "PeriodicalUpdater";

    private Timer timer;
    private CoinListFragment fragment;
    private NavigationKind kind;
    private int interval;
    private Log logger;

    public PeriodicalUpdater(CoinListFragment fragment, NavigationKind kind, int interval) {
        this.fragment = fragment;
        this.kind = kind;
        this.interval = interval;
        this.logger = new Log(fragment.getContext());
    }

    public void restart(String caller) {
        stop(caller);
        start(caller);
    }

    public void start(String caller) {
        if (timer != null) {
            return;
        }

        if (DEBUG) logger.d(TAG, "start() kind=" + kind + " caller=" + caller);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fragment.startTask();
            }
        }, 0, interval);
    }

    public void stop(String caller) {
        if (DEBUG) logger.d(TAG, "stop() kind=" + kind + " caller=" + caller);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
