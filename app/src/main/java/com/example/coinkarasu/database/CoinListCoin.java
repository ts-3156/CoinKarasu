package com.example.coinkarasu.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import com.example.coinkarasu.coins.Coin;

import org.json.JSONException;
import org.json.JSONObject;

@Entity(tableName = "coin_list_coins", indices = {@Index(value = {"symbol"}, unique = true)})
public class CoinListCoin {

    public CoinListCoin() {
    }

    public CoinListCoin(Coin coin) {
        ccid = coin.getId();
        url = coin.getUrl();
        imageUrl = coin.getImageUrl();
        name = coin.getName();
        symbol = coin.getSymbol();
        coinName = coin.getCoinName();
        fullName = coin.getFullName();
        algorithm = coin.getAlgorithm();
        proofType = coin.getProofType();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("Id", ccid);
            json.put("Url", url);
            json.put("ImageUrl", imageUrl);
            json.put("Name", name);
            json.put("Symbol", symbol);
            json.put("CoinName", coinName);
            json.put("FullName", fullName);
            json.put("Algorithm", algorithm);
            json.put("ProofType", proofType);
        } catch (JSONException e) {
            Log.e("toJson", e.getMessage());
        }

        return json;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "ccid")
    private int ccid;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "symbol")
    private String symbol;

    @ColumnInfo(name = "coin_name")
    private String coinName;

    @ColumnInfo(name = "full_name")
    private String fullName;

    @ColumnInfo(name = "algorithm")
    private String algorithm;

    @ColumnInfo(name = "proof_type")
    private String proofType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCcid() {
        return ccid;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCoinName() {
        return coinName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getProofType() {
        return proofType;
    }

    public void setCcid(int ccid) {
        this.ccid = ccid;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setProofType(String proofType) {
        this.proofType = proofType;
    }
}