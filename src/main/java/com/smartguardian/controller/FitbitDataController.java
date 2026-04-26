package com.smartguardian.controller;

import com.smartguardian.security.services.FitbitApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


/* ===================== FITBIT DATA CONTROLLER ===================== */

@RestController
@RequestMapping("/api/fitbit")
public class FitbitDataController {

    // Service
    private final FitbitApiService fitbitApiService;

    public FitbitDataController(FitbitApiService fitbitApiService) {

        this.fitbitApiService = fitbitApiService;
    }


    /* ===================== DASHBOARD ===================== */

    // Fetch latest Fitbit metrics and return combined dashboard summary
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {

        String today =
                LocalDate.now().toString();

        return ResponseEntity.ok(
                fitbitApiService.fetchAndSaveDailySummary(today)
        );
    }


    /* ===================== HEART RATE ===================== */

    // get today's heart rate
    @GetMapping("/heart-rate")
    public ResponseEntity<?> getTodayHeartRate() {

        String today =
                LocalDate.now().toString();

        String heartRateData =
                fitbitApiService.getHeartRateForDate(today);

        return ResponseEntity.ok(heartRateData);
    }

    // get heart rate for specific date
    @GetMapping("/heart-rate/{date}")
    public ResponseEntity<?> getHeartRateForDate(
            @PathVariable String date
    ) {
        String heartRateData =
                fitbitApiService.getHeartRateForDate(date);

        return ResponseEntity.ok(heartRateData);
    }


    /* ===================== STEPS ===================== */

    // get steps (today by default)
    @GetMapping("/steps")
    public ResponseEntity<?> getSteps(
            @RequestParam(defaultValue = "today") String date) {

        if ("today".equals(date)) {
            date = LocalDate.now().toString();
        }

        return ResponseEntity.ok(
                fitbitApiService.getStepsForDate(date)
        );
    }


    /* ===================== SLEEP ===================== */

    // get sleep data (today by default)
    @GetMapping("/sleep")
    public ResponseEntity<?> getSleep(
            @RequestParam(defaultValue = "today") String date) {

        if ("today".equals(date)) {
            date = LocalDate.now().toString();
        }

        return ResponseEntity.ok(
                fitbitApiService.getSleepForDate(date)
        );
    }


}
