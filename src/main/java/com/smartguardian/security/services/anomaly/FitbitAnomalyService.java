package com.smartguardian.security.services.anomaly;

import com.smartguardian.model.FitbitDailySummary;
import com.smartguardian.model.User;
import com.smartguardian.repository.FitbitDailySummaryRepository;
import com.smartguardian.repository.UserRepository;
import com.smartguardian.security.services.FitbitApiService;
import com.smartguardian.security.services.UserDetailsImpl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


/* ===================== FITBIT ANOMALY SERVICE ===================== */

@Service
public class FitbitAnomalyService {

    private final FitbitApiService fitbitApiService;
    private final FitbitDailySummaryRepository summaryRepository;
    private final UserRepository userRepository;
    private final BaselineCalculator baselineCalculator;
    private final AnomalyDetector anomalyDetector;

    public FitbitAnomalyService(
            FitbitApiService fitbitApiService,
            FitbitDailySummaryRepository summaryRepository,
            UserRepository userRepository,
            BaselineCalculator baselineCalculator,
            AnomalyDetector anomalyDetector
    ) {
        this.fitbitApiService = fitbitApiService;
        this.summaryRepository = summaryRepository;
        this.userRepository = userRepository;
        this.baselineCalculator = baselineCalculator;
        this.anomalyDetector = anomalyDetector;
    }


    /* ===================== CHECK TODAY ===================== */

    public AnomalyResult checkTodaysAnomalies() {

        User user = getAuthenticatedUser();

        String today = LocalDate.now().toString();

        // fetch today's data from Fitbit and save to DB
        FitbitDailySummary todaySummary =
                fitbitApiService.fetchAndSaveInternal(today);

        // calculate baseline from previous data
        Baseline baseline =
                baselineCalculator.calculate(user);

        // run anomaly detection
        return anomalyDetector.detect(todaySummary, baseline);
    }


    /* ===================== CHECK EXISTING ===================== */

    public AnomalyResult checkExistingData() {
        User user = getAuthenticatedUser();

        FitbitDailySummary todaySummary = summaryRepository
                .findByUserAndDate(user, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No data for today"));

        Baseline baseline = baselineCalculator.calculate(user);
        return anomalyDetector.detect(todaySummary, baseline);
    }


    /* ===================== AUTH USER ===================== */

    private User getAuthenticatedUser() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl userDetails =
                (UserDetailsImpl) auth.getPrincipal();

        return userRepository
                .findById(userDetails.getId())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }


    /* ===================== BACKFILL ===================== */

    public void backfillLastNDays(int days) {

        for (int i = days; i >= 1; i--) {
            String date =
                    LocalDate.now().minusDays(i).toString();

            try {
                fitbitApiService.fetchAndSaveInternal(date);
                System.out.println("[Backfill] Saved: " + date);
            } catch (Exception e) {
                System.out.println("[Backfill] Skipped "
                        + date
                        + ": "
                        + e.getMessage()
                );
            }
        }
    }
}