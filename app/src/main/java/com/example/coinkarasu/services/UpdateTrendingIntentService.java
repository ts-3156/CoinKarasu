package com.example.coinkarasu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.example.coinkarasu.activities.MainFragment;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.CoinImpl;
import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.cryptocompare.Client;
import com.example.coinkarasu.cryptocompare.ClientImpl;
import com.example.coinkarasu.cryptocompare.data.CoinListImpl;
import com.example.coinkarasu.cryptocompare.data.History;
import com.example.coinkarasu.cryptocompare.data.Prices;
import com.example.coinkarasu.data.Trending;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;

import static com.example.coinkarasu.activities.HomeTabFragment.Kind;

public class UpdateTrendingIntentService extends IntentService {

    private static final String TAG = UpdateTrendingIntentService.class.getSimpleName();

    public UpdateTrendingIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        ArrayList<String> toSymbols = new ArrayList<>();
//        for (MainActivity.Currency currency : MainActivity.Currency.values()) {
//            toSymbols.add(currency.name());
//        }
//
//        ArrayList<String> exchanges = new ArrayList<>();
//        for (ListViewFragment.Exchange exchange : ListViewFragment.Exchange.values()) {
//
//        }

        for (Kind kind : Kind.values()) {
            update(kind, "JPY", "zaif");
        }
    }

    private void update(Kind kind, String toSymbol, String exchange) {
        LinkedHashSet<String> uniqueSymbols = new LinkedHashSet<>();
        String[] array = getResources().getStringArray(MainFragment.NavigationKind.japan.symbolsResId);
        Collections.addAll(uniqueSymbols, array);

        Prices prices = new ClientImpl(this).getPrices(uniqueSymbols.toArray(new String[uniqueSymbols.size()]), toSymbol, exchange);
        ArrayList<PriceMultiFullCoin> baseCoins = prices.getCoins();
        ArrayList<Coin> coins = new ArrayList<>();

        for (PriceMultiFullCoin coin : baseCoins) {
            ArrayList<History> records = getHistories(kind, coin.getFromSymbol(), coin.getToSymbol(), exchange);
            if (records.isEmpty()) {
                continue;
            }

            History oldest = records.get(0);
            History latest = records.get(records.size() - 1);
            if (latest.getClose() - oldest.getClose() <= 0.0) {
                continue;
            }

            String imageUrl = CoinListImpl.getInstance(this).getCoinBySymbol(coin.getFromSymbol()).getImageUrl();
            Coin newCoin = CoinImpl.buildByAttrs(coin.getFromSymbol(), imageUrl);
            newCoin.setToSymbol(coin.getToSymbol());
            newCoin.setPrice(oldest.getClose());
            newCoin.setPrice(latest.getClose());
            newCoin.setTrend((latest.getClose() - oldest.getClose()) / oldest.getClose());
            coins.add(newCoin);
        }

        Collections.sort(coins, new Comparator<Coin>() {
            public int compare(Coin c1, Coin c2) {
                return c1.getTrend() > c2.getTrend() ? -1 : 1;
            }
        });

        Trending trending = new Trending(coins, kind);
        trending.saveToCache(this);
    }

    private ArrayList<History> getHistories(Kind kind, String fromSymbol, String toSymbol, String exchange) {
        Client client = new ClientImpl(this);
        ArrayList<History> records = new ArrayList<>();

        switch (kind) {
            case one_hour:
                records = client.getHistoryMinute(fromSymbol, toSymbol, 60, 1, exchange);
                break;
            case six_hours:
                records = client.getHistoryMinute(fromSymbol, toSymbol, 60 * 6, 1, exchange);
                break;
            case twelve_hours:
                records = client.getHistoryHour(fromSymbol, toSymbol, 12, 1, exchange);
                break;
            case twenty_four_hours:
                records = client.getHistoryHour(fromSymbol, toSymbol, 24, 1, exchange);
                break;
            case three_days:
                records = client.getHistoryDay(fromSymbol, toSymbol, 3, 1, exchange);
                break;
        }

        return records;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, UpdateTrendingIntentService.class);
        context.startService(intent);
    }
}