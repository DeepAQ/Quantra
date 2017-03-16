package nju.quadra.quantra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import nju.quadra.quantra.data.StockBaseProtos;
import nju.quadra.quantra.data.StockData;
import nju.quadra.quantra.data.StockInfoPtr;
import nju.quadra.quantra.ui.chart.*;
import nju.quadra.quantra.utils.DateUtil;
import nju.quadra.quantra.utils.FXUtil;
import nju.quadra.quantra.utils.StockStatisticUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by RaUkonn on 2017/3/10.
 */
public class StockCompareItemVC extends VBox {
    @FXML
    private Label labelName, labelMax, labelMin, labelLogReturnVar, labelCode, labelRising;
    @FXML
    private VBox paneCharts;
    @FXML
    private static int code;
    private String dateStart, dateEnd;

    public StockCompareItemVC(int code, String dateStart, String dateEnd) throws IOException {
        FXUtil.loadFXML(this, getClass().getResource("assets/stockCompareItem.fxml"));

        List<StockInfoPtr> list = StockData.getPtrByCode(code);
        //初始日期直接掩盖掉实际的第一天好了，便于计算
        int startIndex = list.size() - 2, endIndex = 0;
        String listStartDate = list.get(startIndex).get().getDate();
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        if(DateUtil.compare(list.get(0).get().getDate(), dateEnd) < 0)
            this.dateEnd = list.get(0).get().getDate();
        if(DateUtil.compare(listStartDate, dateStart) > 0)
            this.dateStart = dateStart;

        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).get().getDate().equals(this.dateEnd)) {
                endIndex = i;
                break;
            }
        }
        for(int i = endIndex; i < list.size(); i++) {
            if(list.get(i).get().getDate().equals(this.dateStart)) {
                startIndex = i + 1; //找到所选那天的数据，还要往前再取一天
                break;
            }
        }

        list = list.subList(endIndex, startIndex + 1);
        labelName.setText(list.get(0).get().getName());
        labelCode.setText(String.format("%06d", list.get(0).get().getCode()));
        QuantraKChart kChart = QuantraKChart.createFrom(list);
        kChart.setTitle(list.get(0).get().getName() + " 近" + list.size() + "个交易日 K线图");
        QuantraBarChart volumeChart = QuantraBarChart.createFrom(list, "交易量");
        volumeChart.setTitle(list.get(0).get().getName() + " 近" + list.size() + "个交易日 交易量趋势");
        QuantraLineChart lrvChart = QuantraLineChart.createFrom(list);
        lrvChart.addPath("对数收益率", Color.LIGHTPINK, StockStatisticUtil.DAILY_LOG_RETURN(list).stream().map(x -> x.doubleValue()).collect(Collectors.toList()));
        lrvChart.setTitle(list.get(0).get().getName() + " 近" + list.size() + "个交易日 对数收益率趋势");
        paneCharts.getChildren().addAll(kChart, volumeChart, lrvChart);
        //paneCharts.getChildren().add(RisingAndFallingChart.createFrom(list));
        //paneCharts.getChildren().add(VolumeChart.createFrom(list));
        //paneCharts.getChildren().add(LowHighChart.createFrom(list, true));
        //paneCharts.getChildren().add(LowHighChart.createFrom(list, false));
        this.code = code;

        Format f = new DecimalFormat("#.##");

        //因为实际取了n+1天，所以计算最值需要把第n+1天剔除
        labelMax.setText(f.format(StockStatisticUtil.HHV(list.subList(0, list.size() - 1))));
        labelMin.setText(f.format(StockStatisticUtil.LLV(list.subList(0, list.size() - 1))));
        labelRising.setText(Float.toString((list.get(0).get().getAdjClose() - list.get(list.size() - 1).get().getAdjClose()) / list.get(list.size() - 1).get().getAdjClose()));
        labelLogReturnVar.setText(f.format(StockStatisticUtil.DAILY_LOG_RETURN_VAR(list) * 100) + "%");
    }

    @FXML
    private void onPlusClickedAction(MouseEvent t) throws IOException{
        CommonEventController.onPlusClickedEvent(t, code);
        StockCompareVC.load();
    }


}
