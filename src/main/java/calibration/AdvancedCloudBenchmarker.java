package calibration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AdvancedCloudBenchmarker extends CloudBenchmarker{

    @Override
    public String getName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Map<String, BigDecimal[]> calculateThresholds(Map<String, ArrayList<BigDecimal>> measureBenchmarkData) {
        // Full set of benchmark data
        Map<String, BigDecimal[]> measureThresholds = new HashMap<>();
        measureBenchmarkData.forEach((measureName, measureValues) -> {

            int measureValuesSize = measureBenchmarkData.get(measureName).size();
            measureThresholds.putIfAbsent(measureName, new BigDecimal[measureValuesSize]);
            for (int i = 0; i < measureValuesSize; i++) {
                measureThresholds.get(measureName)[i] = measureBenchmarkData.get(measureName).get(i);
            }
        });

        return measureThresholds;
    }

}
