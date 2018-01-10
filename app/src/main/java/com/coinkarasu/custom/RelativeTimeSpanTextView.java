package com.coinkarasu.custom;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.AppCompatTextView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;

import com.coinkarasu.activities.TimeProvider;
import com.coinkarasu.utils.CKLog;

import java.util.Timer;
import java.util.TimerTask;

public class RelativeTimeSpanTextView extends AppCompatTextView {
    private static final boolean DEBUG = true;
    private static final String TAG = "RelativeTimeSpanTextView";
    private static final long DEFAULT_PERIOD = 5000;
    private static final long LONG_PERIOD = 10000;

    private Timer timer;
    private long time;
    private TimeProvider timeProvider;
    private CKLog logger;

    public RelativeTimeSpanTextView(Context context) {
        this(context, null);
    }

    public RelativeTimeSpanTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public RelativeTimeSpanTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (DEBUG) CKLog.d(TAG, "RelativeTimeSpanTextView()");
        logger = new CKLog(getContext());
        time = -1;
        timeProvider = null;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public void updateText() {
        if (DEBUG) CKLog.d(TAG, "updateText() is called.");
        stopTimer("updateText");
        startTimer("updateText");
    }

    private void startTimer(final String caller) {
        if (DEBUG) CKLog.d(TAG, "startTimer() is called from " + caller);
        if (timer != null) {
            return;
        }

        // 表示されているタブの変更をフックしていないため、現状では、初期化が完了した
        // すべてのタブでタイマーが動き続ける。タブが非表示になった時にタイマーを
        // 止めるのは簡単だが、表示された時にタイマーを動かすのは今の実装では単純にはできない。

        if (timeProvider != null) {
            time = timeProvider.getLastUpdated();
        }
        long period = getPeriod(time);
        if (DEBUG) CKLog.d(TAG, "Current period " + period);
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final String str = getRelativeTimeSpanString(time, System.currentTimeMillis());
                if (DEBUG) CKLog.d(TAG, "time=" + time + " text=" + str);
                post(new Runnable() {
                    @Override
                    public void run() {
                        setText(str);
                    }
                });
            }
        }, 0, period);
    }

    private void stopTimer(String caller) {
        if (DEBUG) CKLog.d(TAG, "stopTimer() is called from " + caller);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private long getPeriod(long time) {
        if (time <= 0) {
            return DEFAULT_PERIOD;
        }

        long elapsed = System.currentTimeMillis() - time;
        if (elapsed < 30000) {
            return DEFAULT_PERIOD;
        } else {
            return LONG_PERIOD;
        }
    }

    private static String getRelativeTimeSpanString(long time, long now) {
        if (time <= 0) {
            return "";
        }

        long diff = now - time;
        String str;

        if (diff < 1000) {
            str = "Just now";
        } else {
            str = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();
        }

        return str;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            startTimer("onVisibilityChanged"); // onResume
        } else {
            stopTimer("onVisibilityChanged"); // onPause
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            startTimer("onWindowFocusChanged"); // onResume
        } else {
            stopTimer("onWindowFocusChanged"); // onPause
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimer("onDetachedFromWindow"); // onDestroy
    }

    // TODO 今の実装では、ユニークなIDを持たないため呼ばれることはない。
    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("state", super.onSaveInstanceState());
        bundle.putLong("time", time);
        return bundle;
    }

    // TODO 今の実装では、ユニークなIDを持たないため呼ばれることはない。
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            time = bundle.getLong("time");
            state = bundle.getParcelable("state");
        }
        super.onRestoreInstanceState(state);
    }
}
