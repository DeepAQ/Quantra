package nju.quadra.quantra.pool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by RaUkonn on 2017/4/3.
 */
public class CustomPool extends AbstractPool {
    private static String STOCK_POOL_NAME;
    public CustomPool(String name, List<Integer> list) {
        this.name = name;
        this.stockPool = new HashSet<>(list);
        STOCK_POOL_NAME = "data/pools/" + Base64.getEncoder().encodeToString(this.name.getBytes(StandardCharsets.UTF_8)).replace('/', '~') + ".json";
        saveToFile();
    }

    private void saveToFile() {
        try {
            File file = new File(STOCK_POOL_NAME);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            JSONWriter writer = new JSONWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            writer.startArray();
            for (Integer i : stockPool) {
                writer.writeObject(i);
            }
            writer.endArray();
            writer.flush();
            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        loadFromFile(STOCK_POOL_NAME);
    }

    private void loadFromFile(String filePath) {
        try {
            stockPool = loadCore(filePath);
        } catch (IOException e) {
            saveToFile();
        }
    }

    private static Set<Integer> loadCore(String name) throws IOException {
        InputStream is = new FileInputStream(name);
        Set<Integer> result = new HashSet<>();
        byte[] poolsBytes = new byte[is.available()];
        is.read(poolsBytes);
        is.close();
        JSONArray stocks = JSON.parseArray(new String(poolsBytes, "utf-8"));
        result.clear();

        for (int i = stocks.size() - 1; i >= 0; i--) {
            result.add(stocks.getInteger(i));
        }
        return result;
    }

    public void removeStock(int stock) {
        loadFromFile();
        Iterator<Integer> itr = stockPool.iterator();
        while (itr.hasNext()) {
            if (itr.next() == stock) {
                itr.remove();
                break;
            }
        }
        saveToFile();
    }

    public void removeStockList(List<Integer> stockList) {
        loadFromFile();
        for (int i = stockList.size() - 1; i >= 0; i--) {
            if (stockPool.contains(stockList.get(i))) {
                stockPool.remove(stockList.get(i));
                stockList.remove(i);
            }
        }
        saveToFile();
    }

    public void addStock(int stock) {
        loadFromFile();
        stockPool.add(stock);
        saveToFile();
    }

    public void addStockList(List<Integer> stockList) {
        loadFromFile();
        stockPool.addAll(stockList);
        saveToFile();
    }

    public static CustomPool createPoolFromFile(String name) {
        File poolDir = new File("data/pools");
        String nameEncoded = Base64.getEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8)).replace('/', '~');
        if (!poolDir.exists()) {
            poolDir.getParentFile().mkdirs();
            return null;
        } else if (poolDir.listFiles().length == 0) {
            return null;
        }
        for (File f : poolDir.listFiles()) {
            if (f.getName().equals(nameEncoded)) {
                List<Integer> stockPool = new ArrayList<>();
                try {
                    stockPool = new ArrayList<>(loadCore(f.getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new CustomPool(name, stockPool);
            }
        }
        return null;
    }

    @Override
    public List<Integer> getStockPool() {
        return new ArrayList<>(stockPool);
    }
}
