package com.coinkarasu.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DiskCacheHelper2 {

    private static final boolean DEBUG = false;

    public static void write(File file, String text) {
        FileOutputStream writer = null;

        try {
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

    public static String read(File file) {
        BufferedReader reader = null;
        String text = null;

        try {
            if (file.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                StringBuilder builder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                text = builder.toString();
            } else {
                if (DEBUG) Log.e("read", file.getPath() + " does not exist.");
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
}
