package com.coinkarasu.utils;

import java.util.Timer;
import java.util.TimerTask;

public class PeriodicalUpdater {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "PeriodicalUpdater";
    private static final long FORCE_UPDATE_INTERVAL = 3000;

    private Timer timer;
    private PeriodicalTask task;
    private int interval;
    private long lastUpdated;
    private long forceUpdated;
    private Timer beingUpdatedTimer;
    private boolean isBeingUpdated;

    public PeriodicalUpdater(PeriodicalTask task, int interval) {
        this.task = task;
        this.interval = interval;
        forceUpdated = -1;
        isBeingUpdated = false;
    }

    public void restart(String caller) {
        stop(caller);
        start(caller);
    }

    public synchronized void start(String caller) {
        long delay = Math.max(interval - (CKDateUtils.now() - lastUpdated), 0);
        if (DEBUG) CKLog.d(TAG, "start() is called from " + caller
                + " interval=" + interval + " delay=" + delay);

        if (timer != null) {
            if (DEBUG) CKLog.w(TAG, "start() timer is already started");
            return;
        }

        if (interval <= 0) {
            return;
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isBeingUpdated) {
                    setBeingUpdatedTimer();
                    task.startUpdating();
                }
            }
        }, delay, interval);
    }

    public synchronized void forceStart(String caller) {
        long now = CKDateUtils.now();
        if (!isBeingUpdated && forceUpdated <= now - FORCE_UPDATE_INTERVAL) {
            forceUpdated = now;
            setBeingUpdatedTimer();
            task.startUpdating();
        }
    }

    public synchronized void stop(String caller) {
        if (DEBUG) CKLog.d(TAG, "stop() is called from " + caller);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private synchronized void setBeingUpdatedTimer() {
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

    public synchronized void setLastUpdated(long lastUpdated, boolean restart) {
        this.lastUpdated = lastUpdated;
        isBeingUpdated = false;
        if (beingUpdatedTimer != null) {
            beingUpdatedTimer.cancel();
            beingUpdatedTimer = null;
        }

        if (restart) {
            restart("setLastUpdated");
        }
    }

    public interface PeriodicalTask {
        void startUpdating();
    }
}
