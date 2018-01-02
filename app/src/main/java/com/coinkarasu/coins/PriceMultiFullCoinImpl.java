package com.coinkarasu.coins;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class PriceMultiFullCoinImpl implements PriceMultiFullCoin {
    private String market;
    private String fromSymbol;
    private String toSymbol;
    private double price;
    private long lastUpdate;
    private double lastVolume;
    private double lastVolumeTo;
    private double volumeDay;
    private double volumeDayTo;
    private double volume24Hour;
    private double volume24HourTo;
    private double openDay;
    private double highDay;
    private double lowDay;
    private double open24Hour;
    private double high24Hour;
    private double low24Hour;
    private String lastMarket;
    private double change24Hour;
    private double changePct24Hour;
    private double changeDay;
    private double changePctDay;
    private double supply;
    private double mktCap;
    private double totalVolume24h;
    private double totalVolume24hTo;

    public PriceMultiFullCoinImpl(JSONObject attrs) {
        try {
            market = attrs.getString("MARKET");
            fromSymbol = attrs.getString("FROMSYMBOL");
            toSymbol = attrs.getString("TOSYMBOL");
            price = attrs.getDouble("PRICE");
            lastUpdate = attrs.getLong("LASTUPDATE");
            lastVolume = attrs.getDouble("LASTVOLUME");
            lastVolumeTo = attrs.getDouble("LASTVOLUMETO");
//            volumeDay = attrs.getDouble("VOLUMEDAY");
//            volumeDayTo = attrs.getDouble("VOLUMEDAYTO");
            volume24Hour = attrs.getDouble("VOLUME24HOUR");
            volume24HourTo = attrs.getDouble("VOLUME24HOURTO");
//            openDay = attrs.getDouble("OPENDAY");
//            highDay = attrs.getDouble("HIGHDAY");
//            lowDay = attrs.getDouble("LOWDAY");
            open24Hour = attrs.getDouble("OPEN24HOUR");
            high24Hour = attrs.getDouble("HIGH24HOUR");
            low24Hour = attrs.getDouble("LOW24HOUR");
//            lastMarket = attrs.getString("LASTMARKET");
            change24Hour = attrs.getDouble("CHANGE24HOUR");
            changePct24Hour = attrs.getDouble("CHANGEPCT24HOUR");
            changeDay = attrs.getDouble("CHANGEDAY");
            changePctDay = attrs.getDouble("CHANGEPCTDAY");
            supply = attrs.getDouble("SUPPLY");
            mktCap = attrs.getDouble("MKTCAP");
            totalVolume24h = attrs.getDouble("TOTALVOLUME24H");
            totalVolume24hTo = attrs.getDouble("TOTALVOLUME24HTO");

        } catch (JSONException e) {
            Log.e("PriceMultiFullCoinImpl", e.getMessage() + ", " + attrs.toString());
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("MARKET", market);
            json.put("FROMSYMBOL", fromSymbol);
            json.put("TOSYMBOL", toSymbol);
            json.put("PRICE", price);
            json.put("LASTUPDATE", lastUpdate);
            json.put("LASTVOLUME", lastVolume);
            json.put("LASTVOLUMETO", lastVolumeTo);
            json.put("VOLUME24HOUR", volume24Hour);
            json.put("VOLUME24HOURTO", volume24HourTo);
            json.put("OPEN24HOUR", open24Hour);
            json.put("HIGH24HOUR", high24Hour);
            json.put("LOW24HOUR", low24Hour);
            json.put("CHANGE24HOUR", change24Hour);
            json.put("CHANGEPCT24HOUR", changePct24Hour);
            json.put("CHANGEDAY", changeDay);
            json.put("CHANGEPCTDAY", changePctDay);
            json.put("SUPPLY", supply);
            json.put("MKTCAP", mktCap);
            json.put("TOTALVOLUME24H", totalVolume24h);
            json.put("TOTALVOLUME24HTO", totalVolume24hTo);
        } catch (JSONException e) {
            Log.e("toJson", e.getMessage());
        }

        return json;
    }

    @Override
    public String getMarket() {
        return market;
    }

    @Override
    public String getFromSymbol() {
        return fromSymbol;
    }

    @Override
    public String getToSymbol() {
        return toSymbol;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public long geLastUpdate() {
        return lastUpdate;
    }

    @Override
    public double getLastVolume() {
        return lastVolume;
    }

    @Override
    public double getLastVolumeTo() {
        return lastVolumeTo;
    }

    @Override
    public double getVolumeDay() {
        return volumeDay;
    }

    @Override
    public double getVolumeDayTo() {
        return volumeDayTo;
    }

    @Override
    public double getVolume24Hour() {
        return volume24Hour;
    }

    @Override
    public double getVolume24HourTo() {
        return volume24HourTo;
    }

    @Override
    public double getOpenDay() {
        return openDay;
    }

    @Override
    public double getHighDay() {
        return highDay;
    }

    @Override
    public double getLowDay() {
        return lowDay;
    }

    @Override
    public double getOpen24Hour() {
        return open24Hour;
    }

    @Override
    public double getHigh24Hour() {
        return high24Hour;
    }

    @Override
    public double getLow24Hour() {
        return low24Hour;
    }

    @Override
    public String getLastMarket() {
        return lastMarket;
    }

    @Override
    public double getChange24Hour() {
        return change24Hour;
    }

    @Override
    public double getChangePct24Hour() {
        return changePct24Hour;
    }

    @Override
    public double getChangeDay() {
        return changeDay;
    }

    @Override
    public double getChangePctDay() {
        return changePctDay;
    }

    @Override
    public double getSupply() {
        return supply;
    }

    @Override
    public double getMktCap() {
        return mktCap;
    }

    @Override
    public double getTotalVolume24h() {
        return totalVolume24h;
    }

    @Override
    public double getTotalVolume24hTo() {
        return totalVolume24hTo;
    }
}
