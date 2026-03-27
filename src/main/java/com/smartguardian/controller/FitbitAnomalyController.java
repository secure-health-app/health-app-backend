package com.smartguardian.controller;

import com.smartguardian.security.services.anomaly.AnomalyResult;
import com.smartguardian.security.services.anomaly.FitbitAnomalyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anomaly")
public class FitbitAnomalyController {

    private final FitbitAnomalyService fitbitAnomalyService;

    public FitbitAnomalyController(FitbitAnomalyService fitbitAnomalyService) {
        this.fitbitAnomalyService = fitbitAnomalyService;
    }

    @GetMapping("/check")
    public ResponseEntity<AnomalyResult> checkAnomalies() {
        AnomalyResult result = fitbitAnomalyService.checkTodaysAnomalies();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/backfill")
    public ResponseEntity<String> backfill(@RequestParam(defaultValue = "30") int days) {
        fitbitAnomalyService.backfillLastNDays(days);
        return ResponseEntity.ok("Backfill complete for last " + days + " days");
    }
}