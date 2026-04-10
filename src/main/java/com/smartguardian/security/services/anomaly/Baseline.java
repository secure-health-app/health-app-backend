package com.smartguardian.security.services.anomaly;

import lombok.Builder;
import lombok.Data;


/* ===================== BASELINE ===================== */

@Data
@Builder
public class Baseline {


    /* ===================== RECORD COUNT ===================== */

    private int recordCount;



    /* ===================== HEART RATE ===================== */

    private double heartRateMean;
    private double heartRateStdDev;



    /* ===================== STEPS ===================== */

    private double stepsMean;
    private double stepsStdDev;



    /* ===================== SLEEP ===================== */

    private double sleepMean;
    private double sleepStdDev;

}