package nju.quadra.quantra.ui.chart;

import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import nju.quadra.quantra.data.StockInfoPtr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adn55 on 2017/3/16.
 */
public class QuantraLineChart extends LineChart<String, Number> {

    private List<StockInfoPtr> ptrList;
    private List<List<Number>> dataList = new ArrayList<>();
    private Region plotBackground = (Region) lookup(".chart-plot-background");
    private Region plotArea = new Region();
    private Label toolTip = new Label();

    private QuantraLineChart(Axis<String> xAxis, Axis<Number> yAxis, List<StockInfoPtr> ptrList) {
        super(xAxis, yAxis);
        this.ptrList = ptrList;
        this.setLegendVisible(false);
        this.setCreateSymbols(false);
        this.getStylesheets().setAll(getClass().getResource("QuantraKChart.css").toString());
        getPlotChildren().addAll(plotArea, toolTip);
        // Create tooltip
        toolTip.getStyleClass().add("tooltip");
        toolTip.setMouseTransparent(true);
        toolTip.setVisible(false);
        // Bind mouse events
        plotArea.setOnMouseExited(event -> toolTip.setVisible(false));
        plotArea.setOnMouseMoved(event -> {
            double xPos = event.getX();
            double yPos = event.getY();
            String xValue = xAxis.getValueForDisplay(xPos);
            if (xValue != null) {
                int size = ptrList.size();
                int i = -1;
                for (StockInfoPtr ptr : ptrList) {
                    i++;
                    if (ptr.get().getDate().equals(xValue)) {
                        String tip = "";
                        int lineCount = 1, j = -1;
                        for (Series<String, Number> series : getData()) {
                            j++;
                            double yValue = dataList.get(j).get(i).doubleValue();
                            if (!Double.isNaN(yValue)) {
                                tip += "\n" + series.getName() + ": " + yValue;
                                lineCount++;
                            }
                        }
                        toolTip.setText(xValue + tip);
                        toolTip.resize(120, lineCount * 20);
                        if (xPos + 10 + toolTip.getWidth() > plotBackground.getWidth()) {
                            xPos -= toolTip.getWidth() + 20;
                        }
                        if (yPos + 10 + toolTip.getHeight() > plotBackground.getHeight()) {
                            yPos -= toolTip.getHeight() + 20;
                        }
                        toolTip.relocate(xPos + 10, yPos + 10);
                        toolTip.setVisible(true);
                        break;
                    }
                }
            }
        });
    }

    public static QuantraLineChart createFrom(List<StockInfoPtr> ptrList) {
        // Create axis
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);
        // Create chart
        QuantraLineChart chart = new QuantraLineChart(xAxis, yAxis, ptrList);
        return chart;
    }

    public void addPath(String name, Paint color, List<Number> numbers) {
        Series<String, Number> series = new Series<>();
        int size = Math.min(ptrList.size(), numbers.size());
        for (int i = 0; i < size; i++) {
            if (numbers.get(i) != null && !Double.isNaN(numbers.get(i).doubleValue())) {
                series.getData().add(new Data<>(ptrList.get(i).get().getDate(), numbers.get(i)));
            }
        }
        series.setName(name);
        getData().add(series);
        if (series.getNode() != null) {
            Path path = (Path) series.getNode();
            path.setStroke(color);
            path.setStrokeWidth(2);
        }
        dataList.add(numbers);
    }

    @Override
    protected void seriesAdded(Series<String, Number> series, int seriesIndex) {
        super.seriesAdded(series, seriesIndex);
        plotArea.toFront();
        toolTip.toFront();
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        plotArea.resize(getWidth(), getHeight());
    }

}