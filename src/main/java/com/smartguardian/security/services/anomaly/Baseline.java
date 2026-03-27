package com.smartguardian.security.services.anomaly;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Baseline {
    private int recordCount;
    private double heartRateMean;
    private double heartRateStdDev;
    private double stepsMean;
    private double stepsStdDev;
    private double sleepMean;
    private double sleepStdDev;
}