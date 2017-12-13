package com.example.coinkarasu.coins;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CoinImpl implements Coin {

    private static final int ICON_SIZE = 96;

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
    private double trend;
    private String exchange;

    private double prevPrice;
    private double prevTrend;

    private CoinImpl(JSONObject attrs) {
        try {
            this.id = attrs.getInt("Id");
            this.url = attrs.getString("Url");
            this.imageUrl = attrs.getString("ImageUrl");
            this.name = attrs.getString("Name"); // BTC
            this.symbol = attrs.getString("Symbol"); // BTC
            this.coinName = attrs.getString("CoinName"); // Bitcoin
            this.fullName = attrs.getString("FullName"); // Bitcoin (BTC)
            this.algorithm = attrs.getString("Algorithm"); // SHA256
            this.proofType = attrs.getString("ProofType"); // PoW

            try {
                this.fullyPremined = attrs.getInt("FullyPremined"); // 0
                this.totalCoinSupply = attrs.getString("TotalCoinSupply"); // N/A or 21000000
                this.preminedValue = attrs.getString("PreminedValue"); // N/A
                this.totalCoinsFreeFloat = attrs.getString("TotalCoinsFreeFloat"); // N/A
                this.sortOrder = attrs.getInt("SortOrder"); // 1
            } catch (JSONException e) {
                Log.e("CoinImpl", e.getMessage());
                Log.e("CoinImpl", attrs.toString());
            }

            toSymbol = null;
            price = 0.0;
            trend = 0.0;
            exchange = null;
            prevPrice = 0.0;
            prevTrend = 0.0;

            if (attrs.has("toSymbol")) toSymbol = attrs.getString("toSymbol");
            if (attrs.has("price")) price = attrs.getDouble("price");
            if (attrs.has("trend")) trend = attrs.getDouble("trend");
            if (attrs.has("exchange")) exchange = attrs.getString("exchange");
            if (attrs.has("prevPrice")) prevPrice = attrs.getDouble("prevPrice");
            if (attrs.has("prevTrend")) prevTrend = attrs.getDouble("prevTrend");
        } catch (JSONException e) {
            Log.e("CoinImpl", e.getMessage());
            Log.e("CoinImpl", attrs.toString());
        }

    }

    public static Coin buildByJSONObject(JSONObject attrs) {
        return new CoinImpl(attrs);
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
        return BASE_URL + imageUrl + "?width=" + ICON_SIZE;
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
    public double getPrice() {
        return price;
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
            json.put("trend", trend);
            json.put("exchange", exchange);
            json.put("prevPrice", prevPrice);
            json.put("prevTrend", prevTrend);
        } catch (JSONException e) {
            Log.d("toJson", e.getMessage());
        }

        return json;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }
}
