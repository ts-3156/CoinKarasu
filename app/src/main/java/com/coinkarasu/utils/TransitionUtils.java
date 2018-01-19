package com.coinkarasu.utils;

import android.app.Activity;
import android.os.Build;
import android.support.transition.Fade;
import android.support.transition.Slide;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.Window;

public class TransitionUtils {
    private static final int DURATION = 300;

    public static void setContentTransitionsFlag(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
    }

    public static void setEnterTransition(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionSet set = new TransitionSet()
                    .addTransition(new Fade(Fade.IN))
                    .addTransition(new Slide(GravityCompat.END))
                    .setDuration(DURATION)
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            fragment.setEnterTransition(set);
        }
    }

    public static void setExitTransition(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionSet set = new TransitionSet()
                    .addTransition(new Slide(GravityCompat.START))
                    .setDuration(DURATION)
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            fragment.setExitTransition(set);
        }
    }
}
