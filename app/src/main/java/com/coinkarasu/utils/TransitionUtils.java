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
    private static final int SPLIT_SECOND = 300;
    private static final int SLOWNESS = 600;

    public static void setContentTransitionsFlag(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
    }

    public static void setSlideEnterTransition(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionSet set = new TransitionSet()
                    .addTransition(new Fade(Fade.IN))
                    .addTransition(new Slide(GravityCompat.END))
                    .setDuration(SPLIT_SECOND)
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            fragment.setEnterTransition(set);
        }
    }

    public static void setSlideExitTransition(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionSet set = new TransitionSet()
                    .addTransition(new Slide(GravityCompat.START))
                    .setDuration(SPLIT_SECOND)
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            fragment.setExitTransition(set);
        }
    }

    public static void setFadeEnterTransition(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionSet set = new TransitionSet()
                    .addTransition(new Fade(Fade.IN))
                    .setDuration(SLOWNESS)
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            fragment.setEnterTransition(set);
        }
    }
}
