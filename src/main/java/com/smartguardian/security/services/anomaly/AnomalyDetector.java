package com.smartguardian.security.services.anomaly;

import com.smartguardian.model.FitbitDailySummary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/* ===================== ANOMALY DETECTOR ===================== */

// Compares today's Fitbit metrics against rolling historical baseline
@Component
public class AnomalyDetector {

    // How many standard deviations from the mean counts as anomalous
    private static final double THRESHOLD = 2.0;

    // Minimum days of data needed before anomaly detection is meaningful
    private static final int MIN_RECORDS = 7;

    // Returns anomaly flags for today's summary using z-score comparisons
    public AnomalyResult detect(
            FitbitDailySummary today,
            Baseline baseline
    ) {

        List<String> flags = new ArrayList<>();

        // baseline check
        if (baseline.getRecordCount() < MIN_RECORDS) {
            return AnomalyResult.builder()
                    .anomalyDetected(false)
                    .message("Not enough data to establish baseline (need at least "
                            + MIN_RECORDS + " days)")
                    .flags(flags)
                    .build();
        }


        /* ===================== HEART RATE ===================== */

        // spike above baseline is the concern
        if (today.getRestingHeartRate() != null && baseline.getHeartRateStdDev() > 0) {
            double hrZ = (today.getRestingHeartRate()
                    - baseline.getHeartRateMean())
                    / baseline.getHeartRateStdDev();
            if (hrZ > THRESHOLD) {

                flags.add
                        (String.format(
                                "Resting heart rate (%d BPM) is significantly above normal (mean: %.1f BPM)",
                                today.getRestingHeartRate(),
                                baseline.getHeartRateMean()
                                )
                        );
            }
        }


        /* ===================== STEPS ===================== */

        // drop below baseline is the concern
        if (today.getSteps() != null && baseline.getStepsStdDev() > 0) {

            double stepsZ =
                    (today.getSteps()
                            - baseline.getStepsMean())
                            / baseline.getStepsStdDev();

            if (stepsZ < -THRESHOLD) {
                flags.add(
                        String.format(
                                "Daily steps (%d) are significantly below normal (mean: %.0f steps)",
                                today.getSteps(),
                                baseline.getStepsMean()
                        )
                );
            }
        }


        /* ===================== SLEEP ===================== */

        // drop below baseline is the concern
        if (today.getSleepMinutes() != null && baseline.getSleepStdDev() > 0) {

            double sleepZ = (today.getSleepMinutes()
                    - baseline.getSleepMean())
                    / baseline.getSleepStdDev();

            if (sleepZ < -THRESHOLD) {
                flags.add(
                        String.format(
                                "Sleep (%d mins) is significantly below normal (mean: %.0f mins)",
                                today.getSleepMinutes(),
                                baseline.getSleepMean()
                        )
                );
            }
        }


        /* ===================== RESULT ===================== */

        return AnomalyResult.builder()
                .anomalyDetected(!flags.isEmpty())
                .message(flags.isEmpty()
                        ? "All metrics within normal range"
                        : flags.size() + " anomaly/anomalies detected")
                .flags(flags)
                .build();
    }
}