package com.coinkarasu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileHelper {

    private static final boolean DEBUG = true;
    private static final String TAG = "FileHelper";

    public static void write(File file, String text) {
        FileOutputStream writer = null;

        try {
            file.createNewFile();
            if (file.exists()) {
                writer = new FileOutputStream(file);
                writer.write(text.getBytes());
            }
        } catch (IOException e) {
            if (DEBUG) CKLog.e(TAG, "write1", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                if (DEBUG) CKLog.e(TAG, "write2", e);
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
                if (DEBUG) CKLog.e(TAG, file.getPath() + " does not exist.");
            }
        } catch (IOException e) {
            if (DEBUG) CKLog.e(TAG, "read1", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                if (DEBUG) CKLog.e(TAG, "read2", e);
            }
        }

        return text;
    }


    public static boolean touch(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            if (DEBUG) CKLog.e(TAG, "touch", e);
        }
        return file.setLastModified(System.currentTimeMillis());
    }
}
