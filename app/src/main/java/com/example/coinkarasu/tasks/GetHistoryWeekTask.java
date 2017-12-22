package com.example.coinkarasu.tasks;

import com.example.coinkarasu.api.cryptocompare.Client;

public class GetHistoryWeekTask extends GetHistoryTaskBase {

    public GetHistoryWeekTask(Client client) {
        super(client);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        histories = client.getHistoryHour(fromSymbol, toSymbol, 168, 2); // 24 * 7
        return 200;
    }
}