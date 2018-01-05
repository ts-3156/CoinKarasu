package com.coinkarasu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.animator.PriceAnimator;
import com.coinkarasu.animator.PriceDiffAnimator;
import com.coinkarasu.animator.TrendAnimator;
import com.coinkarasu.animator.ValueAnimatorBase;
import com.coinkarasu.coins.Coin;
import com.coinkarasu.coins.CoinImpl;
import com.coinkarasu.custom.AggressiveProgressbar;
import com.coinkarasu.custom.RelativeTimeSpanTextView;
import com.coinkarasu.format.PriceColorFormat;
import com.coinkarasu.format.PriceFormat;
import com.coinkarasu.format.SignedPriceFormat;
import com.coinkarasu.format.SurroundedTrendValueFormat;
import com.coinkarasu.format.TrendColorFormat;
import com.coinkarasu.format.TrendIconFormat;
import com.coinkarasu.tasks.by_exchange.GetCccaggPricesTask;
import com.coinkarasu.tasks.by_exchange.GetPricesByExchangeTaskBase;
import com.coinkarasu.tasks.by_exchange.data.Price;
import com.coinkarasu.utils.Log;
import com.coinkarasu.utils.PeriodicalUpdater;
import com.coinkarasu.utils.PrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class PriceOverviewFragment extends Fragment implements
        PeriodicalUpdater.PeriodicallyRunnable, GetPricesByExchangeTaskBase.Listener {

    private static final boolean DEBUG = true;
    private static final String TAG = "PriceOverviewFragment";

    private Coin coin;
    private PeriodicalUpdater updater;
    private Log logger;

    public PriceOverviewFragment() {
    }

    public static PriceOverviewFragment newInstance(Coin coin) {
        PriceOverviewFragment fragment = new PriceOverviewFragment();
        Bundle args = new Bundle();
        args.putString("coinJson", coin.toJson().toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = new Log(getActivity());

        if (getArguments() != null) {
            String coinJson = getArguments().getString("coinJson");

            try {
                coin = CoinImpl.buildByAttrs(new JSONObject(coinJson));
            } catch (JSONException e) {
                logger.e(TAG, e);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_price_overview, container, false);

        updateCard(view, coin, true);
        updater = new PeriodicalUpdater(this, PrefHelper.getSyncInterval(getActivity()));
        updater.start("onCreateView");

        return view;
    }

    public void startTask() {
        if (getActivity() == null || getView() == null) {
            return;
        }

        new GetCccaggPricesTask(getContext(), Exchange.cccagg)
                .setFromSymbols(new String[]{coin.getSymbol()})
                .setToSymbol(coin.getToSymbol())
                .setExchange(Exchange.cccagg.name())
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void started(Exchange exchange, CoinKind coinKind) {
        if (getView() != null) {
            ((AggressiveProgressbar) getView().findViewById(R.id.progressbar)).startAnimation();
        }
    }

    @Override
    public void finished(Exchange exchange, CoinKind coinKind, ArrayList<Price> prices) {
        if (isDetached() || getActivity() == null || getView() == null) {
            if (updater != null) {
                updater.stop("finished");
            }
            return;
        }

        ((AggressiveProgressbar) getView().findViewById(R.id.progressbar)).stopAnimationDelayed(ValueAnimatorBase.DURATION);
        ((RelativeTimeSpanTextView) getView().findViewById(R.id.relative_time_span)).updateText();

        Price price = prices.get(0);
        coin.setPrice(price.price);
        coin.setPriceDiff(price.priceDiff);
        coin.setTrend(price.trend);
        updateCard(getView(), coin, false);

        if (DEBUG) logger.d(TAG, "finished()");
    }

    private void updateCard(View view, Coin coin, boolean isFirstUpdate) {
        if (view == null) {
            return;
        }

        TextView priceView = view.findViewById(R.id.price);
        TextView priceDiffView = view.findViewById(R.id.price_diff);
        TextView trendView = view.findViewById(R.id.trend);

        priceDiffView.setTextColor(getResources().getColor(new PriceColorFormat().format(coin.getPriceDiff())));
        trendView.setTextColor(getResources().getColor(new TrendColorFormat().format(coin.getTrend())));

        ((ImageView) view.findViewById(R.id.trend_icon)).setImageResource(new TrendIconFormat().format(coin.getTrend()));

        if (isFirstUpdate || !PrefHelper.isAnimEnabled(getActivity())) {
            priceView.setText(new PriceFormat(coin.getToSymbol()).format(coin.getPrice()));
            priceDiffView.setText(new SignedPriceFormat(coin.getToSymbol()).format(coin.getPriceDiff()));
            trendView.setText(new SurroundedTrendValueFormat().format(coin.getTrend()));
        } else {
            new PriceAnimator(coin, priceView).start();
            new PriceDiffAnimator(coin, priceDiffView).start();
            new TrendAnimator(coin, trendView).start();
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
        coin = null;
        updater = null;
    }
}
