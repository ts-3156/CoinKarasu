package com.coinkarasu.utils;

public class CKStringUtils {
    public static String join(String[] params, String delim) {
        return join(delim, (Object[]) params);
    }

    public static String join(String delim, Object... params) {
        StringBuilder builder = new StringBuilder();
        for (Object obj : params) {
            if (builder.length() > 0) {
                builder.append(delim);
            }
            builder.append(obj);
        }

        return builder.toString();
    }
}
