package com.coinkarasu.tasks;

import com.coinkarasu.api.cryptocompare.Client;

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