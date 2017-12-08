package com.example.toolbartest.tasks;

import com.example.toolbartest.cryptocompare.Client;

public class GetHistoryWeekTask extends GetHistoryTaskBase {

    public GetHistoryWeekTask(Client client) {
        super(client);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        histories = client.getHistoryHour(fromSymbol, toSymbol, 168); // 24 * 7
        return 200;
    }
}