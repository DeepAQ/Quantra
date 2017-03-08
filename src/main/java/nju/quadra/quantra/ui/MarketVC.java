package nju.quadra.quantra.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import nju.quadra.quantra.data.StockBaseProtos;
import nju.quadra.quantra.data.StockData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaUkonn on 2017/3/7.
 */
public class MarketVC extends Pane {

    @FXML
    private GridPane gridPane;

    public MarketVC() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("assets/market.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
        MarketMiniListVC miniList = new MarketMiniListVC(true, "今日涨停", StockData.getList());
        gridPane.add(miniList, 0, 0);
//        loadingLists(currentDate);
    }


    private String dateParser(String dateFromDataset) {
        String[] dateList = dateFromDataset.split("/");
        return "20" + dateList[2] + "/" + dateList[0] + "/" + dateList[1];
    }

    private void loadingLists(String date) {
        List<StockBaseProtos.StockBase.StockInfo> stockRisingLimit = new ArrayList<>();
        List<Double> risingLimitRate = new ArrayList<>();
        List<StockBaseProtos.StockBase.StockInfo> stockFallingLimit = new ArrayList<>();
        List<Double> fallingLimitRate = new ArrayList<>();
        List<StockBaseProtos.StockBase.StockInfo> stockRisingOverFivePer = new ArrayList<>();
        List<Double> risingOverFivePerRate = new ArrayList<>();
        List<StockBaseProtos.StockBase.StockInfo> stockFallingOverFivePer = new ArrayList<>();
        List<Double> fallingOverFivePerRate = new ArrayList<>();
        List<StockBaseProtos.StockBase.StockInfo> stockOverLastFivePer = new ArrayList<>();
        List<Double> overLastFivePerRate = new ArrayList<>();
        List<StockBaseProtos.StockBase.StockInfo> stockUnderLastFivePer = new ArrayList<>();
        List<Double> underLastFivePerRate = new ArrayList<>();
        for(int i = 0; i < StockData.getList().size() - 1; i++) {
            StockBaseProtos.StockBase.StockInfo curr = StockData.getList().get(i);
            StockBaseProtos.StockBase.StockInfo last = StockData.getList().get(i+1);

            if(curr.getDate().equals(date) && last.getCode() == curr.getCode()) {
                double rate = (curr.getClose() - last.getClose()) / last.getClose();
                double otherRate = (curr.getOpen() - curr.getClose()) / last.getClose();
                if(rate > 0.1) {
                    stockRisingLimit.add(curr);
                    risingLimitRate.add(rate);
                } else if(rate < -0.1) {
                    stockFallingLimit.add(curr);
                    fallingLimitRate.add(rate);
                } else if(rate > 0.05) {
                    stockRisingOverFivePer.add(curr);
                    risingOverFivePerRate.add(rate);
                } else if(rate < -0.05) {
                    stockFallingOverFivePer.add(curr);
                    fallingOverFivePerRate.add(rate);
                }
                if(otherRate > 0.05) {
                    stockOverLastFivePer.add(curr);
                    overLastFivePerRate.add(otherRate);
                }else if(otherRate < -0.05) {
                    stockUnderLastFivePer.add(curr);
                    underLastFivePerRate.add(otherRate);
                }
            }
        }
    }

}
