package com.example.toolbartest.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.toolbartest.R;
import com.example.toolbartest.chart.CoinPieChart;
import com.example.toolbartest.cryptocompare.data.CoinSnapshot;
import com.example.toolbartest.cryptocompare.data.Exchange;
import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class CoinPieChartFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener listener;

    Button btn;
    String kind;
    CoinPieChart chart;

    public CoinPieChartFragment() {
    }

    public static CoinPieChartFragment newInstance(String kind) {
        CoinPieChartFragment fragment = new CoinPieChartFragment();
        Bundle args = new Bundle();
        args.putString("lineChartKind", kind);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = getArguments().getString("lineChartKind");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_pie_chart, container, false);

        view.findViewById(R.id.pie_chart_currency).setOnClickListener(this);
        view.findViewById(R.id.pie_chart_exchange).setOnClickListener(this);

        btn = view.findViewById(R.id.pie_chart_currency);
        btn.setBackgroundColor(Color.LTGRAY);

        chart = new CoinPieChart((PieChart) view.findViewById(R.id.pie_chart));
        chart.initialize();

        return view;
    }

    public void updateView(CoinSnapshot snapshot) {
        if (isDetached() || getView() == null) {
            return;
        }

        ArrayList<Exchange> exchanges = snapshot.getExchanges();

        Collections.sort(exchanges, new Comparator<Exchange>() {
            public int compare(Exchange ex1, Exchange ex2) {
                return ex1.getVolume24Hour() > ex2.getVolume24Hour() ? -1 : 1;
            }
        });

        double sum = 0.0;
        for (Exchange exchange : exchanges) {
            sum += exchange.getVolume24Hour();
        }
        sum *= 0.05;

        double others = 0.0;
        ArrayList<Double> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (Exchange exchange : exchanges) {
            double value = exchange.getVolume24Hour();
            if (value < sum) {
                others += value;
            } else {
                values.add(value);
                labels.add(exchange.getMarket());
            }
        }

        if (others > 0.0) {
            values.add(others);
            labels.add("others");
        }

        chart.clear();

        chart = new CoinPieChart((PieChart) getView().findViewById(R.id.pie_chart));
        chart.initialize();
        chart.setData(values, labels);
        chart.invalidate();
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

    @Override
    public void onClick(View view) {
        String next;

        switch (view.getId()) {
            case R.id.pie_chart_currency:
                next = "currency";
                break;
            case R.id.pie_chart_exchange:
                next = "exchange";
                break;
            default:
                next = "currency";
        }

        if (!next.equals(kind)) {
            Button nextBtn = (Button) view;
            btn.setBackgroundColor(Color.WHITE);
            nextBtn.setBackgroundColor(Color.LTGRAY);
            btn = nextBtn;

            kind = next;

            if (chart != null) {
//                chart.setKind(lineChartKind);
//                chart.replaceValueFormatter();

                if (listener != null) {
                    listener.onPieChartKindChanged(kind);
                }
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onPieChartKindChanged(String kind);
    }
}
