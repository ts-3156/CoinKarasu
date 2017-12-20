package com.example.coinkarasu.cryptocompare.response;

import com.example.coinkarasu.cryptocompare.data.History;

import org.json.JSONArray;

import java.util.ArrayList;

public interface HistoryResponse extends Cacheable {
    JSONArray getData();

    ArrayList<History> getHistories();
}
