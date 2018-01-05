package com.coinkarasu.utils.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class IconDiskCacheHelper {

    private static final boolean DEBUG = false;

    public static void write(Context context, String name, Bitmap bitmap) {
        FileOutputStream writer = null;

        try {
            File file = new File(context.getCacheDir(), name);
            file.createNewFile();
            if (file.exists()) {
                writer = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, writer);
            }
        } catch (IOException e) {
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static Bitmap read(Context context, String name) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        File file = new File(context.getCacheDir(), name);

        return BitmapFactory.decodeFile(file.getPath(), options);
    }

    public static boolean exists(Context context, String name) {
        File file = new File(context.getCacheDir(), name);
        return file.exists();
    }
}
