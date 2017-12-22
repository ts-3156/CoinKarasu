package com.example.coinkarasu.chart;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.example.coinkarasu.utils.AssetsHelper;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class CoinPieChart {

    private PieChart chart;

    public CoinPieChart(PieChart chart) {
        this.chart = chart;
    }

    public void invalidate() {
        chart.invalidate();
    }

    public void initialize() {
        chart.setUsePercentValues(true);

        chart.getDescription().setEnabled(false);
//        chart.setExtraOffsets(5, 10, 5, 5);

        chart.setDragDecelerationFrictionCoef(0.95f);

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(true);

        chart.setRotationAngle(0);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(false);

//        chart.setUnit(" €");
//        chart.setDrawUnitsInChart(true);

//        setData(4, 100);

        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        chart.getLegend().setEnabled(false);

        // entry label styling
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(12f);

        chart.offsetLeftAndRight(0);
        chart.setExtraOffsets(0, 0, 0, 0);
        chart.getCircleBox().offset(0, 0);

    }

    public void setData(ArrayList<Double> values, ArrayList<String> labels) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            entries.add(new PieEntry(values.get(i).floatValue(), labels.get(i)));
        }


        PieDataSet dataSet = new PieDataSet(entries, "Election Results");

        dataSet.setDrawIcons(false);

//        dataSet.setSliceSpace(3f);
//        dataSet.setIconsOffset(new MPPointF(0, 40));
//        dataSet.setSelectionShift(5f);
        dataSet.setSelectionShift(0);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

//        for (int c : ColorTemplate.VORDIPLOM_COLORS)
//            colors.add(c);
//
        for (int i = 0; i < ColorTemplate.JOYFUL_COLORS.length; i++) {
            if (i != 2) {
                colors.add(ColorTemplate.JOYFUL_COLORS[i]);
            }
        }

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
//
//        for (int c : ColorTemplate.PASTEL_COLORS)
//            colors.add(c);

//        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);


        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();
    }

    public void setCurrencyCenterText(Activity activity, String symbol) {
        if (activity == null) {
            return;
        }
        Typeface typeFace = AssetsHelper.getInstance(activity).light;
        chart.setCenterTextTypeface(typeFace);
        chart.setCenterText(currencySpannableText(symbol));
    }

    private SpannableString currencySpannableText(String symbol) {
        String text = "Money flow into " + symbol + "\nvolume by Currency";
        int len = ("Money flow into " + symbol).length();

        SpannableString s = new SpannableString(text);
        s.setSpan(new RelativeSizeSpan(1.4f), 0, len, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), len, s.length() - 9, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), len, s.length() - 9, 0);
        s.setSpan(new RelativeSizeSpan(.8f), len, s.length() - 9, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 8, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 8, s.length(), 0);

        return s;
    }

    public void setExchangeCenterText(Activity activity, String fromSymbol, String toSymbol) {
        if (activity == null) {
            return;
        }
        Typeface typeFace = AssetsHelper.getInstance(activity).light;
        chart.setCenterTextTypeface(typeFace);
        chart.setCenterText(exchangeSpannableText(fromSymbol, toSymbol));
    }

    private SpannableString exchangeSpannableText(String fromSymbol, String toSymbol) {
        String text = "Trading " + toSymbol + " for " + fromSymbol + "\nvolume by Exchange";
        int len = ("Trading " + toSymbol + " for " + fromSymbol).length();

        SpannableString s = new SpannableString(text);
        s.setSpan(new RelativeSizeSpan(1.4f), 0, len, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), len, s.length() - 9, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), len, s.length() - 9, 0);
        s.setSpan(new RelativeSizeSpan(.8f), len, s.length() - 9, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 8, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 8, s.length(), 0);

        return s;
    }

    public void animateY() {
        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
    }

    public void clear() {
        chart.clear();
        chart = null;
    }
}
