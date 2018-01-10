package com.coinkarasu.custom;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.coinkarasu.R;

public class AggressiveProgressbar extends AppCompatImageView {
    private Animation anim;

    public AggressiveProgressbar(Context context) {
        this(context, null);
    }

    public AggressiveProgressbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AggressiveProgressbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        anim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
    }

    public void startAnimation() {
        setImageResource(R.drawable.ic_refresh_rotate);
        startAnimation(anim);
    }

    public void stopAnimation() {
        clearAnimation();
        setImageResource(R.drawable.ic_refresh_stop);
    }

    public void stopAnimationDelayed(long delay) {
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
