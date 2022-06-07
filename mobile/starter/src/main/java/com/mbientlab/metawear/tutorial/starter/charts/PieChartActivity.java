package com.mbientlab.metawear.tutorial.starter.charts;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.mbientlab.metawear.tutorial.starter.MyApplication;
import com.mbientlab.metawear.tutorial.starter.R;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;


public class PieChartActivity extends AppCompatActivity {

    TextView count_jj, count_squats, count_running, count_boxing;
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);

        count_jj = findViewById(R.id.count_jj);
        count_squats = findViewById(R.id.count_squats);
        count_running = findViewById(R.id.count_running);
        count_boxing = findViewById(R.id.count_boxing);
        pieChart = findViewById(R.id.piechart);

        setData();

    }

    private void setData(){
        count_jj.setText(((MyApplication) this.getApplication()).getJumpingJacksCounter().toString());
        count_squats.setText(((MyApplication) this.getApplication()).getSquatsCounter().toString());
        count_running.setText(((MyApplication) this.getApplication()).getRunningCounter().toString());
        count_boxing.setText(((MyApplication) this.getApplication()).getBoxingCounter().toString());


        pieChart.addPieSlice(
                new PieModel(
                        "Jumping Jacks",
                        Integer.parseInt(count_jj.getText().toString()),
                        Color.parseColor("#FFA726")));
        pieChart.addPieSlice(
                new PieModel(
                        "Squats",
                        Integer.parseInt(count_squats.getText().toString()),
                        Color.parseColor("#66BB6A")));
        pieChart.addPieSlice(
                new PieModel(
                        "Running",
                        Integer.parseInt(count_running.getText().toString()),
                        Color.parseColor("#29B6F6")));
        pieChart.addPieSlice(
                new PieModel(
                        "Boxing",
                        Integer.parseInt(count_boxing.getText().toString()),
                        Color.parseColor("#EF5350")));

        pieChart.startAnimation();
    }


}