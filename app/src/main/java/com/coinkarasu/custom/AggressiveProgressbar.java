package com.coinkarasu.custom;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.coinkarasu.R;

public class AggressiveProgressbar extends AppCompatImageView {
    private static final boolean DEBUG = true;
    private static final String TAG = "AggressiveProgressbar";

    private enum Status {
        normal(R.drawable.ic_refresh_rotate, R.drawable.ic_refresh_stop),
        warning(R.drawable.ic_refresh_rotate_warning, R.drawable.ic_refresh_stop_warning),
        error(R.drawable.ic_refresh_rotate_error, R.drawable.ic_refresh_stop_error);

        public int rotate;
        public int stop;

        Status(int rotate, int stop) {
            this.rotate = rotate;
            this.stop = stop;
        }
    }

    private Animation anim;
    private Status status;

    public AggressiveProgressbar(Context context) {
        this(context, null);
    }

    public AggressiveProgressbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AggressiveProgressbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        anim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        status = Status.normal;
    }

    public void startAnimation() {
        setImageResource(status.rotate);
        startAnimation(anim);
    }

    public void stopAnimation() {
        clearAnimation();
        setImageResource(status.stop);
    }

    public void stopAnimationWithError() {
        status = Status.error;
        stopAnimation();
    }

    public void stopAnimationDelayed(long delay, boolean withWarning) {
        status = withWarning ? Status.warning : Status.normal;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                stopAnimation();
            }
        }, delay);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
        } else {
            stopAnimation(); // onPause
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
        } else {
            stopAnimation(); // onPause
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation(); // onDestroy
    }
}
