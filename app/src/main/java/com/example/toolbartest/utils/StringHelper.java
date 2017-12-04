package com.example.toolbartest.utils;

public class StringHelper {

    public static String join(String delimiter, String[] array) {
        StringBuilder builder = new StringBuilder();
        for (String str : array) {
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(str);
        }

        return builder.toString();
    }
}
