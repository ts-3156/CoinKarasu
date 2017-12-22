package com.example.coinkarasu.tasks;

import com.example.coinkarasu.api.cryptocompare.Client;

public class GetHistoryYearTask extends GetHistoryTaskBase {

    public GetHistoryYearTask(Client client) {
        super(client);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        histories = client.getHistoryDay(fromSymbol, toSymbol, 365, 5);
        histories.addAll(client.getHistoryHour(fromSymbol, toSymbol, 6));
        return 200;
    }
}