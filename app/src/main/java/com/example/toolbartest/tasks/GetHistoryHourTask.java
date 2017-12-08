package com.example.toolbartest.tasks;

import com.example.toolbartest.cryptocompare.Client;

public class GetHistoryHourTask extends GetHistoryTaskBase {

    public GetHistoryHourTask(Client client) {
        super(client);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        histories = client.getHistoryMinute(fromSymbol, toSymbol, 60);
        return 200;
    }
}