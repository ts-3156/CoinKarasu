package com.coinkarasu.utils.volley;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {

    private static final boolean DEBUG = false;

    public static void write(File file, Bitmap bitmap) {
        FileOutputStream writer = null;

        try {
            file.createNewFile();
            writer = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, writer);
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

    public static Bitmap read(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(file.getPath(), options);
    }
}
