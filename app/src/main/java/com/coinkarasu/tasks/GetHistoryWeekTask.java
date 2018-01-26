package com.coinkarasu.tasks;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.History;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetHistoryWeekTask extends GetHistoryTaskBase {

    public GetHistoryWeekTask(Client client) {
        super(client);
    }

    @Override
    protected List<History> doInBackground(Integer... params) {
        return client.getHistoryHour(fromSymbol, toSymbol, (int) TimeUnit.DAYS.toHours(7), 2, cacheMode);
    }
}
