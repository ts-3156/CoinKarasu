package com.coinkarasu.tasks;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.History;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetHistoryMonthTask extends GetHistoryTaskBase {

    public GetHistoryMonthTask(Client client) {
        super(client);
    }

    @Override
    protected List<History> doInBackground(Integer... params) {
//        histories = client.getHistoryDay(fromSymbol, toSymbol, 30);
        List<History> histories = client.getHistoryHour(fromSymbol, toSymbol, (int) TimeUnit.DAYS.toHours(30), 10, cacheMode);
        if (histories == null || histories.isEmpty()) {
            return histories;
        }

        List<History> histories2 = client.getHistoryHour(fromSymbol, toSymbol, 6, cacheMode);
        if (histories2 == null || histories2.isEmpty()) {
            return histories;
        }

        histories.addAll(histories2);

        return histories;
    }
}
