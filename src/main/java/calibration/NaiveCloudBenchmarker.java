package calibration;

import pique.calibration.IBenchmarker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NaiveCloudBenchmarker extends CloudBenchmarker{

    @Override
    public String getName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Map<String, BigDecimal[]> calculateThresholds(Map<String, ArrayList<BigDecimal>> measureBenchmarkData) {
        // Identify the lowest and highest of each measure value
        Map<String, BigDecimal[]> measureThresholds = new HashMap<>();
        measureBenchmarkData.forEach((measureName, measureValues) -> {
            BigDecimal[] temp = new BigDecimal[2];
            temp[0] = Collections.min(measureValues);
            temp[1] = Collections.max(measureValues);
            measureThresholds.putIfAbsent(measureName, temp);
        });

        return measureThresholds;
    }
}
