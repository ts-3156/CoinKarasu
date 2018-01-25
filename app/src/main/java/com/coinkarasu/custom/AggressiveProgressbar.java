package com.coinkarasu.custom;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.animator.ValueAnimatorBase;

public class AggressiveProgressbar extends AppCompatImageView {
    private static final boolean DEBUG = true;
    private static final String TAG = "AggressiveProgressbar";
    private static final long DELAY = ValueAnimatorBase.DURATION;

    public enum Status {
        normal(R.drawable.ic_refresh_rotate, R.drawable.ic_refresh_stop),
        warning(R.drawable.ic_refresh_rotate_warning, R.drawable.ic_refresh_stop_warning),
        error(R.drawable.ic_refresh_rotate_error, R.drawable.ic_refresh_stop_error),
        airplane(R.drawable.ic_refresh_rotate, R.drawable.ic_airplanemode_active);

        public int rotate;
        public int stop;

        Status(int rotate, int stop) {
            this.rotate = rotate;
            this.stop = stop;
        }
    }

    private Animation anim;
    private Status status;
    private Section section;

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

    public void stopAnimationWithAirplaneMode() {
        status = Status.airplane;
        stopAnimation();
    }

    public void stopAnimationWithError() {
        status = Status.error;
        stopAnimation();
    }

    public void stopAnimationDelayed(boolean withWarning) {
        status = withWarning ? Status.warning : Status.normal;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                stopAnimation();
            }
        }, DELAY);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        setImageResource(status.stop);
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
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
