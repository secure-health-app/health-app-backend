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


    @GetMapping("/check-existing")
    public ResponseEntity<AnomalyResult> checkExisting() {
        return ResponseEntity.ok(fitbitAnomalyService.checkExistingData());
    }

    /* ===================== BACKFILL DATA ===================== */

    // generate baseline using previous days
    @PostMapping("/backfill")
    public ResponseEntity<String> backfill(
            @RequestParam(defaultValue = "30") int days) {

        fitbitAnomalyService.backfillLastNDays(days);

        return ResponseEntity.ok(
                "Backfill complete for last " + days + " days"
        );
    }
}