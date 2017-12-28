package com.example.coinkarasu.coins;

import com.example.coinkarasu.activities.etc.Exchange;
import com.example.coinkarasu.activities.etc.CoinKind;

import org.json.JSONObject;

public class SectionHeaderCoinImpl implements SectionHeaderCoin, Coin {
    private Exchange exchange;
    private CoinKind coinKind;

    public SectionHeaderCoinImpl(Exchange exchange, CoinKind coinKind) {
        this.exchange = exchange;
        this.coinKind = coinKind;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return null;
    }

    @Override
    public String getFullImageUrl() {
        return null;
    }

    @Override
    public String getLargeImageUrl() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getSymbol() {
        return null;
    }

    @Override
    public void setPrice(double price) {
    }

    @Override
    public void setPriceDiff(double trend) {
    }

    @Override
    public double getPrice() {
        return 0.0;
    }

    @Override
    public double getPriceDiff() {
        return 0.0;
    }

    @Override
    public long geLastUpdate() {
        return 0;
    }

    @Override
    public double getLastVolume() {
        return 0;
    }

    @Override
    public double getLastVolumeTo() {
        return 0;
    }

    @Override
    public double getVolumeDay() {
        return 0;
    }

    @Override
    public double getVolumeDayTo() {
        return 0;
    }

    @Override
    public double getVolume24Hour() {
        return 0;
    }

    @Override
    public double getVolume24HourTo() {
        return 0;
    }

    @Override
    public double getOpenDay() {
        return 0;
    }

    @Override
    public double getHighDay() {
        return 0;
    }

    @Override
    public double getLowDay() {
        return 0;
    }

    @Override
    public double getOpen24Hour() {
        return 0;
    }

    @Override
    public double getHigh24Hour() {
        return 0;
    }

    @Override
    public double getLow24Hour() {
        return 0;
    }

    @Override
    public String getLastMarket() {
        return null;
    }

    @Override
    public double getChange24Hour() {
        return 0;
    }

    @Override
    public double getChangePct24Hour() {
        return 0;
    }

    @Override
    public double getChangeDay() {
        return 0;
    }

    @Override
    public double getChangePctDay() {
        return 0;
    }

    @Override
    public double getSupply() {
        return 0;
    }

    @Override
    public double getMktCap() {
        return 0;
    }

    @Override
    public double getTotalVolume24h() {
        return 0;
    }

    @Override
    public double getTotalVolume24hTo() {
        return 0;
    }

    @Override
    public void setTrend(double trend) {
    }

    @Override
    public double getTrend() {
        return 0.0;
    }

    @Override
    public String getCoinName() {
        return null;
    }

    @Override
    public String getFullName() {
        return null;
    }

    @Override
    public String getAlgorithm() {
        return null;
    }

    @Override
    public String getProofType() {
        return null;
    }

    @Override
    public int getFullyPremined() {
        return 0;
    }

    @Override
    public String getTotalCoinSupply() {
        return null;
    }

    @Override
    public String getPreMinedValue() {
        return null;
    }

    @Override
    public String getTotalCoinsFreeFloat() {
        return null;
    }

    @Override
    public int getSortOrder() {
        return 0;
    }

    @Override
    public String getMarket() {
        return null;
    }

    @Override
    public String getFromSymbol() {
        return null;
    }

    @Override
    public String getToSymbol() {
        return null;
    }

    @Override
    public void setToSymbol(String toSymbol) {
    }

    @Override
    public String getExchange() {
        return exchange.name();
    }

    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public void setExchange(String exchange) {
    }

    @Override
    public String toString() {
        return exchange.name();
    }

    @Override
    public double getPrevPrice() {
        return 0.0;
    }

    @Override
    public double getPrevPriceDiff() {
        return 0;
    }

    @Override
    public double getPrevTrend() {
        return 0.0;
    }

    @Override
    public JSONObject toJson() {
        return null;
    }

    @Override
    public boolean isSectionHeader() {
        return true;
    }

    @Override
    public int getHeaderNameResId() {
        return exchange.getHeaderNameResId(coinKind);
    }

    @Override
    public boolean isSalesCoin() {
        return coinKind == CoinKind.sales;
    }

    @Override
    public boolean isTradingCoin() {
        return coinKind == CoinKind.trading;
    }

    @Override
    public void setCoinKind(CoinKind coinKind) {
    }

    @Override
    public CoinKind getCoinKind() {
        return coinKind;
    }

}
