package com.coinkarasu.format;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;

import com.coinkarasu.utils.CKLog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeightedPriceFormat extends PriceFormat {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "WeightedPriceFormat";
    private static final Pattern PATTERN = Pattern.compile("([0.]+)");
    private static final TypefaceSpan THIN = new TypefaceSpan("sans-serif-thin");

    public WeightedPriceFormat(String toSymbol) {
        super(toSymbol);
    }

    @Override
    public CharSequence format(double price) {
        CharSequence seq = super.format(price);
        Matcher matcher = PATTERN.matcher(seq);

        if (!matcher.find()) {
            return seq;
        }

        try {
            String matched = matcher.group(1);
            String str = seq.toString();
            int index = str.indexOf(matched);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(str.substring(0, index));

            int start = builder.length();
            builder.append(matched);
            builder.setSpan(THIN, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append(str.substring(index + matched.length(), str.length()));
            return builder;
        } catch (Exception e) {
            CKLog.e(TAG, e);
            return seq;
        }
    }
}
