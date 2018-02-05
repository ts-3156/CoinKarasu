package com.coinkarasu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.CoinKind;
import com.coinkarasu.activities.etc.Exchange;
import com.coinkarasu.activities.etc.Section;
import com.coinkarasu.animator.PriceAnimator;
import com.coinkarasu.animator.PriceDiffAnimator;
import com.coinkarasu.animator.TrendAnimator;
import com.coinkarasu.coins.Coin;
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
import com.coinkarasu.utils.CKDateUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PeriodicalUpdater;
import com.coinkarasu.utils.PrefHelper;
import com.coinkarasu.utils.Tutorial;

import java.util.List;


public class PriceOverviewFragment extends Fragment implements
        PeriodicalUpdater.PeriodicalTask,
        GetPricesByExchangeTaskBase.Listener,
        RelativeTimeSpanTextView.TimeProvider {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "PriceOverviewFragment";
    private static final String STATE_LAST_UPDATED_KEY = "lastUpdated";

    private Coin coin;
    private PeriodicalUpdater updater;
    private AggressiveProgressbar progressbar;
    private RelativeTimeSpanTextView timeSpan;
    private boolean isAnimStarted;
    private TextView priceView;
    private TextView priceDiffView;
    private TextView trendView;
    private ImageView trendIconView;

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

        if (getArguments() != null) {
            coin = Coin.buildBy(getArguments().getString("coinJson"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_price_overview, container, false);

        updater = new PeriodicalUpdater(this, PrefHelper.getSyncInterval(getActivity()));

        if (savedInstanceState != null) {
            updater.setLastUpdated(savedInstanceState.getLong(STATE_LAST_UPDATED_KEY), false);
        }

        progressbar = view.findViewById(R.id.progressbar);

        timeSpan = view.findViewById(R.id.relative_time_span);
        timeSpan.setTimeProvider(this);

        priceView = view.findViewById(R.id.price);
        priceDiffView = view.findViewById(R.id.price_diff);
        trendView = view.findViewById(R.id.trend);
        trendIconView = view.findViewById(R.id.trend_icon);

        isAnimStarted = false;
        updateCard(coin);

        return view;
    }

    public void startUpdating() {
        if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
            return;
        }

        new GetCccaggPricesTask(getContext(), Exchange.cccagg)
                .setFromSymbols(new String[]{coin.getSymbol()})
                .setToSymbol(coin.getToSymbol())
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void started(Exchange exchange, CoinKind coinKind) {
        progressbar.startAnimation();
    }

    @Override
    public void finished(Exchange exchange, CoinKind coinKind, List<Price> prices, boolean withWarning) {
        if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
            if (updater != null) {
                updater.stop("finished");
            }
            return;
        }

        updater.setLastUpdated(CKDateUtils.now(), true);
        timeSpan.updateText(true);

        if (prices == null || prices.isEmpty()) {
            if (DEBUG) CKLog.w(TAG, "finished() prices is null " + exchange + " " + coinKind);
            progressbar.stopAnimationWithError();
            return;
        }

        progressbar.stopAnimationDelayed(withWarning);

        Price price = prices.get(0);
        coin.setPrice(price.price);
        coin.setPriceDiff(price.priceDiff);
        coin.setTrend(price.trend);

        isAnimStarted = true;
        updateCard(coin);

        if (getView() != null) {
            Tutorial.showPriceOverviewTutorial((CoinActivity) getActivity(), getView().findViewById(R.id.container), coin);
        }
    }

    private void updateCard(Coin coin) {
        priceDiffView.setTextColor(getResources().getColor(new PriceColorFormat().format(coin.getPriceDiff())));
        trendView.setTextColor(getResources().getColor(new TrendColorFormat().format(coin.getTrend())));
        trendIconView.setImageResource(new TrendIconFormat().format(coin.getTrend()));

        if (isAnimStarted && PrefHelper.shouldAnimatePrice(getActivity())) {
            new PriceAnimator(coin, priceView).start();
            new PriceDiffAnimator(coin, priceDiffView).start();
            new TrendAnimator(coin, trendView).start();
        } else {
            priceView.setText(PriceFormat.getInstance(coin.getToSymbol()).format(coin.getPrice()));
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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        if (updater != null) {
            savedInstanceState.putLong(STATE_LAST_UPDATED_KEY, updater.getLastUpdated());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public long getLastUpdated(Section section) {
        if (updater != null) {
            return updater.getLastUpdated();
        } else {
            return -1L;
        }
    }
}
