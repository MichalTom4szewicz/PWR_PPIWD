package com.mbientlab.metawear.tutorial.starter.charts;

import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.mbientlab.metawear.tutorial.starter.R;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;

public class BarChartActivity extends AppCompatActivity {

    private XYPlot plot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        // wyniki
        final Number[] domainLabels = {1, 2, 3, 6, 7, 8, 9, 10, 13, 14};
        Number[] seriesSquatsNumbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};
        Number[] seriesJumpsNumbers = {5, 2, 10, 5, 20, 10, 40, 20, 80, 40};
        Number[] seriesRunningNumbers = {4, 6, 4, 2, 26, 7, 8, 9, 1, 15};

        //nazwy cwiczen
        XYSeries seriesSquats = new SimpleXYSeries(Arrays.asList(seriesSquatsNumbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Squats");
        XYSeries seriesJumps = new SimpleXYSeries(Arrays.asList(seriesJumpsNumbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Jumps");
        XYSeries seriesRunning = new SimpleXYSeries(Arrays.asList(seriesRunningNumbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Running");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter seriesSquatsFormat =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels_squats);

        LineAndPointFormatter seriesJumpsFormat  =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_jumps);

        LineAndPointFormatter seriesRunningFormat  =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels_running);

        // add an "dash" effect to the series2 line:
        seriesJumpsFormat.getLinePaint().setPathEffect(new DashPathEffect(new float[]{

                // always use DP when specifying pixel sizes, to keep things consistent across devices:
                PixelUtils.dpToPix(20),
                PixelUtils.dpToPix(15)}, 0));

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        seriesSquatsFormat.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        seriesJumpsFormat.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        seriesRunningFormat.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // dodawanie wynikow do wykresu:
        plot.addSeries(seriesSquats, seriesSquatsFormat);
        plot.addSeries(seriesJumps, seriesJumpsFormat);
        plot.addSeries(seriesRunning, seriesRunningFormat);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return toAppendTo.append(domainLabels[i]);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });
    }
}