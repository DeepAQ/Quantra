package nju.quadra.quantra.data;

import nju.quadra.quantra.data.StockBaseProtos.StockBase;
import nju.quadra.quantra.data.StockBaseProtos.StockBase.StockInfo;

import java.io.FileInputStream;
import java.util.List;

/**
 * Created by adn55 on 2017/3/3.
 */
public class StockData {

    private static final String DATA_FILE = "stock_data.protobuf";
    private static StockBase base;

    public static List<StockInfo> getList() {
        if (base == null) {
            FileInputStream is = null;
            try {
                is = new FileInputStream(DATA_FILE);
                base = StockBaseProtos.StockBase.parseFrom(is);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return base.getInfoList();
    }

}