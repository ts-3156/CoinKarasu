package com.example.coinkarasu.activities;

import android.content.Context;

import com.example.coinkarasu.R;
import com.example.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;

import static com.example.coinkarasu.activities.ListViewFragment.*;

public enum NavigationKind {
    home(R.string.nav_home, R.string.tab_home, R.string.summary_home, R.id.nav_home, R.color.colorNavHome, R.color.colorNavHomeDark, R.color.state_nav_home, -1, new Exchange[]{}),
    japan(R.string.nav_japan, R.string.tab_japan, R.string.summary_japan, R.id.nav_japan, R.color.colorNavJapan, R.color.colorNavJapanDark, R.color.state_nav_japan, R.array.japan_all_symbols, new Exchange[]{Exchange.bitflyer, Exchange.coincheck, Exchange.zaif}),
    coincheck(R.string.nav_coincheck, R.string.tab_coincheck, R.string.summary_coincheck, R.id.nav_coincheck, R.color.colorCoincheck, R.color.colorCoincheckDark, R.color.state_nav_coincheck, R.array.coincheck_trading_symbols, new Exchange[]{}),
    jpy_toplist(R.string.nav_jpy_toplist, R.string.tab_jpy_toplist, R.string.summary_jpy_toplist, R.id.nav_jpy_toplist, R.color.colorJpyToplist, R.color.colorJpyToplistDark, R.color.state_nav_jpy_toplist, R.array.jpy_toplist_symbols, new Exchange[]{Exchange.cccagg}),
    usd_toplist(R.string.nav_usd_toplist, R.string.tab_usd_toplist, R.string.summary_usd_toplist, R.id.nav_usd_toplist, R.color.colorUsdToplist, R.color.colorUsdToplistDark, R.color.state_nav_usd_toplist, R.array.usd_toplist_symbols, new Exchange[]{Exchange.cccagg}),
    eur_toplist(R.string.nav_eur_toplist, R.string.tab_eur_toplist, R.string.summary_eur_toplist, R.id.nav_eur_toplist, R.color.colorEurToplist, R.color.colorEurToplistDark, R.color.state_nav_eur_toplist, R.array.eur_toplist_symbols, new Exchange[]{Exchange.cccagg}),
    btc_toplist(R.string.nav_btc_toplist, R.string.tab_btc_toplist, R.string.summary_btc_toplist, R.id.nav_btc_toplist, R.color.colorBtcToplist, R.color.colorBtcToplistDark, R.color.state_nav_btc_toplist, R.array.btc_toplist_symbols, new Exchange[]{Exchange.cccagg}),
    edit_tabs(R.string.nav_edit_tabs, R.string.tab_edit_tabs, R.string.summary_edit_tabs, R.id.nav_edit_tabs, R.color.colorEditTabs, R.color.colorEditTabsDark, R.color.state_nav_edit_tabs, -1, new Exchange[]{});

    public int navStrResId;
    public int headerStrResId;
    public int tabStrResId;
    public int titleStrResId;
    public int summaryStrResId;
    public int navResId;
    public int colorResId;
    public int colorDarkResId;
    public int colorStateResId;
    public int symbolsResId;
    public Exchange[] exchanges;

    NavigationKind(int navStrResId, int tabStrResId, int summaryStrResId, int navResId, int colorResId, int colorDarkResId,
                   int colorStateResId, int symbolsResId, Exchange[] exchanges) {
        this.navStrResId = navStrResId;
        this.headerStrResId = navStrResId; // headerStrResId == navStrResId
        this.tabStrResId = tabStrResId;
        this.titleStrResId = tabStrResId; // titleStrResId == tabStrResId
        this.summaryStrResId = summaryStrResId;
        this.navResId = navResId;
        this.colorResId = colorResId;
        this.colorDarkResId = colorDarkResId;
        this.colorStateResId = colorStateResId;
        this.symbolsResId = symbolsResId;
        this.exchanges = exchanges;
    }

    public boolean isHideable() {
        return this != home && this != edit_tabs;
    }

    public boolean isVisible(Context context) {
        return !isHideable() || PrefHelper.isVisibleTab(context, this);
    }

    public static ArrayList<NavigationKind> visibleValues(Context context) {
        ArrayList<NavigationKind> values = new ArrayList<>();
        for (NavigationKind kind : values()) {
            if (kind.isVisible(context)) {
                values.add(kind);
            }
        }
        return values;
    }

    public static int visiblePosition(Context context, NavigationKind kind) {
        return visibleValues(context).indexOf(kind);
    }

    public boolean defaultVisibility() {
        return this != eur_toplist && this != btc_toplist;
    }

    public static NavigationKind valueByNavResId(int navResId) {
        for (NavigationKind kind : values()) {
            if (kind.navResId == navResId) {
                return kind;
            }
        }
        return null;
    }
}
