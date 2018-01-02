package com.coinkarasu.api.cryptocompare.response;

import com.coinkarasu.api.cryptocompare.data.History;

import org.json.JSONArray;

import java.util.ArrayList;

public interface HistoryResponse extends Cacheable {
    JSONArray getData();

    ArrayList<History> getHistories();
}
