package nju.quadra.quantra.ui.chart;

import javafx.collections.FXCollections;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import nju.quadra.quantra.data.StockBaseProtos;
import nju.quadra.quantra.utils.StockStatisticUtil;

import java.util.List;

/**
 * Created by RaUkonn on 2017/3/10.
 */
public class RisingAndFallingChart extends LineChart<String, Number> {

    public RisingAndFallingChart(Axis<String> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
    }

    public static RisingAndFallingChart createFrom(List<StockBaseProtos.StockBase.StockInfo> infoList) {
        // Create axis
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);
        // Create series and add data
        infoList = infoList.subList(0, infoList.size());
        List<Double> risingList = StockStatisticUtil.RISING_RATE(infoList);
        Series<String, Number> series = new Series<>();
        for (int i = 0; i < infoList.size() - 1; i++) {
            series.getData().add(new Data<>(infoList.get(i).getDate(), risingList.get(i), infoList.get(0)));
        }
        // Create chart
        RisingAndFallingChart chart = new RisingAndFallingChart(xAxis, yAxis);
        chart.setData(FXCollections.observableArrayList(series));
        chart.setTitle(infoList.get(0).getName() + "近" + infoList.size() + "天涨幅趋势");
        return chart;
    }
}
