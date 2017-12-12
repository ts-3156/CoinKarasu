package com.example.coinkarasu.tasks;

import com.example.coinkarasu.cryptocompare.Client;

public class GetHistoryDayTask extends GetHistoryTaskBase {

    public GetHistoryDayTask(Client client) {
        super(client);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        histories = client.getHistoryMinute(fromSymbol, toSymbol, 1440, 20); // 60 * 24
        return 200;
    }
}