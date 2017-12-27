package com.example.coinkarasu.activities;

import com.example.coinkarasu.R;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.CoinImpl;
import com.example.coinkarasu.coins.SectionHeaderCoinImpl;

public enum Exchange {
    bitflyer(R.array.bitflyer_trading_symbols, R.array.bitflyer_sales_symbols, R.string.header_name_bitflyer, R.string.trading_header_name_bitflyer, R.string.sales_header_name_bitflyer),
    coincheck(R.array.coincheck_trading_symbols, R.array.coincheck_sales_symbols, R.string.header_name_coincheck, R.string.trading_header_name_coincheck, R.string.sales_header_name_coincheck),
    zaif(R.array.zaif_trading_symbols, R.array.zaif_sales_symbols, R.string.header_name_zaif, R.string.trading_header_name_zaif, R.string.sales_header_name_zaif),
    cccagg(-1, -1, R.string.header_name_cccagg, R.string.trading_header_name_cccagg, R.string.sales_header_name_cccagg);

    public int tradingSymbolsResId;
    public int salesSymbolsResId;
    int headerNameResId;
    int tradingHeaderNameResId;
    int salesHeaderNameResId;

    Exchange(int tradingSymbolsResId, int salesSymbolsResId, int headerNameResId, int tradingHeaderNameResId, int salesHeaderNameResId) {
        this.tradingSymbolsResId = tradingSymbolsResId;
        this.salesSymbolsResId = salesSymbolsResId;
        this.headerNameResId = headerNameResId;
        this.tradingHeaderNameResId = tradingHeaderNameResId;
        this.salesHeaderNameResId = salesHeaderNameResId;
    }

    public int getHeaderNameResId(CoinImpl.Kind kind) {
        int id;
        switch (kind) {
            case trading:
                id = tradingHeaderNameResId;
                break;
            case sales:
                id = salesHeaderNameResId;
                break;
            default:
                id = headerNameResId;
        }
        return id;
    }

    public Coin createSectionHeaderCoin(CoinImpl.Kind kind) {
        return new SectionHeaderCoinImpl(this, kind);
    }
}
