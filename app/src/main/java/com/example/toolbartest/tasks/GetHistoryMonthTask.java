package com.example.toolbartest.tasks;

import com.example.toolbartest.cryptocompare.Client;

public class GetHistoryMonthTask extends GetHistoryTaskBase {

    public GetHistoryMonthTask(Client client) {
        super(client);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
//        histories = client.getHistoryDay(fromSymbol, toSymbol, 30);
        histories = client.getHistoryHour(fromSymbol, toSymbol, 720, 2); // 30 * 24
        return 200;
    }
}