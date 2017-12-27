package com.example.coinkarasu.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.coins.CoinImpl;

import java.util.Timer;
import java.util.TimerTask;


public class RelativeTimeSpanFragment extends Fragment {

    private static final String STATE_TIME_KEY = "time";

    private Timer timer;
    private long time;

    public RelativeTimeSpanFragment() {
    }

    public static RelativeTimeSpanFragment newInstance() {
        return new RelativeTimeSpanFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relative_time_span, container, false);

        if (savedInstanceState != null) {
            time = savedInstanceState.getLong(STATE_TIME_KEY);
        } else {
            time = System.currentTimeMillis();
        }

        return view;
    }

    private void updateText() {
        if (isDetached() || getView() == null || getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isDetached() && getView() != null) {
                    String text = getRelativeTimeSpanString(time, System.currentTimeMillis());
                    ((TextView) getView().findViewById(R.id.time_span)).setText(text);
                }
            }
        });
    }

    private static String getRelativeTimeSpanString(long time, long now) {
        long diff = now - time;
        String str;

        if (diff < 1000) {
            str = "Just now";
        } else {
            str = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();
        }

        return str;
    }

    private void startAutoUpdate() {
        if (timer != null) {
            stopAutoUpdate();
        }

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateText();
            }
        }, 0, 5000);
    }

    private void stopAutoUpdate() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void updateText(long time) {
        this.time = time;
        stopAutoUpdate();
        startAutoUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateText();
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
        timer = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_TIME_KEY, time);
        super.onSaveInstanceState(savedInstanceState);
    }

    public static String getTag(String exchange) {
        return getTag(exchange, -1);
    }

    public static String getTag(ListViewFragment.Exchange exchange, CoinImpl.Kind kind) {
        return getTag(exchange.name(), exchange.getHeaderNameResId(kind));
    }

    public static String getTag(Coin coin) {
        if (!coin.isSectionHeader()) {
            throw new RuntimeException("Invalid coin " + coin.toString());
        }
        return getTag(coin.getExchange(), coin.getHeaderNameResId());
    }

    private static String getTag(String exchange, int headerNameResId) {
        return exchange + "-" + headerNameResId + "-time_span";
    }
}
