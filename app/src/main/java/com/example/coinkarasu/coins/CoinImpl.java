package com.example.coinkarasu.coins;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CoinImpl implements Coin {

    public enum Kind {
        trading, sales, none
    }

    private static final int ICON_SIZE = 96;
    private static final int ICON_LARGE_SIZE = 96;

    private static final String BASE_URL = "https://www.cryptocompare.com";

    private int id;
    private String url;
    private String imageUrl;
    private String name;
    private String symbol;
    private String coinName;
    private String fullName;
    private String algorithm;
    private String proofType;
    private int fullyPremined;
    private String totalCoinSupply;
    private String preminedValue;
    private String totalCoinsFreeFloat;
    private int sortOrder;

    private String toSymbol;
    private double price;
    private double priceDiff;
    private double trend;
    private String exchange;

    private double prevPrice;
    private double prevPriceDiff;
    private double prevTrend;

    private Kind kind;

    private CoinImpl(JSONObject attrs) {
        try {
            if (attrs.has("Id"))
                id = attrs.getInt("Id");
            if (attrs.has("Url"))
                url = attrs.getString("Url");
            if (attrs.has("ImageUrl"))
                imageUrl = attrs.getString("ImageUrl");
            if (attrs.has("Name"))
                name = attrs.getString("Name"); // BTC
            if (attrs.has("Symbol"))
                symbol = attrs.getString("Symbol"); // BTC
            if (attrs.has("CoinName"))
                coinName = attrs.getString("CoinName"); // Bitcoin
            if (attrs.has("FullName"))
                fullName = attrs.getString("FullName"); // Bitcoin (BTC)
            if (attrs.has("Algorithm"))
                algorithm = attrs.getString("Algorithm"); // SHA256
            if (attrs.has("ProofType"))
                proofType = attrs.getString("ProofType"); // PoW

            if (attrs.has("FullyPremined"))
                fullyPremined = attrs.getInt("FullyPremined"); // 0
            if (attrs.has("TotalCoinSupply"))
                totalCoinSupply = attrs.getString("TotalCoinSupply"); // N/A or 21000000

            if (attrs.has("PreminedValue")) {
                preminedValue = attrs.getString("PreminedValue"); // N/A
            } else if (attrs.has("PremMinedValue")) {
                preminedValue = attrs.getString("PreMinedValue");
            }

            if (attrs.has("TotalCoinsFreeFloat"))
                totalCoinsFreeFloat = attrs.getString("TotalCoinsFreeFloat"); // N/A
            if (attrs.has("SortOrder"))
                sortOrder = attrs.getInt("SortOrder"); // 1

            toSymbol = null;
            price = 0.0;
            priceDiff = 0.0;
            trend = 0.0;
            exchange = null;
            prevPrice = 0.0;
            prevPriceDiff = 0.0;
            prevTrend = 0.0;

            if (attrs.has("toSymbol")) toSymbol = attrs.getString("toSymbol");
            if (attrs.has("price")) price = attrs.getDouble("price");
            if (attrs.has("priceDiff")) priceDiff = attrs.getDouble("priceDiff");
            if (attrs.has("trend")) trend = attrs.getDouble("trend");
            if (attrs.has("exchange")) exchange = attrs.getString("exchange");
            if (attrs.has("prevPrice")) prevPrice = attrs.getDouble("prevPrice");
            if (attrs.has("prevPriceDiff")) prevPriceDiff = attrs.getDouble("prevPriceDiff");
            if (attrs.has("prevTrend")) prevTrend = attrs.getDouble("prevTrend");

            kind = Kind.trading;
        } catch (JSONException e) {
            Log.e("CoinImpl", e.getMessage() + ", " + attrs.toString());
        }

    }

    public static Coin buildByAttrs(JSONObject attrs) {
        return new CoinImpl(attrs);
    }

    public static Coin buildByAttrs(String symbol, String imageUrl) {
        JSONObject attrs = new JSONObject();

        try {
            attrs.put("Symbol", symbol);
            attrs.put("ImageUrl", imageUrl);
        } catch (JSONException e) {
        }

        return new CoinImpl(attrs);
    }

    public static Coin buildByCoinListCoin(com.example.coinkarasu.database.CoinListCoin coin) {
        return new CoinImpl(coin.toJson());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String getFullImageUrl() {
        return BASE_URL + imageUrl + "?width=" + ICON_SIZE;
    }

    @Override
    public String getLargeImageUrl() {
        return BASE_URL + imageUrl + "?width=" + ICON_LARGE_SIZE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public void setPrice(double price) {
        prevPrice = this.price;
        this.price = price;
    }

    @Override
    public void setPriceDiff(double priceDiff) {
        prevPriceDiff = this.priceDiff;
        this.priceDiff = priceDiff;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public double getPriceDiff() {
        return priceDiff;
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
        prevTrend = this.trend;
        this.trend = trend;
    }

    @Override
    public double getTrend() {
        return trend;
    }

    @Override
    public String getCoinName() {
        return coinName;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String getProofType() {
        return proofType;
    }

    @Override
    public int getFullyPremined() {
        return fullyPremined;
    }

    @Override
    public String getTotalCoinSupply() {
        return totalCoinSupply;
    }

    @Override
    public String getPreMinedValue() {
        return preminedValue;
    }

    @Override
    public String getTotalCoinsFreeFloat() {
        return totalCoinsFreeFloat;
    }

    @Override
    public int getSortOrder() {
        return sortOrder;
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
        return toSymbol;
    }

    @Override
    public void setToSymbol(String toSymbol) {
        this.toSymbol = toSymbol;
    }

    @Override
    public String getExchange() {
        return exchange;
    }

    @Override
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override
    public double getPrevPrice() {
        return prevPrice;
    }

    @Override
    public double getPrevPriceDiff() {
        return prevPriceDiff;
    }

    @Override
    public double getPrevTrend() {
        return prevTrend;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("Id", id);
            json.put("Url", url);
            json.put("ImageUrl", imageUrl);
            json.put("Name", name);
            json.put("Symbol", symbol);
            json.put("CoinName", coinName);
            json.put("FullName", fullName);
            json.put("Algorithm", algorithm);
            json.put("ProofType", proofType);
            json.put("FullyPremined", fullyPremined);
            json.put("TotalCoinSupply", totalCoinSupply);
            json.put("PreminedValue", preminedValue);
            json.put("TotalCoinsFreeFloat", totalCoinsFreeFloat);
            json.put("SortOrder", sortOrder);

            json.put("toSymbol", toSymbol);
            json.put("price", price);
            json.put("priceDiff", priceDiff);
            json.put("trend", trend);
            json.put("exchange", exchange);
            json.put("prevPrice", prevPrice);
            json.put("prevPriceDiff", prevPriceDiff);
            json.put("prevTrend", prevTrend);
        } catch (JSONException e) {
            Log.e("toJson", e.getMessage());
        }

        return json;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    @Override
    public int getHeaderNameResId() {
        return 0;
    }

    @Override
    public boolean isSalesCoin() {
        return kind == Kind.sales;
    }

    @Override
    public boolean isTradingCoin() {
        return kind == Kind.trading;
    }

    @Override
    public void setCoinKind(Kind kind) {
        this.kind = kind;
    }
}
