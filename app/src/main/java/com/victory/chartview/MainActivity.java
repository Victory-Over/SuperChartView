package com.victory.chartview;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ScrollChartView scrollChartView;
    private CircleIndicatorView circleIndicatorView;
    private TextView tvTime;
    private TextView tvData;

    private ScrollChartView.LineType lineType = ScrollChartView.LineType.ARC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //加个延时设置数据，防止view未绘制完成的情况下就设置数据，正常业务不会出现这种情况，因为会有网络加载数据的过程
        handler.sendEmptyMessageDelayed(0, 1000);
    }


    private void initView() {
        scrollChartView = findViewById(R.id.scroll_chart_main);
        circleIndicatorView = findViewById(R.id.civ_main);
        tvTime = findViewById(R.id.tv_time);
        tvData = findViewById(R.id.tv_data);

        final Button btnLine = findViewById(R.id.btn_line);
        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scrollChartView.getLineType() == ScrollChartView.LineType.LINE) {
                    scrollChartView.setLineType(ScrollChartView.LineType.ARC);
                    scrollChartView.invalidateView();
                    btnLine.setText("折线");
                } else {
                    scrollChartView.setLineType(ScrollChartView.LineType.LINE);
                    scrollChartView.invalidateView();
                    btnLine.setText("曲线");
                }
            }
        });
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            initData();
        }
    };

    private void initData() {
        final List<String> timeList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            timeList.add(i + ":00");
        }

        final List<Double> dataList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            dataList.add((double) new Random().nextInt(100));
        }
        scrollChartView.setData(timeList, dataList);
        scrollChartView.setOnScaleListener(new ScrollChartView.OnScaleListener() {
            @Override
            public void onScaleChanged(int position) {
                tvTime.setText(timeList.get(position));
                tvData.setText(dataList.get(position) + "");
                ScrollChartView.Point point = scrollChartView.getList().get(position);
                circleIndicatorView.setCircleY(point.y);
            }
        });

        //滚动到目标position
        scrollChartView.smoothScrollTo(dataList.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
