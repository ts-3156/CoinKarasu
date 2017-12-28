package com.example.coinkarasu.tasks.by_exchange;

import android.content.Context;
import android.os.AsyncTask;

import com.example.coinkarasu.activities.etc.CoinKind;
import com.example.coinkarasu.activities.etc.Exchange;

import java.util.ArrayList;

public abstract class GetPricesByExchangeTaskBase extends AsyncTask<Integer, Integer, Integer> {
    protected Listener listener;
    protected Exchange exchange;
    protected CoinKind coinKind;

    protected GetPricesByExchangeTaskBase(Exchange exchange, CoinKind coinKind) {
        this.listener = null;
        this.exchange = exchange;
        this.coinKind = coinKind;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        throw new RuntimeException("Stub");
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (listener != null) {
            listener.started(exchange, coinKind);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        throw new RuntimeException("Stub");
    }

    public GetPricesByExchangeTaskBase setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public static GetPricesByExchangeTaskBase getInstance(Context context, Exchange exchange, CoinKind coinKind) {
        switch (exchange) {
            case bitflyer:
                switch (coinKind) {
                    case none:
                        return null;
                    case trading:
                        return new GetBitflyerTradingRatesTask(context);
                    case sales:
                        return null;
                }
            case coincheck:
                switch (coinKind) {
                    case none:
                        return null;
                    case trading:
                        return new GetCoincheckTradingRatesTask(context);
                    case sales:
                        return new GetCoincheckSalesRatesTask(context);
                }
            case zaif:
                switch (coinKind) {
                    case none:
                        return null;
                    case trading:
                        return null;
                    case sales:
                        return null;
                }
            default:
                throw new RuntimeException("Invalid Exchange and CoinKind " + exchange.name() + ", " + coinKind.name());
        }
    }

    public interface Listener {
        void started(Exchange exchange, CoinKind coinKind);

        void finished(Exchange exchange, CoinKind coinKind, ArrayList<Price> prices);
    }

}