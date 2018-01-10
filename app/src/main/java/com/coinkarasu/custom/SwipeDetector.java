package com.coinkarasu.custom;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.utils.CKLog;

public class SwipeDetector extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "SwipeDetector";

    public static final int TO_LEFT = 1;
    public static final int TO_RIGHT = 2;
    private static final int SCROLL_MIN_DISTANCE = 50;
    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_MIN_VELOCITY = 200;
    private static final int SWIPE_MAX_OFF_PATH = 100;

    private GestureDetector gesture;
    private OnSwipeListener listener;
    private View view;
    private ViewGroup parent;

    private boolean isSwiped;
    private boolean isScrolling;
    private boolean isSwipeCanceled;
    private int scrollDirection;
    private float scrollStartX;
    private float scrollLastX;

    public SwipeDetector(Context context) {
        gesture = new GestureDetector(context, this);
    }

    public void attach(View view, ViewGroup parent) {
        this.view = view;
        this.parent = parent;
        view.setOnTouchListener(this);
    }

    public void setOnSwipeListener(OnSwipeListener listener) {
        this.listener = listener;
    }

    private void onSwipe(int direction) {
        if (isSwiped) {
            return;
        }
        isSwiped = true;

        if (DEBUG) CKLog.d(TAG, "onSwipe() " + directionToName(direction));
        if (listener != null) {
            listener.onSwipe(view, direction);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (DEBUG) CKLog.d(TAG, "onTouch() " + actionToName(event));
            isSwiped = false;
            isScrolling = false;
            isSwipeCanceled = false;
            scrollStartX = 0f;
            scrollLastX = 0f;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (DEBUG) CKLog.d(TAG, "onTouch() " + actionToName(event));
            if (isScrolling && !isSwipeCanceled) {
                if (Math.abs(scrollStartX - scrollLastX) > SCROLL_MIN_DISTANCE) {
                    onSwipe(scrollDirection);
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (DEBUG) CKLog.d(TAG, "onTouch() " + actionToName(event));
        }

        return gesture.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isSwipeCanceled) {
            return false;
        }

        if (!isScrolling) {
            isScrolling = true;
            scrollStartX = e1.getX();
            scrollDirection = distanceX > 0 ? TO_LEFT : TO_RIGHT;
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }

        scrollLastX = e2.getX();
        int newDirection = distanceX > 0 ? TO_LEFT : TO_RIGHT;
        if (scrollDirection != newDirection) {
            if (DEBUG) CKLog.d(TAG, "onScroll() Cancel since direction is changed");
            isSwipeCanceled = true;
        }

        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
            if (DEBUG) CKLog.d(TAG, "onScroll() Cancel since distance y is too long");
            isSwipeCanceled = true;
        }

        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (isSwiped || isSwipeCanceled) {
            return false;
        }

        float distance = Math.abs((e1.getX() - e2.getX()));
        float velocity = Math.abs(velocityX);

        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
            if (DEBUG) CKLog.d(TAG, "onFling() Distance y is too long");
            return false;
        }

        if (velocity < SWIPE_MIN_VELOCITY) {
            if (DEBUG) CKLog.d(TAG, "onFling() Velocity is too slow");
            return false;
        }

        if (distance > SWIPE_MIN_DISTANCE) {
            if (e1.getX() > e2.getX()) {
                onSwipe(TO_LEFT);
            } else {
                onSwipe(TO_RIGHT);
            }
        } else {
            if (DEBUG) CKLog.d(TAG, "onFling() Distance x is too short "
                    + distance + " < " + SWIPE_MIN_DISTANCE);
        }

        return false;
    }

    private String actionToName(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return "ACTION_DOWN";
            case MotionEvent.ACTION_UP:
                return "ACTION_UP";
            case MotionEvent.ACTION_MOVE:
                return "ACTION_MOVE";
            case MotionEvent.ACTION_CANCEL:
                return "ACTION_CANCEL";
            default:
                throw new RuntimeException("Invalid action " + e.getAction());
        }
    }

    private String directionToName(int direction) {
        if (direction == TO_LEFT) {
            return "TO_LEFT";
        } else if (direction == TO_RIGHT) {
            return "TO_LEFT";
        } else {
            throw new RuntimeException("Invalid direction " + direction);
        }
    }

    public interface OnSwipeListener {
        void onSwipe(View view, int direction);
    }
}
