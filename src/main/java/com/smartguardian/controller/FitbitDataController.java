package com.smartguardian.controller;

import com.smartguardian.security.services.FitbitApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/fitbit")
public class FitbitDataController {

    private final FitbitApiService fitbitApiService;

    public FitbitDataController(FitbitApiService fitbitApiService) {
        this.fitbitApiService = fitbitApiService;
    }

    /**
     * Returns the authenticated user's heart rate data for today.
     */
    @GetMapping("/heart-rate")
    public ResponseEntity<?> getTodayHeartRate() {

        String today = LocalDate.now().toString();

        String heartRateData =
                fitbitApiService.getHeartRateForDate(today);

        return ResponseEntity.ok(heartRateData);
    }

    /**
     * Returns heart rate data for a specific date (YYYY-MM-DD).
     */
    @GetMapping("/heart-rate/{date}")
    public ResponseEntity<?> getHeartRateForDate(
            @PathVariable String date
    ) {
        String heartRateData =
                fitbitApiService.getHeartRateForDate(date);

        return ResponseEntity.ok(heartRateData);
    }

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
