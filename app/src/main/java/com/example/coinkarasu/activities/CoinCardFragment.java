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

import java.util.TimerTask;


public class CoinCardFragment extends Fragment implements GetPriceTask.Listener {

    private OnFragmentInteractionListener listener;

    private String kind;
    private Coin coin;

    private int errorCount = 0;
    private AutoUpdateTimer autoUpdateTimer;

    public CoinCardFragment() {
    }

    public static CoinCardFragment newInstance(String kind, String coinJson) {
        CoinCardFragment fragment = new CoinCardFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind);
        args.putString("coinJson", coinJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = getArguments().getString("kind");
            String coinJson = getArguments().getString("coinJson");

            try {
                coin = CoinImpl.buildByJSONObject(new JSONObject(coinJson));
            } catch (JSONException e) {
                Log.d("onCreate", e.getMessage());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_card, container, false);

        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
        ((TextView) view.findViewById(R.id.caption_left)).setTypeface(typeFace);
        ((TextView) view.findViewById(R.id.caption_right)).setTypeface(typeFace);

        updatePrice(view, coin);
        startAutoUpdate(0, true);

        return view;
    }

    public void updateView() {
        if (isDetached() || getView() == null) {
            return;
        }

        if (autoUpdateTimer != null) {
            return;
        }

        startAutoUpdate(0, true);
    }

    private void startTask() {
        new GetPriceTask(new ClientImpl(getActivity()))
                .setFromSymbol(coin.getSymbol())
                .setToSymbol(coin.getToSymbol())
                .setExchange("cccagg")
                .setListener(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void startAutoUpdate(int delay, boolean isRepeated) {
        if (autoUpdateTimer != null) {
            stopAutoUpdate();
        }

        autoUpdateTimer = new AutoUpdateTimer(getTimerTag(kind));

        if (isRepeated) {
            autoUpdateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startTask();
                }
            }, delay, 10000);
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
            return;
        }

        PriceMultiFullCoin coin = price.getCoin();

        this.coin.setPrice(coin.getPrice());
        this.coin.setTrend(coin.getChangePct24Hour() / 100.0);
        updatePrice(getView(), this.coin);
        hideProgressbarDelayed();

        Log.d("UPDATED", kind);
    }

    private void updatePrice(View view, Coin coin) {
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
            startAutoUpdate(0, true);
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
    }

    public interface OnFragmentInteractionListener {
        void onLineChartKindChanged(String kind);
    }
}
