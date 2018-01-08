package com.coinkarasu.utils;

import android.util.Log;

import java.io.*;

public class DiskCacheHelper2 {

    private static final boolean DEBUG = false;
    private static final String TAG = "DiskCacheHelper2";

    public static void write(File file, String text) {
        FileOutputStream writer = null;

        try {
            file.createNewFile();
            if (file.exists()) {
                writer = new FileOutputStream(file);
                writer.write(text.getBytes());
            }
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "write1 " + e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                if (DEBUG) Log.e(TAG, "write2 " + e.getMessage());
            }
        }
    }

    public static String read(File file) {
        if (!file.exists()) {
            if (DEBUG) Log.e(TAG, file.getPath() + " does not exist.");
            return null;
        }

        BufferedReader reader = null;
        String text = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            text = builder.toString();
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "read1 " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                if (DEBUG) Log.e(TAG, "read2 " + e.getMessage());
            }
        }

        return text;
    }

    public static boolean clear(File rootDir) {
        return remove(rootDir);
    }

    private static boolean remove(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                remove(child);
            }
        }
        return file.delete();
    }
}
