package com.coinkarasu.tasks;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.History;

import java.util.List;

public class GetHistoryHourTask extends GetHistoryTaskBase {

    public GetHistoryHourTask(Client client) {
        super(client);
    }

    @Override
    protected List<History> doInBackground(Integer... params) {
        return client.getHistoryMinute(fromSymbol, toSymbol, 60);
    }
}