package com.coinkarasu.tasks;

import com.coinkarasu.api.cryptocompare.Client;
import com.coinkarasu.api.cryptocompare.data.History;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetHistoryYearTask extends GetHistoryTaskBase {

    public GetHistoryYearTask(Client client) {
        super(client);
    }

    @Override
    protected List<History> doInBackground(Integer... params) {
        List<History> histories = client.getHistoryDay(fromSymbol, toSymbol, (int) TimeUnit.DAYS.toDays(365), 5, cacheMode);
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
