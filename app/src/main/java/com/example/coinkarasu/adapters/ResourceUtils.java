package com.example.coinkarasu.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;

import com.android.volley.toolbox.ImageLoader;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.format.PriceFormat;
import com.example.coinkarasu.format.TrendColorFormat;
import com.example.coinkarasu.format.TrendIconFormat;
import com.example.coinkarasu.format.TrendValueFormat;
import com.example.coinkarasu.utils.VolleyHelper;

import java.util.HashMap;
import java.util.List;

class ResourceUtils {

    ImageLoader imageLoader;
    HashMap<String, Integer> symbolIconResIdMap;
    private int trendUp;
    private int trendFlat;
    private int trendDown;
    int priceUpFromColor;
    int priceDownFromColor;
    int priceToColor;
    Typeface typeFace;
    Typeface typeFaceItalic;

    PriceFormat priceFormatter;
    TrendValueFormat trendFormatter;
    TrendIconFormat trendIconFormat;

    ResourceUtils(Context context, List<Coin> coins) {
        symbolIconResIdMap = buildIconResIdMap(context, coins);
        initializeColors(context);
        imageLoader = VolleyHelper.getInstance(context).getImageLoader();

        typeFace = Typeface.createFromAsset(context.getAssets(), "OpenSans-Light.ttf");
        typeFaceItalic = Typeface.createFromAsset(context.getAssets(), "OpenSans-LightItalic.ttf");

        for (Coin coin : coins) {
            if (!coin.isSectionHeader()) {
                priceFormatter = new PriceFormat(coin.getToSymbol());
                break;
            }
        }
        trendFormatter = new TrendValueFormat();
        trendIconFormat = new TrendIconFormat();
    }

    private HashMap<String, Integer> buildIconResIdMap(Context context, List<Coin> coins) {
        HashMap<String, Integer> map = new HashMap<>();
        Resources resources = context.getResources();
        String packageName = context.getPackageName();

        for (Coin coin : coins) {
            if (coin.isSectionHeader()) {
                continue;
            }
            String name = "ic_coin_" + coin.getSymbol().toLowerCase();
            int resId = resources.getIdentifier(name, "raw", packageName);
            map.put(coin.getSymbol(), resId);
        }

        return map;
    }

    private void initializeColors(Context context) {
        Resources resources = context.getResources();
        TrendColorFormat formatter = new TrendColorFormat();

        trendUp = resources.getColor(formatter.format(1.0));
        trendFlat = resources.getColor(formatter.format(0.0));
        trendDown = resources.getColor(formatter.format(-1.0));
    }

    void toSymbolChanged(String symbol) {
        priceFormatter = new PriceFormat(symbol);
    }

    int getTrendColor(double trend) {
        int color;

        if (trend > 0.0) {
            color = trendUp;
        } else if (trend < 0.0) {
            color = trendDown;
        } else {
            color = trendFlat;
        }

        return color;
    }


}
