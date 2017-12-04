package com.example.toolbartest.cryptocompare;

import android.app.Activity;
import android.util.Log;

import com.example.toolbartest.coins.CoinList;
import com.example.toolbartest.coins.CoinListImpl;
import com.example.toolbartest.utils.StringHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class ClientImpl implements Client {
    private Activity activity;

    public ClientImpl(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void getCoinList(final CoinListListener listener) {
        if (CoinListResponseImpl.cacheExists(activity)) {
            Log.d("CoinList cache hit", CoinListResponseImpl.lastModified(activity).toString());
            CoinList coinList = CoinListImpl.builder().setActivity(activity).build();
            if (listener != null) {
                listener.finished(coinList);
            }
            CoinListImpl.fetcher().setActivity(activity).fetch();
        } else {
            Log.d("CoinList cache", "Not found");
            CoinListImpl.fetcher().setActivity(activity).setListener(new CoinList.Listener() {
                @Override
                public void finished(CoinList coinList) {
                    if (listener != null) {
                        listener.finished(coinList);
                    }
                }
            }).fetch();
        }
    }

    @Override
    public void getCoinPrices(String[] fromSymbols, final String toSymbol, final CoinPricesListener listener) {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=" + StringHelper.join(",", fromSymbols) + "&tsyms=" + toSymbol;

        new Request(activity, url).perform(new Request.Listener() {
            @Override
            public void finished(JSONObject response) {
                HashMap<String, Double> prices = new HashMap<>();
                HashMap<String, Double> trends = new HashMap<>();

                try {
                    JSONObject raw = response.getJSONObject("RAW");

                    for (Iterator<String> it = raw.keys(); it.hasNext(); ) {
                        String fromSymbol = it.next();
                        JSONObject attrs = raw.getJSONObject(fromSymbol).getJSONObject(toSymbol);

                        prices.put(fromSymbol, attrs.getDouble("PRICE"));
                        trends.put(fromSymbol, attrs.getDouble("CHANGEPCT24HOUR") / 100.0);

                    }
                } catch (JSONException e) {
                }

                if (listener != null) {
                    listener.finished(prices, trends);
                }
            }
        });
    }

}
