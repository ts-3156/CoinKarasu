package com.example.toolbartest.chart;

import android.graphics.Color;
import android.util.Log;

import com.example.toolbartest.cryptocompare.data.History;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CoinLineChart {
    private LineChart chart;
    private String kind;

    private long offset;

    public CoinLineChart(LineChart chart, String kind) {
        this.chart = chart;
        this.kind = kind;
        this.offset = 0;
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

    private class CutOffFormatter implements IAxisValueFormatter {
        private SimpleDateFormat format;

        CutOffFormatter(SimpleDateFormat format) {
            this.format = format;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long millis = convert((long) value);
            return format.format(new Date(millis));
        }
    }

    private class OffsetFormatter implements IAxisValueFormatter {
        private SimpleDateFormat format;

        OffsetFormatter(SimpleDateFormat format) {
            this.format = format;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long millis = (long) value + offset;
            return format.format(new Date(millis));
        }
    }

    public void initialize() {
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
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1f); // one hour
//        xAxis.setValueFormatter(new CutOffFormatter(getSimpleDateFormat(kind)));
        xAxis.setValueFormatter(new OffsetFormatter(getSimpleDateFormat(kind)));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(true);

        chart.getAxisRight().setEnabled(false);

        chart.setHighlightPerDragEnabled(false);
    }

    public void replaceValueFormatter() {
//        chart.getXAxis().setValueFormatter(new CutOffFormatter(getSimpleDateFormat(kind)));
        chart.getXAxis().setValueFormatter(new OffsetFormatter(getSimpleDateFormat(kind)));
    }

    public void setData(ArrayList<History> histories) {
        ArrayList<Entry> values = new ArrayList<>(histories.size());

        offset = histories.get(0).getTime() * 1000;

        for (History history : histories) {
//            long x = inverseConvert(history.getTime() * 1000);
            long x = history.getTime() * 1000 - offset;
            values.add(new Entry(x, (float) history.getClose()));
        }

        LineDataSet set = new LineDataSet(values, "DataSet 1");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.JOYFUL_COLORS[0]);
        set.setValueTextColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(1.5f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawCircleHole(false);

//        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

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

    public void clear() {
        chart.fitScreen();
        chart.clear();
        chart = null;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
