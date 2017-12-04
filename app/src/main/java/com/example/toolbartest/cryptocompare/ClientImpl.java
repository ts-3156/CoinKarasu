package com.example.toolbartest.cryptocompare;

import android.app.Activity;
import android.util.Log;

import com.example.toolbartest.coins.CoinList;
import com.example.toolbartest.coins.CoinListImpl;
import com.example.toolbartest.utils.SnackbarHelper;
import com.example.toolbartest.utils.StringHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

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
            CoinListImpl.fetcher().setActivity(activity).setListener(new CoinListImpl.Listener() {
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
        String url = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=" + StringHelper.join(",", fromSymbols) + "&tsyms=" + toSymbol;

        new Request(activity, url).perform(new Request.Listener() {
            @Override
            public void finished(JSONObject response) {
                HashMap<String, Double> map = new HashMap<>();

                for (Iterator<String> it = response.keys(); it.hasNext(); ) {
                    String fromSymbol = it.next();
                    try {
                        map.put(fromSymbol, response.getJSONObject(fromSymbol).getDouble(toSymbol));
                    } catch (JSONException e) {
                    }

                }

                if (listener != null) {
                    listener.finished(map);
                }
            }
        });
    }

    public interface CoinListListener {
        void finished(CoinList coinList);
    }

    public interface CoinPricesListener {
        void finished(HashMap<String, Double> prices);
    }
}
