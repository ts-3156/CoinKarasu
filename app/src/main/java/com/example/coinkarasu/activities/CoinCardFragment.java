package com.example.coinkarasu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.animator.PriceAnimator;
import com.example.coinkarasu.animator.PriceDiffAnimator;
import com.example.coinkarasu.animator.TrendAnimator;
import com.example.coinkarasu.animator.ValueAnimatorBase;
import com.example.coinkarasu.api.cryptocompare.ClientFactory;
import com.example.coinkarasu.api.cryptocompare.data.Price;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.CoinImpl;
import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.format.PriceColorFormat;
import com.example.coinkarasu.format.PriceFormat;
import com.example.coinkarasu.format.SignedPriceFormat;
import com.example.coinkarasu.format.SurroundedTrendValueFormat;
import com.example.coinkarasu.format.TrendColorFormat;
import com.example.coinkarasu.format.TrendIconFormat;
import com.example.coinkarasu.tasks.GetPriceTask;
import com.example.coinkarasu.utils.AutoUpdateTimer;
import com.example.coinkarasu.utils.PrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimerTask;


public class CoinCardFragment extends Fragment implements GetPriceTask.Listener {

    private String kind;
    private Coin coin;
    private AutoUpdateTimer autoUpdateTimer;

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

        getChildFragmentManager().beginTransaction()
                .replace(R.id.time_span_container, RelativeTimeSpanFragment.newInstance(), RelativeTimeSpanFragment.getTag(coin.getSymbol()))
                .commit();

        kind = "coin_card";
        updatePrice(view, coin, true);
        startAutoUpdate(true);

        return view;
    }

    private void startTask() {
        if (getActivity() == null) {
            return;
        }

        String toSymbol = getToSymbol();
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

    private void startAutoUpdate(boolean isRepeated) {
        if (autoUpdateTimer != null) {
            stopAutoUpdate();
        }

        String tag = getTimerTag(kind);
        if (tag == null) {
            return;
        }
        autoUpdateTimer = new AutoUpdateTimer(tag);

        if (isRepeated) {
            autoUpdateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startTask();
                }
            }, 0, 10000);
        } else {
            startTask();
        }
    }

    public void stopAutoUpdate() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }

    private String getTimerTag(String kind) {
        String suffix = getToSymbol();
        if (suffix == null) {
            return null;
        }
        return kind + "-" + suffix;
    }

    @Override
    public void started(String exchange, String fromSymbol, String toSymbol) {
        setProgressbarVisibility(View.VISIBLE);
    }

    @Override
    public void finished(Price price) {
        if (isDetached() || getActivity() == null) {
            stopAutoUpdate();
            return;
        }

        String tag = getTimerTag(kind);
        if (autoUpdateTimer == null || tag == null || !autoUpdateTimer.getTag().equals(tag)) {
            stopAutoUpdate();
            return;
        }

        PriceMultiFullCoin coin = price.getCoin();
        if (coin == null) {
            stopAutoUpdate();
            hideProgressbarDelayed();
            updateRelativeTimeSpanText();
            return;
        }

        this.coin.setPrice(coin.getPrice());
        this.coin.setPriceDiff(coin.getChange24Hour());
        this.coin.setTrend(coin.getChangePct24Hour() / 100.0);
        updatePrice(getView(), this.coin, false);
        hideProgressbarDelayed();
        updateRelativeTimeSpanText();

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

    private void hideProgressbarDelayed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setProgressbarVisibility(View.GONE);
            }
        }, ValueAnimatorBase.DURATION);
    }

    public void setProgressbarVisibility(int flag) {
        if (isDetached() || getView() == null) {
            return;
        }

        ImageView progressbar = getView().findViewById(R.id.progressbar);
        if (progressbar == null) {
            return;
        }

        if (flag == View.GONE) {
            progressbar.clearAnimation();
            progressbar.setImageResource(R.drawable.ic_refresh_stop);
        } else if (flag == View.VISIBLE) {
            progressbar.setImageResource(R.drawable.ic_refresh_rotate);
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
            progressbar.startAnimation(anim);
        }
    }

    private void updateRelativeTimeSpanText() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(RelativeTimeSpanFragment.getTag(coin.getSymbol()));
        if (fragment != null) {
            ((RelativeTimeSpanFragment) fragment).updateText(System.currentTimeMillis());
        }
    }

    private String getToSymbol() {
        if (getActivity() == null) {
            return null;
        }

        return PrefHelper.getToSymbol(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (autoUpdateTimer == null) {
            startAutoUpdate(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoUpdate();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAutoUpdate();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        kind = null;
        coin = null;
        autoUpdateTimer = null;
    }
}
