package com.coinkarasu.api.cryptocompare.response;

import com.coinkarasu.api.cryptocompare.data.History;

import org.json.JSONArray;

import java.util.List;

public interface HistoryResponse {
    JSONArray getData();

    List<History> getHistories();
}
