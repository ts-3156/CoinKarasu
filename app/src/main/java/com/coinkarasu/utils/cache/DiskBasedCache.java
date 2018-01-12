package com.coinkarasu.utils.cache;

import android.text.TextUtils;

import com.coinkarasu.utils.CKLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DiskBasedCache implements Cache {

    private static final boolean DEBUG = true;
    private static final String TAG = "DiskBasedCache";

    private File rootDir;

    public DiskBasedCache(File rootDir) {
        this.rootDir = rootDir;
    }

    private File getFileFor(String path) {
        return new File(rootDir, path);
    }

    @Override
    public Entry get(String key) {
        String text = read(getFileFor(key));
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        return new Entry(text);
    }

    @Override
    public void put(String key, Entry entry) {
        write(getFileFor(key), entry.data);
    }

    @Override
    public void remove(String key) {
        if (!remove(getFileFor(key))) {
            if (DEBUG) CKLog.d(TAG, "remove() Could not delete cache file for " + key);
        }
    }

    @Override
    public void clear() {
        if (!remove(rootDir)) {
            if (DEBUG) CKLog.d(TAG, "clear() Could not clear cache dir " + rootDir.getPath());
        }
    }

    private static boolean remove(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                remove(child);
            }
        }
        return file.delete();
    }

    private static void write(File file, String data) {
        FileOutputStream writer = null;

        try {
            file.createNewFile();
            if (file.exists()) {
                writer = new FileOutputStream(file);
                writer.write(data.getBytes());
            }
        } catch (IOException e) {
            CKLog.e(TAG, "write1", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                CKLog.e(TAG, "write2", e);
            }
        }
    }

    private static String read(File file) {
        if (!file.exists()) {
            if (DEBUG) CKLog.w(TAG, "read() " + file.getPath() + " does not exist.");
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
            CKLog.e(TAG, "read1", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                CKLog.e(TAG, "read2", e);
            }
        }

        return text;
    }
}
