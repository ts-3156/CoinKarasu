package com.example.coinkarasu.utils;

import android.app.Activity;
import android.support.design.widget.Snackbar;

public class SnackbarHelper {
    public static void showSnackbar(Activity activity, String message) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }
}
