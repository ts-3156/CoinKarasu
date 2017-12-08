package com.example.toolbartest.chart;

import android.graphics.Color;

import com.example.toolbartest.cryptocompare.data.History;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
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
import java.util.concurrent.TimeUnit;

public class CoinLineChart {
    private LineChart chart;
    private String kind;

    public CoinLineChart(LineChart chart, String kind) {
        this.chart = chart;
        this.kind = kind;
    }

    private SimpleDateFormat getSimpleDateFormat(String kind) {
        SimpleDateFormat formatter = null;

        switch (kind) {
            case "hour":
                formatter = new SimpleDateFormat("HH:mm");
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

    private long convert(long value) {
        long converted;

        switch (kind) {
            case "hour":
            case "day":
                converted = TimeUnit.MINUTES.toMillis(value);
                break;
            default:
                converted = TimeUnit.HOURS.toMillis(value);
        }

        return converted;
    }

    private long inverseConvert(long value) {
        long converted;

        switch (kind) {
            case "hour":
            case "day":
                converted = TimeUnit.MILLISECONDS.toMinutes(value);
                break;
            default:
                converted = TimeUnit.MILLISECONDS.toHours(value);
        }

        return converted;
    }

    private class ValueFormatter implements IAxisValueFormatter {
        private SimpleDateFormat format;

        ValueFormatter(SimpleDateFormat format) {
            this.format = format;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long millis = convert((long) value);
            return format.format(new Date(millis));
        }
    }

    public void initialize() {
        chart.getDescription().setEnabled(false);
//        chart.setTouchEnabled(true);
//        chart.setDragDecelerationFrictionCoef(0.9f);
//        chart.setDragEnabled(true);
//        chart.setScaleEnabled(true);
//        chart.setDrawGridBackground(false);
//        chart.setHighlightPerDragEnabled(true);
//        chart.setBackgroundColor(Color.WHITE);
//        chart.setViewPortOffsets(0f, 0f, 0f, 0f);

        chart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
////        xAxis.setTypeface(mTfLight);
//        xAxis.setTextSize(14f);
//        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
//        xAxis.setTextColor(Color.rgb(255, 192, 56));
//        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new ValueFormatter(getSimpleDateFormat(kind)));

        YAxis leftAxis = chart.getAxisLeft();
//        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
////        leftAxis.setTypeface(mTfLight);
//        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(true);
//        leftAxis.setGranularityEnabled(true);
//        leftAxis.setAxisMinimum(0f);
//        leftAxis.setAxisMaximum(170f);
//        leftAxis.setYOffset(-9f);
//        leftAxis.setTextColor(Color.rgb(255, 192, 56));
//
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    public void updateValueFormatter() {
        chart.getXAxis().setValueFormatter(new ValueFormatter(getSimpleDateFormat(kind)));
    }

    public void setData(ArrayList<History> histories) {
        ArrayList<Entry> values = new ArrayList<>(histories.size());

        for (History history : histories) {
            values.add(new Entry(inverseConvert(history.getTime() * 1000), (float) history.getClose()));
        }

        LineDataSet set = new LineDataSet(values, "DataSet 1");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setValueTextColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(1.5f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawCircleHole(false);

        LineData data = new LineData(set);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        chart.setData(data);
    }

    public void invalidate() {
        chart.invalidate();
    }


    public LineChart getChart() {
        return chart;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
