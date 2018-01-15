package com.coinkarasu.tasks;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.History;

import java.util.List;

public class GetHistoryDayTask extends GetHistoryTaskBase {

    public GetHistoryDayTask(Client client) {
        super(client);
    }

    public GetHistoryDayTask(Client client, String exchange) {
        super(client);
        this.exchange = exchange;
    }

    @Override
    protected List<History> doInBackground(Integer... params) {
        return client.getHistoryMinute(fromSymbol, toSymbol, 1440, 20, exchange); // 60 * 24
    }
}