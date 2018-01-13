package com.coinkarasu.utils;

import java.util.Timer;
import java.util.TimerTask;

public class PeriodicalUpdater {
    private static final boolean DEBUG = true;
    private static final String TAG = "PeriodicalUpdater";
    private static final long FORCE_UPDATE_INTERVAL = 3000;

    private Timer timer;
    private PeriodicallyRunnable runnable;
    private int interval;
    private long lastUpdated;
    private long forceUpdated;
    private Timer beingUpdatedTimer;
    private boolean isBeingUpdated;

    public PeriodicalUpdater(PeriodicallyRunnable runnable, int interval) {
        this.runnable = runnable;
        this.interval = interval;
        forceUpdated = -1;
        isBeingUpdated = false;
    }

    public void restart(String caller) {
        stop(caller);
        start(caller);
    }

    public void start(String caller) {
        if (DEBUG) CKLog.d(TAG, "start() is called from " + caller);
        if (timer != null) {
            return;
        }

        if (interval <= 0) {
            return;
        }

        long delay = Math.max(interval - (System.currentTimeMillis() - lastUpdated), 0);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isBeingUpdated) {
                    setBeingUpdatedTimer();
                    runnable.startTask();
                }
            }
        }, delay, interval);
    }

    public void forceStart(String caller) {
        long now = System.currentTimeMillis();
        if (!isBeingUpdated && forceUpdated <= now - FORCE_UPDATE_INTERVAL) {
            forceUpdated = now;
            setBeingUpdatedTimer();
            runnable.startTask();
        }
    }

    public void stop(String caller) {
        if (DEBUG) CKLog.d(TAG, "stop() is called from " + caller);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void setBeingUpdatedTimer() {
        if (isBeingUpdated) {
            return;
        }

        isBeingUpdated = true;
        beingUpdatedTimer = new Timer();
        beingUpdatedTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        isBeingUpdated = false;
                        beingUpdatedTimer = null;
                    }
                },
                10000
        );
    }

    public void setInterval(int interval) {
        if (DEBUG) CKLog.d(TAG, "setInterval() " + interval);
        this.interval = interval;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // startTaskが1回だったとしても、タブによっては複数回コールバックされる
    public synchronized void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
        isBeingUpdated = false;
        if (beingUpdatedTimer != null) {
            beingUpdatedTimer.cancel();
            beingUpdatedTimer = null;
        }
        restart("setLastUpdated");
    }

    public interface PeriodicallyRunnable {
        void startTask();
    }
}
