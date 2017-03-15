package nju.quadra.quantra.ui;

import com.jfoenix.controls.JFXDatePicker;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import nju.quadra.quantra.data.StockData;
import nju.quadra.quantra.data.StockInfoPtr;
import nju.quadra.quantra.utils.DateUtil;
import nju.quadra.quantra.utils.FXUtil;
import nju.quadra.quantra.utils.StockStatisticUtil;

import java.io.IOException;

/**
 * Created by Lenovo on 2017/3/14.
 */
public class StockListVC extends BorderPane {

    @FXML
    private TableView<StockInfoPtr> table;
    @FXML
    private JFXDatePicker datePicker;

    public StockListVC() throws IOException {
        FXUtil.loadFXML(this, getClass().getResource("assets/stockList.fxml"));

        addColumn("代码", param -> new ReadOnlyObjectWrapper<>(String.format("%06d", param.getValue().getToday().getCode())));
        addColumn("名称", param -> new ReadOnlyObjectWrapper<>(param.getValue().getToday().getName()));
        addColumn("涨幅", param -> new ReadOnlyObjectWrapper<>(Math.floor(StockStatisticUtil.RATE(param.getValue()) * 10000) / 100.0 + "%"));
        addColumn("今收", param -> new ReadOnlyObjectWrapper<>(param.getValue().getToday().getClose()));
        addColumn("交易量", param -> new ReadOnlyObjectWrapper<>(param.getValue().getToday().getVolume()));
        addColumn("今开", param -> new ReadOnlyObjectWrapper<>(param.getValue().getToday().getOpen()));
        addColumn("最高", param -> new ReadOnlyObjectWrapper<>(param.getValue().getToday().getHigh()));
        addColumn("最低", param -> new ReadOnlyObjectWrapper<>(param.getValue().getToday().getLow()));
        addColumn("昨收", param -> new ReadOnlyObjectWrapper<>(param.getValue().getYesterday().getClose()));

        datePicker.valueProperty().addListener(observable -> updateInfo());
        datePicker.setValue(DateUtil.parseLocalDate(StockData.latest));
    }

    private void addColumn(String title, Callback<TableColumn.CellDataFeatures<StockInfoPtr, Object>, ObservableValue<Object>> factory) {
        TableColumn<StockInfoPtr, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(factory);
        table.getColumns().add(column);
    }

    private void updateInfo() {
        String date = DateUtil.localDateToString(datePicker.getValue());
        table.getItems().setAll(StockData.getByDate(date));
    }

}