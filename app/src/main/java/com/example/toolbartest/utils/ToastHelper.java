package com.example.toolbartest.utils;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.example.toolbartest.MainActivity;

public class ToastHelper {
    public static void showToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
}
