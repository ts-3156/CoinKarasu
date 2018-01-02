package com.coinkarasu.chart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.List;

class StackedFillFormatter implements IFillFormatter {
    private ILineDataSet boundaryDataSet = null;

    public StackedFillFormatter(ILineDataSet boundaryDataSet) {
        this.boundaryDataSet = boundaryDataSet;
    }

    @Override
    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
        return 0;
    }

    public List<Entry> getFillLineBoundary() {
        if (boundaryDataSet != null) {
            return ((LineDataSet) boundaryDataSet).getValues();
        }
        return null;
    }
}
