package nju.quadra.quantra.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import nju.quadra.quantra.data.StrategyData;
import nju.quadra.quantra.strategy.AbstractStrategy;
import nju.quadra.quantra.utils.FXUtil;

import java.io.IOException;

/**
 * Created by MECHREVO on 2017/3/30.
 */
public class StrategyListVC extends Pane {
    @FXML
    private VBox vboxStrategy;

    public StrategyListVC() throws IOException {
        FXUtil.loadFXML(this, getClass().getResource("assets/strategyList.fxml"));
        updateStrategy();
    }

    public void updateStrategy() {
        for (AbstractStrategy strategy : StrategyData.getStrategyList()) {
            try {
                vboxStrategy.getChildren().add(new StrategyItemVC(strategy));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onAddAction() throws IOException {
        new Thread(() -> {
            try {
                UIContainer.showLoading();
                UIContainer.loadContent(new StrategyEditVC(null));
                UIContainer.hideLoading();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
