package com.example.coinkarasu.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.CoinImpl;
import com.example.coinkarasu.coins.PriceMultiFullCoin;
import com.example.coinkarasu.cryptocompare.ClientImpl;
import com.example.coinkarasu.cryptocompare.data.Price;
import com.example.coinkarasu.format.PriceAnimator;
import com.example.coinkarasu.format.TrendAnimator;
import com.example.coinkarasu.format.TrendColorFormat;
import com.example.coinkarasu.format.TrendIconFormat;
import com.example.coinkarasu.format.ValueAnimatorBase;
import com.example.coinkarasu.tasks.GetPriceTask;
import com.example.coinkarasu.utils.AutoUpdateTimer;
import com.example.coinkarasu.utils.PrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimerTask;


public class CoinCardFragment extends Fragment implements GetPriceTask.Listener {

    private OnFragmentInteractionListener listener;

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

        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
        ((TextView) view.findViewById(R.id.caption_left)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_right)).setTypeface(typeFace);

        kind = "coin_card";
        updatePrice(view, coin);
        startAutoUpdate(true);

        return view;
    }

    public void updateView() {
        if (isDetached() || getView() == null) {
            return;
        }

        if (autoUpdateTimer != null) {
            return;
        }

        startAutoUpdate(true);
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

        new GetPriceTask(new ClientImpl(getActivity()))
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
            return;
        }

        this.coin.setPrice(coin.getPrice());
        this.coin.setTrend(coin.getChangePct24Hour() / 100.0);
        updatePrice(getView(), this.coin);
        hideProgressbarDelayed();

        Log.d("UPDATED", kind + ", " + new Date().toString());
    }

    private void updatePrice(View view, Coin coin) {
        if (view == null) {
            return;
        }

        new PriceAnimator(coin, (TextView) view.findViewById(R.id.price)).start();

        TextView trendView = view.findViewById(R.id.trend);
        new TrendAnimator(coin, trendView).start();
        trendView.setTextColor(getResources().getColor(new TrendColorFormat().format(coin.getTrend())));
        ((ImageView) view.findViewById(R.id.trend_icon)).setImageResource(new TrendIconFormat().format(coin.getTrend()));
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

        View progressbar = getView().findViewById(R.id.progressbar);
        progressbar.setVisibility(flag);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        kind = null;
        coin = null;
        autoUpdateTimer = null;
    }

    public interface OnFragmentInteractionListener {
        void onLineChartKindChanged(String kind);
    }
}
