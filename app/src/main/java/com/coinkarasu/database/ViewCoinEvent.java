package com.coinkarasu.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.utils.CKLog;

import java.util.Date;

@Entity(tableName = "view_coin_events", indices = {@Index(value = {"created"})})
public class ViewCoinEvent {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "ViewCoinEvent";

    public ViewCoinEvent() {
    }

    @Ignore
    public ViewCoinEvent(NavigationKind kind, Coin coin) {
        this.kind = kind.name();
        this.symbol = coin.getSymbol();
        this.created = new Date();
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "kind")
    private String kind;

    @ColumnInfo(name = "symbol")
    private String symbol;

    @ColumnInfo(name = "created")
    private Date created;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
