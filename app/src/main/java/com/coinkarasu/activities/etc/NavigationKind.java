package com.coinkarasu.activities.etc;

import android.content.Context;

import com.coinkarasu.R;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;
import java.util.List;

public enum NavigationKind {
    home(R.string.nav_home, R.string.header_home, R.string.tab_home, R.string.summary_home, R.id.nav_home, R.color.colorNavHome, R.color.colorNavHomeDark, R.color.state_nav_home, -1, new Exchange[]{}, new Section[]{}, R.drawable.ic_nav_home, R.anim.progress_home),
    //    assets(R.string.nav_assets, R.string.header_assets, R.string.tab_assets, R.string.summary_assets, R.id.nav_assets, R.color.colorNavAssets, R.color.colorNavAssetsDark, R.color.state_nav_assets, -1, new Exchange[]{}, new Section[]{}, R.drawable.ic_nav_assets, R.anim.progress_assets),
    japan(R.string.nav_japan, R.string.header_japan, R.string.tab_japan, R.string.summary_japan, R.id.nav_japan, R.color.colorNavJapan, R.color.colorNavJapanDark, R.color.state_nav_japan, R.array.japan_all_symbols, new Exchange[]{Exchange.bitflyer, Exchange.coincheck, Exchange.zaif}, new Section[]{new Section(Exchange.bitflyer, CoinKind.none), new Section(Exchange.coincheck, CoinKind.none), new Section(Exchange.zaif, CoinKind.none)}, R.drawable.ic_nav_japan, R.anim.progress_japan),
    bitflyer(R.string.nav_bitflyer, R.string.header_bitflyer, R.string.tab_bitflyer, R.string.summary_bitflyer, R.id.nav_bitflyer, R.color.colorBitflyer, R.color.colorBitflyerDark, R.color.state_nav_bitflyer, R.array.bitflyer_all_symbols, new Exchange[]{}, new Section[]{}, R.drawable.ic_nav_bitflyer, R.anim.progress_bitflyer),
    coincheck(R.string.nav_coincheck, R.string.header_coincheck, R.string.tab_coincheck, R.string.summary_coincheck, R.id.nav_coincheck, R.color.colorCoincheck, R.color.colorCoincheckDark, R.color.state_nav_coincheck, R.array.coincheck_all_symbols, new Exchange[]{Exchange.coincheck}, new Section[]{new Section(Exchange.coincheck, CoinKind.trading), new Section(Exchange.coincheck, CoinKind.sales)}, R.drawable.ic_nav_coincheck, R.anim.progress_coincheck),
    zaif(R.string.nav_zaif, R.string.header_zaif, R.string.tab_zaif, R.string.summary_zaif, R.id.nav_zaif, R.color.colorZaif, R.color.colorZaifDark, R.color.state_nav_zaif, R.array.zaif_all_symbols, new Exchange[]{}, new Section[]{}, R.drawable.ic_nav_zaif, R.anim.progress_zaif),
    jpy_toplist(R.string.nav_jpy_toplist, R.string.header_jpy_toplist, R.string.tab_jpy_toplist, R.string.summary_jpy_toplist, R.id.nav_jpy_toplist, R.color.colorJpyToplist, R.color.colorJpyToplistDark, R.color.state_nav_jpy_toplist, R.array.jpy_toplist_symbols, new Exchange[]{Exchange.cccagg}, new Section[]{new Section(Exchange.cccagg, CoinKind.none)}, R.drawable.ic_nav_jpy, R.anim.progress_jpy_toplist),
    usd_toplist(R.string.nav_usd_toplist, R.string.header_usd_toplist, R.string.tab_usd_toplist, R.string.summary_usd_toplist, R.id.nav_usd_toplist, R.color.colorUsdToplist, R.color.colorUsdToplistDark, R.color.state_nav_usd_toplist, R.array.usd_toplist_symbols, new Exchange[]{Exchange.cccagg}, new Section[]{new Section(Exchange.cccagg, CoinKind.none)}, R.drawable.ic_nav_usd, R.anim.progress_usd_toplist),
    eur_toplist(R.string.nav_eur_toplist, R.string.header_eur_toplist, R.string.tab_eur_toplist, R.string.summary_eur_toplist, R.id.nav_eur_toplist, R.color.colorEurToplist, R.color.colorEurToplistDark, R.color.state_nav_eur_toplist, R.array.eur_toplist_symbols, new Exchange[]{Exchange.cccagg}, new Section[]{new Section(Exchange.cccagg, CoinKind.none)}, R.drawable.ic_nav_eur, R.anim.progress_eur_toplist),
    btc_toplist(R.string.nav_btc_toplist, R.string.header_btc_toplist, R.string.tab_btc_toplist, R.string.summary_btc_toplist, R.id.nav_btc_toplist, R.color.colorBtcToplist, R.color.colorBtcToplistDark, R.color.state_nav_btc_toplist, R.array.btc_toplist_symbols, new Exchange[]{Exchange.cccagg}, new Section[]{new Section(Exchange.cccagg, CoinKind.none)}, R.drawable.ic_nav_btc, R.anim.progress_btc_toplist),
    edit_tabs(R.string.nav_edit_tabs, R.string.header_edit_tabs, R.string.tab_edit_tabs, R.string.summary_edit_tabs, R.id.nav_edit_tabs, R.color.colorEditTabs, R.color.colorEditTabsDark, R.color.state_nav_edit_tabs, -1, new Exchange[]{}, new Section[]{}, R.drawable.ic_nav_playlist_add, R.anim.progress_edit_tabs);

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
    public Section[] sections;
    public int iconResId;
    public int progressDrawableResId;

    NavigationKind(int navStrResId, int headerStrResId, int tabStrResId, int summaryStrResId, int navResId, int colorResId, int colorDarkResId,
                   int colorStateResId, int symbolsResId, Exchange[] exchanges, Section[] sections, int iconResId, int progressDrawableResId) {
        this.navStrResId = navStrResId;
        this.headerStrResId = headerStrResId;
        this.tabStrResId = tabStrResId;
        this.titleStrResId = tabStrResId; // titleStrResId == tabStrResId
        this.summaryStrResId = summaryStrResId;
        this.navResId = navResId;
        this.colorResId = colorResId;
        this.colorDarkResId = colorDarkResId;
        this.colorStateResId = colorStateResId;
        this.symbolsResId = symbolsResId;
        this.exchanges = exchanges;
        this.sections = sections;
        this.iconResId = iconResId;
        this.progressDrawableResId = progressDrawableResId;
    }

    public boolean isHideable() {
        return !(this == home || this == edit_tabs);
    }

    public boolean isShowable() {
        return !(this == bitflyer || this == zaif);
    }

    public boolean isVisible(Context context) {
        return !isHideable() || (isShowable() && PrefHelper.isVisibleTab(context, this));
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
        return !(this == eur_toplist || this == btc_toplist || this == usd_toplist || this == bitflyer || this == zaif);
    }

    public static NavigationKind valueByNavResId(int navResId) {
        for (NavigationKind kind : values()) {
            if (kind.navResId == navResId) {
                return kind;
            }
        }
        return null;
    }

    public boolean isToplist() {
        return name().endsWith("toplist");
    }

    public static NavigationKind[] toplistValues() {
        List<NavigationKind> list = new ArrayList<>();
        for (NavigationKind kind : values()) {
            if (kind.isToplist()) {
                list.add(kind);
            }
        }
        return list.toArray(new NavigationKind[list.size()]);
    }

    public String getToSymbol() {
        String symbol;

        if (isToplist()) {
            symbol = name().substring(0, 3).toUpperCase();
        } else {
            switch (this) {
                case home:
                case japan:
                case bitflyer:
                case coincheck:
                case zaif:
                    symbol = "JPY";
                    break;
                default:
                    RuntimeException e = new RuntimeException("Invalid kind " + name());
                    CKLog.e("NavigationKind", e);
                    throw e;
            }
        }

        return symbol;
    }

    public static NavigationKind getDefault() {
        return home;
    }
}
