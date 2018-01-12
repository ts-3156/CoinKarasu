package com.coinkarasu.tasks.by_exchange;

import android.content.Context;
import android.os.AsyncTask;

import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.tasks.by_exchange.data.Price;

import java.util.List;

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
                        return new GetBitflyerTradingRatesTask(context);
                    case trading:
                        return null;
                    case sales:
                        return null;
                }
            case coincheck:
                switch (coinKind) {
                    case none:
                        return new GetCoincheckTradingRatesTask(context, coinKind);
                    case trading:
                        return new GetCoincheckTradingRatesTask(context, coinKind);
                    case sales:
                        return new GetCoincheckSalesRatesTask(context);
                }
            case zaif:
                switch (coinKind) {
                    case none:
                        return new GetZaifTradingRatesTask(context, coinKind);
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

        void finished(Exchange exchange, CoinKind coinKind, List<Price> prices);
    }

}