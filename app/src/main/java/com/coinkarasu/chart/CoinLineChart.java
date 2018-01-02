package com.coinkarasu.chart;

import android.graphics.Color;

import com.coinkarasu.api.cryptocompare.data.History;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CoinLineChart {
    private LineChart chart;

    private long offsetSeconds;

    public CoinLineChart(LineChart chart) {
        this.chart = chart;
        this.offsetSeconds = 0;
    }

    public void initialize(String kind) {
        replaceValueFormatter(kind);

        chart.getDescription().setEnabled(false);
//        chart.setViewPortOffsets(0f, 0f, 0f, 0f);

        chart.invalidate();

        chart.getLegend().setEnabled(false);
        chart.animateX(1000);
        // enable touch gestures
        chart.setTouchEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);

        // enable scaling and dragging
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new OffsetFormatter(getSimpleDateFormat(kind)));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(true);

        chart.getAxisRight().setEnabled(false);

        chart.setHighlightPerDragEnabled(false);
    }

    private void replaceValueFormatter(String kind) {
        chart.getXAxis().setValueFormatter(new OffsetFormatter(getSimpleDateFormat(kind)));
    }

    public void setData(ArrayList<History> records) {
        ArrayList<Entry> values = new ArrayList<>(records.size());

        offsetSeconds = records.get(0).getTime();

        for (int i = 0; i < records.size(); i++) {
            History history = records.get(i);
            long x = history.getTime() - offsetSeconds;
            values.add(new Entry(x, (float) history.getClose()));
        }

        LineDataSet set = new LineDataSet(values, "DataSet 1");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.JOYFUL_COLORS[0]);
        set.setLineWidth(1.5f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
//        set.setFillAlpha(255);
//        set.setFillColor(ColorTemplate.JOYFUL_COLORS[1]);
        set.setDrawCircleHole(false);
//        set.setDrawFilled(true);

        LineData data = new LineData(set);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

//        chart.setRenderer(new StackedLineChartRenderer(chart, chart.getAnimator(), chart.getViewPortHandler()));

        chart.setData(data);
    }

    public void setData(HashMap<String, ArrayList<History>> map) {
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        for (String exchange : map.keySet()) {
            ArrayList<History> records = map.get(exchange);
            if (records.isEmpty()) {
                continue;
            }

            ArrayList<Entry> values = new ArrayList<>(records.size());

            offsetSeconds = records.get(0).getTime();

            for (int i = 0; i < records.size(); i++) {
                History history = records.get(i);
                long x = history.getTime() - offsetSeconds;
                values.add(new Entry(x, (float) history.getClose()));
            }

            LineDataSet set = new LineDataSet(values, "DataSet 1");
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setColor(ColorTemplate.JOYFUL_COLORS[0]);
            set.setLineWidth(1.5f);
            set.setDrawCircles(false);
            set.setDrawValues(false);
//        set.setFillAlpha(255);
//        set.setFillColor(ColorTemplate.JOYFUL_COLORS[1]);
            set.setDrawCircleHole(false);
//        set.setDrawFilled(true);

            data.addDataSet(set);

//        chart.setRenderer(new StackedLineChartRenderer(chart, chart.getAnimator(), chart.getViewPortHandler()));
        }

        chart.setData(data);
    }

    public void invalidate() {
        chart.invalidate();
    }

    public void animateX() {
        chart.animateX(1000);
    }

    public void clear() {
        chart.fitScreen();
        chart.clear();
        chart = null;
    }

    private SimpleDateFormat getSimpleDateFormat(String kind) {
        SimpleDateFormat formatter = null;

        switch (kind) {
            case "hour":
                formatter = new SimpleDateFormat("HH:mm:ss");
                break;
            case "day":
                formatter = new SimpleDateFormat("HH:mm");
                break;
            case "week":
                formatter = new SimpleDateFormat("MM/dd");
                break;
            case "month":
                formatter = new SimpleDateFormat("MM/dd");
                break;
            case "year":
                formatter = new SimpleDateFormat("MM/dd");
                break;
            default:
                formatter = new SimpleDateFormat("MM/dd HH:mm");
        }

        return formatter;
    }

    private class OffsetFormatter implements IAxisValueFormatter {
        private SimpleDateFormat format;

        OffsetFormatter(SimpleDateFormat format) {
            this.format = format;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long millis = (long) (value + offsetSeconds) * 1000;
            return format.format(new Date(millis));
        }
    }
}
