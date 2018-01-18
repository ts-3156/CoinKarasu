package com.coinkarasu.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.coinkarasu.coins.Coin;
import com.coinkarasu.utils.CKLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

@Entity(tableName = "launch_events", indices = {@Index(value = {"created"})})
public class LaunchEvent {
    private static final boolean DEBUG = true;
    private static final String TAG = "LaunchEvent";

    public LaunchEvent() {
    }

    public LaunchEvent(String activityName, Date created) {
        this.activityName = activityName;
        this.created = created;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "activity_name")
    private String activityName;

    @ColumnInfo(name = "created")
    private Date created;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}