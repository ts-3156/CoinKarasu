package com.coinkarasu.utils.cache;

import android.text.TextUtils;
import android.util.Log;

import java.io.*;

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
            Log.d(TAG, "Could not delete cache file for " + key);
        }
    }

    @Override
    public void clear() {
        if (!remove(rootDir)) {
            Log.d(TAG, "Could not clear cache dir " + rootDir.getPath());
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

    private static String read(File file) {
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
}
