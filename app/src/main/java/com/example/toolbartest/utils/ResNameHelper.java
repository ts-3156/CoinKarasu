package com.example.toolbartest.utils;

import android.app.Activity;
import android.content.res.Resources;

import com.example.toolbartest.R;

public class ResNameHelper {

    private static final String COIN_SYMBOLS_RESOURCE_NAME_KEY = "COIN_SYMBOLS_RESOURCE_NAME_KEY";

//    public static final String SYMBOLS_NAME_MAIN = "default_symbols";
    public static final String SYMBOLS_NAME_MAIN = "japan_all_symbols";
    public static final String SYMBOLS_NAME_JPY_TOPLIST = "jpy_toplist_symbols";
    public static final String SYMBOLS_NAME_USD_TOPLIST = "usd_toplist_symbols";
    public static final String SYMBOLS_NAME_JAPAN_ALL = "japan_all_symbols";
    public static final String SYMBOLS_NAME_BITFLYER = "bitflyer_symbols";
    public static final String SYMBOLS_NAME_COINCHECK = "coincheck_symbols";
    public static final String SYMBOLS_NAME_ZAIF = "zaif_symbols";

    private static final String EXCHANGE_NAME_DEFAULT = "cccagg";
    private static final String EXCHANGE_NAME_BITFLYER = "bitflyer";
    private static final String EXCHANGE_NAME_COINCHECK = "coincheck";
    private static final String EXCHANGE_NAME_ZAIF = "zaif";

    private static final String DEFAULT_TO_SYMBOL = "JPY";

    public static String getSymbolsName(Activity activity) {
        String name = activity.getIntent().getStringExtra(COIN_SYMBOLS_RESOURCE_NAME_KEY);
        if (name == null) {
            name = SYMBOLS_NAME_MAIN;
        }
        return name;
    }

    public static void setSymbolsName(Activity activity, String symbolsName) {
        activity.getIntent().putExtra(COIN_SYMBOLS_RESOURCE_NAME_KEY, symbolsName);
    }

    public static String[] getFromSymbols(Activity activity) {
        return ResourceHelper.getStringArrayResourceByName(activity, getSymbolsName(activity));
    }

    public static String[] getFromSymbolsByExchange(Activity activity, String exchange) {
        String[] symbols = null;

        if (exchange.equals(EXCHANGE_NAME_BITFLYER)) {
            symbols = ResourceHelper.getStringArrayResourceByName(activity, SYMBOLS_NAME_BITFLYER);
        } else if (exchange.equals(EXCHANGE_NAME_COINCHECK)) {
            symbols = ResourceHelper.getStringArrayResourceByName(activity, SYMBOLS_NAME_COINCHECK);
        } else if (exchange.equals(EXCHANGE_NAME_ZAIF)) {
            symbols = ResourceHelper.getStringArrayResourceByName(activity, SYMBOLS_NAME_ZAIF);
        }

        return symbols;
    }

    public static String getToSymbol() {
        return DEFAULT_TO_SYMBOL;
    }

    public static String getToolbarTitle(Activity activity) {
        String title = "";
        Resources res = activity.getResources();

        switch (getSymbolsName(activity)) {
            case SYMBOLS_NAME_MAIN:
                title = res.getString(R.string.nav_main);
                break;
            case SYMBOLS_NAME_JPY_TOPLIST:
                title = res.getString(R.string.nav_jpy_toplist);
                break;
            case SYMBOLS_NAME_USD_TOPLIST:
                title = res.getString(R.string.nav_usd_toplist);
                break;
//            case SYMBOLS_NAME_JAPAN_ALL:
//                title = res.getString(R.string.nav_japan);
//                break;
            case SYMBOLS_NAME_BITFLYER:
                title = res.getString(R.string.nav_bitflyer);
                break;
            case SYMBOLS_NAME_COINCHECK:
                title = res.getString(R.string.nav_coincheck);
                break;
            case SYMBOLS_NAME_ZAIF:
                title = res.getString(R.string.nav_zaif);
                break;
        }

        return title;
    }

    public static boolean hasMultiExchanges(Activity activity) {
        return getSymbolsName(activity).equals(SYMBOLS_NAME_JAPAN_ALL);
    }

    public static boolean useFixedListView(Activity activity) {
        return getSymbolsName(activity).equals(SYMBOLS_NAME_JAPAN_ALL);
    }

    public static String getExchangeName(Activity activity) {
        String symbolsName = getSymbolsName(activity);
        String name = EXCHANGE_NAME_DEFAULT;

        switch (symbolsName) {
            case SYMBOLS_NAME_BITFLYER:
                name = EXCHANGE_NAME_BITFLYER;
                break;
            case SYMBOLS_NAME_COINCHECK:
                name = EXCHANGE_NAME_COINCHECK;
                break;
            case SYMBOLS_NAME_ZAIF:
                name = EXCHANGE_NAME_ZAIF;
                break;
        }

        return name;
    }
}
