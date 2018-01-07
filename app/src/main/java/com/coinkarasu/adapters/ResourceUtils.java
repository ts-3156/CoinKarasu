package com.coinkarasu.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.SparseArray;

import com.android.volley.toolbox.ImageLoader;
import com.coinkarasu.R;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.format.PriceFormat;
import com.coinkarasu.format.SignedPriceFormat;
import com.coinkarasu.format.SurroundedTrendValueFormat;
import com.coinkarasu.format.TrendColorFormat;
import com.coinkarasu.format.TrendIconFormat;
import com.coinkarasu.format.TrendValueFormat;
import com.coinkarasu.utils.volley.VolleyHelper;

import java.util.HashMap;
import java.util.List;

public class ResourceUtils {

    public ImageLoader imageLoader;
    public HashMap<String, Integer> symbolIconResIdMap;
    public SparseArray<String> headerNameResIdStringMap;
    private int trendUp;
    private int trendFlat;
    private int trendDown;
    public int priceUpFromColor;
    public int priceDownFromColor;
    public int priceToColor;

    public PriceFormat priceFormatter;
    public SignedPriceFormat signedPriceFormatter;
    TrendValueFormat trendFormatter;
    public SurroundedTrendValueFormat surroundedTrendFormatter;
    public TrendIconFormat trendIconFormat;

    ResourceUtils(Context context, List<Coin> coins) {
        symbolIconResIdMap = buildIconResIdMap(context, coins);
        headerNameResIdStringMap = buildHeaderNameResIdMap(context, coins);
        initializeColors(context);
        imageLoader = VolleyHelper.getInstance(context).getImageLoader();

        for (Coin coin : coins) {
            if (!coin.isSectionHeader() && !coin.isAdCoin()) {
                priceFormatter = new PriceFormat(coin.getToSymbol());
                signedPriceFormatter = new SignedPriceFormat(coin.getToSymbol());
                break;
            }
        }
        trendFormatter = new TrendValueFormat();
        surroundedTrendFormatter = new SurroundedTrendValueFormat();
        trendIconFormat = new TrendIconFormat();
    }

    private HashMap<String, Integer> buildIconResIdMap(Context context, List<Coin> coins) {
        HashMap<String, Integer> map = new HashMap<>();
        Resources resources = context.getResources();
        String packageName = context.getPackageName();

        for (Coin coin : coins) {
            if (coin.isSectionHeader() || coin.isAdCoin()) {
                continue;
            }
            String name = "ic_coin_" + coin.getSymbol().toLowerCase();
            int resId = resources.getIdentifier(name, "raw", packageName);
            map.put(coin.getSymbol(), resId);
        }

        return map;
    }

    private SparseArray<String> buildHeaderNameResIdMap(Context context, List<Coin> coins) {
        SparseArray<String> map = new SparseArray<>();
        Resources resources = context.getResources();

        for (Coin coin : coins) {
            if (!coin.isSectionHeader()) {
                continue;
            }
            int resId = coin.getHeaderNameResId();
            map.put(resId, resources.getString(resId));
        }

        return map;
    }

    private void initializeColors(Context context) {
        Resources resources = context.getResources();
        TrendColorFormat formatter = new TrendColorFormat();

        trendUp = resources.getColor(formatter.format(1.0));
        trendFlat = resources.getColor(formatter.format(0.0));
        trendDown = resources.getColor(formatter.format(-1.0));

        priceUpFromColor = resources.getColor(R.color.colorPriceBgUp);
        priceDownFromColor = resources.getColor(R.color.colorPriceBgDown);
        priceToColor = resources.getColor(R.color.colorPriceDefault);
    }

    void toSymbolChanged(String symbol) {
        priceFormatter = new PriceFormat(symbol);
        signedPriceFormatter = new SignedPriceFormat(symbol);
    }

    public int getTrendColor(double trend) {
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

    public int getPriceColor(double price) {
        return getTrendColor(price);
    }
}
