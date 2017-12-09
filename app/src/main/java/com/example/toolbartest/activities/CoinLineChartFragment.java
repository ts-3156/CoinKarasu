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
import com.example.toolbartest.chart.CoinLineChart;
import com.example.toolbartest.cryptocompare.data.History;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;


public class CoinLineChartFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener listener;

    Button btn;
    String kind;
    CoinLineChart chart;

    public CoinLineChartFragment() {
    }

    public static CoinLineChartFragment newInstance(String kind) {
        CoinLineChartFragment fragment = new CoinLineChartFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = getArguments().getString("kind");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_line_chart, container, false);

        view.findViewById(R.id.chart_hour).setOnClickListener(this);
        view.findViewById(R.id.chart_day).setOnClickListener(this);
        view.findViewById(R.id.chart_week).setOnClickListener(this);
        view.findViewById(R.id.chart_month).setOnClickListener(this);
        view.findViewById(R.id.chart_year).setOnClickListener(this);

        btn = view.findViewById(R.id.chart_hour);
        btn.setBackgroundColor(Color.LTGRAY);

        chart = new CoinLineChart((LineChart) view.findViewById(R.id.line_chart));
        chart.initialize(kind);

        return view;
    }

    public void updateView(ArrayList<History> records) {
        if (isDetached() || getView() == null) {
            return;
        }

        chart.clear();

        chart = new CoinLineChart((LineChart) getView().findViewById(R.id.line_chart));
        chart.initialize(kind);
        chart.setData(records);
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
            case R.id.chart_hour:
                next = "hour";
                break;
            case R.id.chart_day:
                next = "day";
                break;
            case R.id.chart_week:
                next = "week";
                break;
            case R.id.chart_month:
                next = "month";
                break;
            case R.id.chart_year:
                next = "year";
                break;
            default:
                next = "hour";
        }

        if (!next.equals(kind)) {
            Button nextBtn = (Button) view;
            btn.setBackgroundColor(Color.WHITE);
            nextBtn.setBackgroundColor(Color.LTGRAY);
            btn = nextBtn;

            kind = next;

            if (listener != null) {
                listener.onLineChartKindChanged(kind);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onLineChartKindChanged(String kind);
    }
}
