package com.example.coinkarasu.tasks;

import com.example.coinkarasu.cryptocompare.Client;

public class GetHistoryDayTask extends GetHistoryTaskBase {

    public GetHistoryDayTask(Client client) {
        super(client);
    }

    public GetHistoryDayTask(Client client, String exchange) {
        super(client);
        this.exchange = exchange;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        histories = client.getHistoryMinute(fromSymbol, toSymbol, 1440, 20, exchange); // 60 * 24
        return 200;
    }
}