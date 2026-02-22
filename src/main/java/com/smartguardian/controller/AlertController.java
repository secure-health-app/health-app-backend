package com.smartguardian.controller;

import com.smartguardian.model.FallAlert;
import com.smartguardian.repository.FallAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private FallAlertRepository fallAlertRepository;

    // Loaded from environment variable DEVICE_API_KEY
    @Value("${smartguardian.device.apiKey}")
    private String deviceApiKey;

    /**
     * POST /api/alerts/fall
     * Called by the Raspberry Pi when a fall is detected.
     * Secured by a pre-shared API key in the X-Device-Key header.
     *
     * Expected JSON body:
     * {
     *   "deviceId": "raspberry-pi-sense-hat",
     *   "peakAcceleration": 3.2,
     *   "detectionPhase": "IMPACT"
     * }
     */
    @PostMapping("/fall")
    public ResponseEntity<?> receiveFallAlert(
            @RequestHeader("X-Device-Key") String apiKey,
            @RequestBody Map<String, Object> payload) {

        // Validate the API key from the Pi
        if (!deviceApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid device API key"));
        }

        // Extract fields from the request body
        String deviceId = (String) payload.getOrDefault("deviceId", "unknown");
        Double peakAcceleration = payload.get("peakAcceleration") != null
                ? ((Number) payload.get("peakAcceleration")).doubleValue()
                : null;
        String detectionPhase = (String) payload.getOrDefault("detectionPhase", "UNKNOWN");

        // Persist the alert
        FallAlert alert = new FallAlert(Instant.now(), peakAcceleration, detectionPhase, deviceId);
        fallAlertRepository.save(alert);

        System.out.println("[ALERT] Fall detected from device: " + deviceId
                + " | Phase: " + detectionPhase
                + " | Peak: " + peakAcceleration + "g");

        return ResponseEntity.ok(Map.of(
                "message", "Fall alert received and saved",
                "alertId", alert.getId()
        ));
    }

    /**
     * GET /api/alerts/fall
     * Returns all unacknowledged fall alerts for the dashboard.
     * Requires JWT authentication (standard user login).
     */
    @GetMapping("/fall")
    public ResponseEntity<List<FallAlert>> getUnacknowledgedAlerts() {
        return ResponseEntity.ok(fallAlertRepository.findByAcknowledgedFalse());
    }

    /**
     * PUT /api/alerts/fall/{id}/acknowledge
     * Marks an alert as acknowledged from the dashboard.
     */
    @PutMapping("/fall/{id}/acknowledge")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable Long id) {
        return fallAlertRepository.findById(id).map(alert -> {
            alert.setAcknowledged(true);
            fallAlertRepository.save(alert);
            return ResponseEntity.ok(Map.of("message", "Alert acknowledged"));
        }).orElse(ResponseEntity.notFound().build());
    }
}