package com.example.toolbartest.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CacheHelper {

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
            }
        } catch (IOException e) {
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        return text;
    }

    public static boolean exists(Context context, String name) {
        File file = new File(context.getCacheDir(), name);
        return file.exists();
    }
}
