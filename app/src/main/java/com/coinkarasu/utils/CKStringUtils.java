package com.coinkarasu.utils;

import java.util.List;

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

    public static String join(String delim, List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String str : list) {
            if (builder.length() > 0) {
                builder.append(delim);
            }
            builder.append(str);
        }
        return builder.toString();
    }
}
