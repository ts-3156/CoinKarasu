package com.coinkarasu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.animator.PriceAnimator;
import com.coinkarasu.animator.PriceDiffAnimator;
import com.coinkarasu.animator.TrendAnimator;
import com.coinkarasu.animator.ValueAnimatorBase;
import com.coinkarasu.api.cryptocompare.ClientFactory;
import com.coinkarasu.api.cryptocompare.data.Price;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.coins.CoinImpl;
import com.coinkarasu.coins.PriceMultiFullCoin;
import com.coinkarasu.custom.AggressiveProgressbar;
import com.coinkarasu.custom.RelativeTimeSpanTextView;
import com.coinkarasu.format.PriceColorFormat;
import com.coinkarasu.format.PriceFormat;
import com.coinkarasu.format.SignedPriceFormat;
import com.coinkarasu.format.SurroundedTrendValueFormat;
import com.coinkarasu.format.TrendColorFormat;
import com.coinkarasu.format.TrendIconFormat;
import com.coinkarasu.tasks.GetPriceTask;
import com.coinkarasu.utils.PeriodicalUpdater;
import com.coinkarasu.utils.PrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class CoinCardFragment extends Fragment implements
        GetPriceTask.Listener,
        PeriodicalUpdater.PeriodicallyRunnable {

    private String kind;
    private Coin coin;
    private PeriodicalUpdater updater;

    public CoinCardFragment() {
    }

    public static CoinCardFragment newInstance(Coin coin) {
        CoinCardFragment fragment = new CoinCardFragment();
        Bundle args = new Bundle();
        args.putString("coinJson", coin.toJson().toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String coinJson = getArguments().getString("coinJson");

            try {
                coin = CoinImpl.buildByAttrs(new JSONObject(coinJson));
            } catch (JSONException e) {
                Log.e("onCreate", e.getMessage());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_card, container, false);

        kind = "coin_card";
        updatePrice(view, coin, true);
        updater = new PeriodicalUpdater(this, PrefHelper.getSyncInterval(getActivity()));
        updater.start("onCreateView");

        return view;
    }

    public void startTask() {
        if (getActivity() == null) {
            return;
        }

        String toSymbol = coin.getToSymbol();
        if (toSymbol == null) {
            return;
        }
        coin.setToSymbol(toSymbol);

        new GetPriceTask(ClientFactory.getInstance(getActivity()))
                .setFromSymbol(coin.getSymbol())
                .setToSymbol(toSymbol)
                .setExchange("cccagg")
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void started(String exchange, String fromSymbol, String toSymbol) {
        ((AggressiveProgressbar) getView().findViewById(R.id.progressbar)).startAnimation();
    }

    @Override
    public void finished(Price price) {
        if (isDetached() || getActivity() == null) {
            if (updater != null) {
                updater.stop("finished");
            }
            return;
        }

        RelativeTimeSpanTextView timeSpan = getView().findViewById(R.id.relative_time_span);
        AggressiveProgressbar progressbar = getView().findViewById(R.id.progressbar);
        PriceMultiFullCoin coin = price.getCoin();
        if (coin == null) {
            if (updater != null) {
                updater.stop("finished");
            }
            progressbar.stopAnimationDelayed(ValueAnimatorBase.DURATION);
            timeSpan.updateText();
            return;
        }

        this.coin.setPrice(coin.getPrice());
        this.coin.setPriceDiff(coin.getChange24Hour());
        this.coin.setTrend(coin.getChangePct24Hour() / 100.0);
        updatePrice(getView(), this.coin, false);
        progressbar.stopAnimationDelayed(ValueAnimatorBase.DURATION);
        timeSpan.updateText();

        Log.d("UPDATED", kind + ", " + new Date().toString());
    }

    private void updatePrice(View view, Coin coin, boolean isFirstUpdate) {
        if (view == null) {
            return;
        }

        TextView priceView = view.findViewById(R.id.price);
        TextView priceDiffView = view.findViewById(R.id.price_diff);
        TextView trendView = view.findViewById(R.id.trend);

        priceDiffView.setTextColor(getResources().getColor(new PriceColorFormat().format(coin.getPriceDiff())));
        trendView.setTextColor(getResources().getColor(new TrendColorFormat().format(coin.getTrend())));

        ((ImageView) view.findViewById(R.id.trend_icon)).setImageResource(new TrendIconFormat().format(coin.getTrend()));

        if (!isFirstUpdate && PrefHelper.isAnimEnabled(getActivity())) {
            if (coin.getPrevPrice() != coin.getPrice()) {
                new PriceAnimator(coin, priceView).start();
            } else {
                priceView.setText(new PriceFormat(coin.getToSymbol()).format(coin.getPrice()));
            }
            if (coin.getPrevPriceDiff() != coin.getPriceDiff()) {
                new PriceDiffAnimator(coin, priceDiffView).start();
            } else {
                priceDiffView.setText(new SignedPriceFormat(coin.getToSymbol()).format(coin.getPriceDiff()));
            }
            if (coin.getPrevTrend() != coin.getTrend()) {
                new TrendAnimator(coin, trendView).start();
            } else {
                trendView.setText(new SurroundedTrendValueFormat().format(coin.getTrend()));
            }
        } else {
            priceView.setText(new PriceFormat(coin.getToSymbol()).format(coin.getPrice()));
            priceDiffView.setText(new SignedPriceFormat(coin.getToSymbol()).format(coin.getPriceDiff()));
            trendView.setText(new SurroundedTrendValueFormat().format(coin.getTrend()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (updater != null) {
            updater.start("onResume");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updater != null) {
            updater.stop("onPause");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        kind = null;
        coin = null;
        updater = null;
    }
}
