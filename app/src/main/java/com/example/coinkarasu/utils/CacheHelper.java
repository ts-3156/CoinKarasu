package com.example.coinkarasu.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

public class CacheHelper {

    private static final boolean DEBUG = false;

    public static void write(Context context, String name, String text) {
        FileOutputStream writer = null;

        try {
            File file = new File(context.getCacheDir(), name);
            file.createNewFile();
            if (file.exists()) {
                writer = new FileOutputStream(file);
                writer.write(text.getBytes());
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

    public static String read(Context context, String name) {
        BufferedReader reader = null;
        String text = null;

        try {
            File file = new File(context.getCacheDir(), name);

            if (file.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                StringBuilder builder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                text = builder.toString();
            } else {
                if (DEBUG) Log.e("read", name + " does not exist.");
            }
        } catch (IOException e) {
            Log.e("read1", e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.e("read2", e.getMessage());
            }
        }

        return text;
    }

    public static boolean exists(Context context, String name) {
        File file = new File(context.getCacheDir(), name);
        return file.exists();
    }

    public static boolean remove(Context context, String name) {
        File file = new File(context.getCacheDir(), name);
        return file.delete();
    }

    public static Date lastModified(Context context, String name) {
        File file = new File(context.getCacheDir(), name);
        return new Date(file.lastModified());
    }

    public static boolean isExpired(Context context, String name, long duration) {
        return new Date(System.currentTimeMillis() - duration).compareTo(lastModified(context, name)) > 0;
    }

    public static boolean touch(Context context, String name) {
        File file = new File(context.getCacheDir(), name);
        try {
            file.createNewFile();
        } catch (IOException e) {
        }
        return file.setLastModified(System.currentTimeMillis());
    }
}
