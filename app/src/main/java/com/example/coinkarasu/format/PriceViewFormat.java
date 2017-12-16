package com.example.coinkarasu.format;

import android.animation.ValueAnimator;
import android.util.Log;
import android.widget.TextView;

import com.example.coinkarasu.coins.Coin;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class PriceViewFormat {
    public static final long DURATION = 1000;

    private double prevPrice;
    private double curPrice;
    private String toSymbol;
    private boolean anim;

    public PriceViewFormat(Coin coin) {
        this(-1.0, coin.getPrice(), coin.getToSymbol(), false);
    }

    public PriceViewFormat(Coin coin, boolean anim) {
        this(coin.getPrevPrice(), coin.getPrice(), coin.getToSymbol(), anim);
    }

    public PriceViewFormat(String curPrice, String toSymbol) {
        this(-1.0, Double.valueOf(curPrice), toSymbol, false);
    }

    private PriceViewFormat(double prevPrice, double curPrice, String toSymbol, boolean anim) {
        this.prevPrice = prevPrice;
        this.curPrice = curPrice;
        this.toSymbol = toSymbol;
        this.anim = anim;
    }

    public void format(TextView view) {
        Locale locale = symbolToLocale(toSymbol);
        Currency currency = Currency.getInstance(Currency.getInstance(locale).getCurrencyCode());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        String value;

        if (toSymbol.equals("JPY")) {
            if (curPrice > 1000.0 || curPrice < -1000.0) {
                formatter.setMaximumFractionDigits(0);
                formatter.setMinimumFractionDigits(0);
            } else {
                formatter.setMaximumFractionDigits(2);
                formatter.setMinimumFractionDigits(2);
            }
            value = formatter.format(curPrice / Math.pow(10, currency.getDefaultFractionDigits()));
        } else {
            value = formatter.format(curPrice);
        }

        view.setText(value);

        if (anim && prevPrice != -0.1) {
            setAnim(view);
        }
    }

    private void setAnim(final TextView view) {
        double prev = prevPrice;
        if (prev == 0.0) {
            prev = 0.95 * curPrice;
        }

        ValueAnimator animator = ValueAnimator.ofFloat((float) prev, (float) curPrice);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                new PriceViewFormat(animation.getAnimatedValue().toString(), toSymbol).format(view);
            }
        });
        animator.start();
    }


    private Locale symbolToLocale(String symbol) {
        Locale locale = null;

        switch (symbol) {
            case "JPY":
                locale = Locale.JAPAN;
                break;
            case "USD":
                locale = Locale.US;
                break;
            default:
                Log.d("Invalid locale", symbol);
                locale = Locale.JAPAN;
        }

        return locale;
    }
}
