package calibration;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import pique.evaluation.ProbabilityDensityFunctionUtilityFunction;
import pique.utility.PDFUtils;
import utilities.HelperFunctions;

import java.math.BigDecimal;
import java.util.Arrays;

public class PDFBandwidthCalculator extends ProbabilityDensityFunctionUtilityFunction {

    @Override
    public BigDecimal utilityFunction(BigDecimal inValue, BigDecimal[] thresholds, boolean positive) {
        this.setBandwidth(calculateBandwidth(thresholds));
        this.setSamplingSpace(calculateNumberOfXPoints(thresholds));
        return super.utilityFunction(inValue, thresholds, positive);
    }

    private int calculateNumberOfXPoints(BigDecimal[] thresholds){
        //experiment more, find functions for how many points
        if (thresholds == null || thresholds.length == 0) {
            return 1;
        }
        return thresholds.length * 5;
    }

    private double calculateBandwidth(BigDecimal[] thresholds){
        //bandwidth will calculate to NaN if thresholds are all zero, so we need logic to set bandwidth to 0 if we have all zero thresholds.
        //the pdf function itself it responsible for skipping these values
        double newBandwidth = 0.0;
        if (HelperFunctions.doThresholdsHaveNonZero(thresholds)) {
            double nFactor = Math.pow(thresholds.length, -0.2);
            double iqr = getIQR(thresholds);
            double sigma = calculateSampleStandardDeviation(thresholds);
            if (iqr == 0){
                newBandwidth = 1.06 * sigma * nFactor;
            }else {
                newBandwidth = 0.9 * (Math.min(sigma, (iqr / 1.34))) * nFactor;
            }
        }
        return newBandwidth;
    }

    private double getIQR(BigDecimal[] data){
        double[] doubleData = Arrays.stream(data).mapToDouble(BigDecimal::doubleValue).toArray();
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(doubleData);
        double iqr = descriptiveStatistics.getPercentile(75) - descriptiveStatistics.getPercentile(25);
        return iqr;
    }

    private double calculateSampleStandardDeviation(BigDecimal[] data){
        double[] doubleData = Arrays.stream(data).mapToDouble(BigDecimal::doubleValue).toArray();

        double sum  = 0.0;
        for (double d : doubleData) {
            sum += d;
        }
        double mean = sum / doubleData.length;

        double sumOfSquaredDeviations = 0.0;
        for (double d : doubleData){
            sumOfSquaredDeviations += Math.pow(d - mean, 2);
        }
        double asDouble = Math.sqrt(sumOfSquaredDeviations / doubleData.length);
        return asDouble;
    }
}
