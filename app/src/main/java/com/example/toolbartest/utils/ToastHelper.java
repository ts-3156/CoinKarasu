package com.example.toolbartest.utils;

import android.app.Activity;
import android.widget.Toast;

public class ToastHelper {
    public static void showToast(Activity activity, String message) {
        showToast(activity, message, Toast.LENGTH_LONG);
    }

    public static void showToast(Activity activity, String message, int duration) {
        Toast.makeText(activity, message, duration).show();
    }
}
