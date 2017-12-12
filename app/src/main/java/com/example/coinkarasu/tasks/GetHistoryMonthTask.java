package com.example.coinkarasu.tasks;

import com.example.coinkarasu.cryptocompare.Client;

public class GetHistoryMonthTask extends GetHistoryTaskBase {

    public GetHistoryMonthTask(Client client) {
        super(client);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
//        histories = client.getHistoryDay(fromSymbol, toSymbol, 30);
        histories = client.getHistoryHour(fromSymbol, toSymbol, 720, 10); // 30 * 24
        return 200;
    }
}