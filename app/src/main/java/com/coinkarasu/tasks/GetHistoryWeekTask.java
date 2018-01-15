package com.coinkarasu.tasks;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.History;

import java.util.List;

public class GetHistoryWeekTask extends GetHistoryTaskBase {

    public GetHistoryWeekTask(Client client) {
        super(client);
    }

    @Override
    protected List<History> doInBackground(Integer... params) {
        return client.getHistoryHour(fromSymbol, toSymbol, 168, 2); // 24 * 7
    }
}