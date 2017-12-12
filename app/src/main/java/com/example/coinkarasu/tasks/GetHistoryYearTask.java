package com.example.coinkarasu.tasks;

import com.example.coinkarasu.cryptocompare.Client;

public class GetHistoryYearTask extends GetHistoryTaskBase {

    public GetHistoryYearTask(Client client) {
        super(client);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        histories = client.getHistoryDay(fromSymbol, toSymbol, 365, 5);
        return 200;
    }
}