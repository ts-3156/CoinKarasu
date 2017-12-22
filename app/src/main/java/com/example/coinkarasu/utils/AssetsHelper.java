package com.example.coinkarasu.utils;

import android.content.Context;
import android.graphics.Typeface;

public class AssetsHelper {

    private static AssetsHelper instance;

    public Typeface light;
    public Typeface lightItalic;

    private AssetsHelper(Context context) {
        light = Typeface.createFromAsset(context.getAssets(), "OpenSans-Light.ttf");
        lightItalic = Typeface.createFromAsset(context.getAssets(), "OpenSans-LightItalic.ttf");
    }

    public static AssetsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AssetsHelper(context);
        }
        return instance;
    }
}
