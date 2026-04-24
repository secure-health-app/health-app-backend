package com.smartguardian.security.services.anomaly;

import com.smartguardian.model.FitbitDailySummary;
import com.smartguardian.model.User;
import com.smartguardian.repository.FitbitDailySummaryRepository;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;


/* ===================== BASELINE CALCULATOR ===================== */

@Component
public class BaselineCalculator {

    private final FitbitDailySummaryRepository summaryRepository;

    public BaselineCalculator(
            FitbitDailySummaryRepository summaryRepository) {

        this.summaryRepository = summaryRepository;
    }


    /* ===================== CALCULATE BASELINE ===================== */

    public Baseline calculate(User user) {

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate thirtyDaysAgo = today.minusDays(30);

        List<FitbitDailySummary> records = summaryRepository
                .findByUserAndDateBetweenOrderByDateAsc(
                        user,
                        thirtyDaysAgo,
                        yesterday  // exclude today
                );

        return Baseline.builder()
                .recordCount(records.size())

                .heartRateMean(
                        mean(records.stream()
                                .filter(r -> r.getRestingHeartRate() != null)
                                .mapToInt(FitbitDailySummary::getRestingHeartRate))
                )
                .heartRateStdDev(stdDev(
                        records.stream()
                                .filter(r -> r.getRestingHeartRate() != null)
                                .mapToInt(FitbitDailySummary::getRestingHeartRate))
                )
                .stepsMean(mean(
                        records.stream()
                                .filter(r -> r.getSteps() != null)
                                .mapToInt(FitbitDailySummary::getSteps))
                )
                .stepsStdDev(stdDev(
                        records.stream()
                                .filter(r -> r.getSteps() != null)
                                .mapToInt(FitbitDailySummary::getSteps))
                )
                .sleepMean(mean(
                        records.stream()
                                .filter(r -> r.getSleepMinutes() != null)
                                .mapToInt(FitbitDailySummary::getSleepMinutes))
                )
                .sleepStdDev(stdDev(
                        records.stream()
                                .filter(r -> r.getSleepMinutes() != null)
                                .mapToInt(FitbitDailySummary::getSleepMinutes))
                )
                .build();
    }

    /* ===================== MATHS ===================== */

    private double mean(java.util.stream.IntStream stream) {
        OptionalDouble result = stream.average();
        return result.orElse(0.0);
    }

    private double stdDev(java.util.stream.IntStream stream) {
        int[] values = stream.toArray();

        if (values.length < 2) return 0.0;

        double mean = 0.0;
        for (int v : values)
            mean += v;
        mean /= values.length;

        double variance = 0.0;
        for (int v : values)
            variance += Math.pow(v - mean, 2);
        variance /= values.length;

        return Math.sqrt(variance);
    }
}