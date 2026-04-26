package com.smartguardian.controller;

import com.smartguardian.security.services.anomaly.AnomalyResult;
import com.smartguardian.security.services.anomaly.FitbitAnomalyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/* ===================== FITBIT ANOMALY CONTROLLER ===================== */

@RestController
@RequestMapping("/api/anomaly")
public class FitbitAnomalyController {


    /* ===================== SERVICE ===================== */

    private final FitbitAnomalyService fitbitAnomalyService;


    // constructor injection
    public FitbitAnomalyController(FitbitAnomalyService fitbitAnomalyService) {
        this.fitbitAnomalyService = fitbitAnomalyService;
    }


    /* ===================== CHECK ANOMALIES ===================== */

    // check today's Fitbit anomaly status
    @GetMapping("/check")
    public ResponseEntity<AnomalyResult> checkAnomalies() {

        AnomalyResult result =
                fitbitAnomalyService.checkTodaysAnomalies();

        return ResponseEntity.ok(result);
    }

    // Evaluate already stored Fitbit summaries without fetching new API data
    @GetMapping("/check-existing")
    public ResponseEntity<AnomalyResult> checkExisting() {
        return ResponseEntity.ok(fitbitAnomalyService.checkExistingData());
    }

    // Frontend polling endpoint used by dashboard banners and caregiver alerts
    @GetMapping("/status")
    public ResponseEntity<AnomalyResult> getAnomalyStatus() {
        try {
            return ResponseEntity.ok(
                    fitbitAnomalyService.checkExistingData()
            );
        } catch (Exception e) {
            // If today's Fitbit data not available yet, return safe no-alert response
            return ResponseEntity.ok(
                    AnomalyResult.builder()
                            .anomalyDetected(false)
                            .message("No data available")
                            .flags(java.util.List.of())
                            .build()
            );
        }
    }

    /* ===================== BACKFILL DATA ===================== */

    // Populate historical summaries to build rolling anomaly baseline
    @PostMapping("/backfill")
    public ResponseEntity<String> backfill(
            @RequestParam(defaultValue = "30") int days) {

        fitbitAnomalyService.backfillLastNDays(days);

        return ResponseEntity.ok(
                "Backfill complete for last " + days + " days"
        );
    }
}