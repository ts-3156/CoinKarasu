package com.coinkarasu.utils;

import java.util.Timer;
import java.util.TimerTask;

public class PeriodicalUpdater {
    private static final boolean DEBUG = true;
    private static final String TAG = "PeriodicalUpdater";

    private Timer timer;
    private PeriodicallyRunnable runnable;
    private int interval;
    private long lastUpdated;

    public PeriodicalUpdater(PeriodicallyRunnable runnable, int interval) {
        this.runnable = runnable;
        this.interval = interval;
    }

    public void restart(String caller) {
        stop(caller);
        start(caller);
    }

    public void start(String caller) {
        if (timer != null) {
            return;
        }

        long delay = Math.max(interval - (System.currentTimeMillis() - lastUpdated), 0);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                lastUpdated = System.currentTimeMillis();
                runnable.startTask();
            }
        }, delay, interval);
    }

    public void stop(String caller) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public interface PeriodicallyRunnable {
        void startTask();
    }
}
