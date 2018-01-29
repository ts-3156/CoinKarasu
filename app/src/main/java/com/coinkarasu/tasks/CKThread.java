package com.coinkarasu.tasks;

import android.support.annotation.CallSuper;

public abstract class CKThread extends Thread {
    @CallSuper
    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
    }
}
