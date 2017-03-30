package nju.quadra.quantra.strategy;

/**
 * Created by MECHREVO on 2017/3/30.
 */
public class StubStrategy extends AbstractStrategy{

    public StubStrategy(String name, String type, int freq) {
        super(name, type, freq);
    }

    @Override
    public String getCode() {
        return "";
    }

    @Override
    public String getDescription() {
        return "假策略";
    }
}
