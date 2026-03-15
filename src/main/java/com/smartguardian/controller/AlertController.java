package com.smartguardian.controller;

import com.smartguardian.model.FallAlert;
import com.smartguardian.repository.FallAlertRepository;
import com.smartguardian.repository.UserRepository;
import com.smartguardian.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.smartguardian.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private FallAlertRepository fallAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${smartguardian.device.apiKey}")
    private String deviceApiKey;

    // PI ENDPOINT

    // Raspberry Pi posts here when a fall is detected
    @PostMapping("/fall")
    public ResponseEntity<?> receiveFallAlert(
            @RequestHeader("X-Device-Key") String apiKey,
            @RequestBody Map<String, Object> payload) {

        if (!deviceApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid device API key"));
        }

        String deviceId = (String) payload.getOrDefault("deviceId", "unknown");
        Double peakAcceleration = payload.get("peakAcceleration") != null
                ? ((Number) payload.get("peakAcceleration")).doubleValue() : null;
        String detectionPhase = (String) payload.getOrDefault("detectionPhase", "UNKNOWN");

        // saved as PENDING so the patient's dashboard shows the response card
        FallAlert alert = new FallAlert(Instant.now(), peakAcceleration, detectionPhase, deviceId);
        fallAlertRepository.save(alert);

        System.out.println("[ALERT] Fall from Pi: " + deviceId
                + " | Phase: " + detectionPhase + " | Peak: " + peakAcceleration + "g");

        return ResponseEntity.ok(Map.of("message", "Fall alert received", "alertId", alert.getId()));
    }

    // MANUAL SOS

    // User pressed the SOS button - no fall needed, no countdown
    // saved straight to CONFIRMED so the caregiver's view shows it immediately
    @PostMapping("/manual")
    public ResponseEntity<?> createManualAlert(
            @RequestBody(required = false) Map<String, Object> payload,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        FallAlert alert = new FallAlert();
        alert.setDetectedAt(Instant.now());
        alert.setDetectionPhase("MANUAL_SOS");
        alert.setDeviceId("patient-app");
        alert.setStatus("CONFIRMED");  // deliberate press - skip straight to confirmed

        if (payload != null) {
            if (payload.get("latitude") != null)
                alert.setLatitude(((Number) payload.get("latitude")).doubleValue());
            if (payload.get("longitude") != null)
                alert.setLongitude(((Number) payload.get("longitude")).doubleValue());
        }

        // Store the user's username so the caregiver view can display it
        if (userDetails != null) {
            alert.setUserName(userDetails.getUsername());
            // look up the user's linked caregiver and assign the alert to them
            userRepository.findById(userDetails.getId()).ifPresent(user -> {
                if (user.getCaregiverUsername() != null) {
                    alert.setAssignedCaregiver(user.getCaregiverUsername().toLowerCase());
                }
            });
        }

        fallAlertRepository.save(alert);
        System.out.println("[ALERT] Manual SOS from " + alert.getUserName()
                + " - assigned to caregiver: " + alert.getAssignedCaregiver());

        return ResponseEntity.ok(Map.of("message", "SOS alert sent", "alertId", alert.getId()));
    }

    // USER APP POLLING

    // User dashboard calls this every 5s to check if the Pi detected a fall
    @GetMapping("/fall/latest")
    public ResponseEntity<?> getLatestPendingAlert() {
        List<FallAlert> pending = fallAlertRepository.findByStatusOrderByDetectedAtDesc("PENDING");
        if (pending.isEmpty()) {
            return ResponseEntity.ok(Map.of("pending", false));
        }
        FallAlert latest = pending.get(0);
        return ResponseEntity.ok(Map.of(
                "pending", true,
                "alertId", latest.getId(),
                "detectedAt", latest.getDetectedAt().toString(),
                "detectionPhase", latest.getDetectionPhase() != null ? latest.getDetectionPhase() : "UNKNOWN",
                "peakAcceleration", latest.getPeakAcceleration() != null ? latest.getPeakAcceleration() : 0.0
        ));
    }

    // User tapped "I'm Okay" - false alarm
    @PostMapping("/fall/{id}/cancel")
    public ResponseEntity<?> cancelAlert(@PathVariable Long id) {
        return fallAlertRepository.findById(id).map(alert -> {
            alert.setStatus("CANCELLED");
            alert.setAcknowledged(true);
            fallAlertRepository.save(alert);
            System.out.println("[ALERT] Alert " + id + " cancelled (false alarm)");
            return ResponseEntity.ok(Map.of("message", "Alert cancelled"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // User tapped "Send Help" or countdown expired
    // moves to CONFIRMED so the caregiver's polling view picks it up
    @PostMapping("/fall/{id}/confirm")
    public ResponseEntity<?> confirmAlert(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Optional<FallAlert> alertOpt = fallAlertRepository.findById(id);
        if (alertOpt.isEmpty()) return ResponseEntity.notFound().build();

        FallAlert alert = alertOpt.get();
        alert.setStatus("CONFIRMED");
        alert.setAcknowledged(true);

        if (payload.get("latitude") != null)
            alert.setLatitude(((Number) payload.get("latitude")).doubleValue());
        if (payload.get("longitude") != null)
            alert.setLongitude(((Number) payload.get("longitude")).doubleValue());

        if (userDetails != null) {
            alert.setUserName(userDetails.getUsername());
            // route the alert to this user's linked caregiver
            userRepository.findById(userDetails.getId())
                    .ifPresent(user -> alert.setAssignedCaregiver(
                            user.getCaregiverUsername() != null ? user.getCaregiverUsername().toLowerCase() : null
                    ));
        }

        fallAlertRepository.save(alert);
        System.out.println("[ALERT] Alert " + id + " confirmed by " + alert.getUserName()
                + " — assigned to caregiver: " + alert.getAssignedCaregiver());

        return ResponseEntity.ok(Map.of("message", "Alert confirmed"));
    }

    // CAREGIVER APP POLLING

    // Caregiver's view polls this every 5s — returns the latest CONFIRMED alert
    // this is what triggers the full-screen red view on the caregiver's phone
    @GetMapping("/caregiver/latest")
    public ResponseEntity<?> getLatestConfirmedAlert(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String caregiverUsername = userDetails.getUsername().toLowerCase();
        List<FallAlert> confirmed = fallAlertRepository
                .findByStatusAndAssignedCaregiverOrderByDetectedAtDesc("CONFIRMED", caregiverUsername);

        if (confirmed.isEmpty()) {
            return ResponseEntity.ok(Map.of("active", false));
        }

        FallAlert latest = confirmed.get(0);
        String phone = userRepository.findByUsername(latest.getUserName())
                .map(user -> user.getPhoneNumber())
                .orElse("");

        User user = userRepository.findByUsername(latest.getUserName()).orElse(null);

        String name = user != null ? user.getName() : latest.getUserName();

        return ResponseEntity.ok(Map.of(
                "active", true,
                "alertId", latest.getId(),
                "detectedAt", latest.getDetectedAt().toString(),
                "detectionPhase", latest.getDetectionPhase() != null ? latest.getDetectionPhase() : "Sensor",
                "peakAcceleration", latest.getPeakAcceleration() != null ? latest.getPeakAcceleration() : 0.0,
                "latitude", latest.getLatitude() != null ? latest.getLatitude() : "",
                "longitude", latest.getLongitude() != null ? latest.getLongitude() : "",
                "userName", latest.getUserName(),
                "name", name,
                "phoneNumber", phone
        ));
    }

    // Caregiver tapped a response button - marks alert as resolved
    @PostMapping("/caregiver/{id}/resolve")
    public ResponseEntity<?> resolveAlert(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        return fallAlertRepository.findById(id).map(alert -> {

            String action = payload.get("action");

            if ("onway".equals(action)) {
                alert.setStatus("CAREGIVER_ON_THE_WAY");
            }
            else if ("emergency".equals(action)) {
                alert.setStatus("EMERGENCY_SERVICES_CALLED");
            }

            alert.setAcknowledged(true);
            fallAlertRepository.save(alert);

            System.out.println("[ALERT] Alert " + id + " responded: " + alert.getStatus());

            return ResponseEntity.ok(Map.of("message", "Alert response recorded"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // PROFILE SETTINGS

    // user saves their caregiver's username from the settings screen
    @PutMapping("/caregiver-link")
    public ResponseEntity<?> updateCaregiverLink(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String caregiverUsername = payload.get("caregiverUsername");
        String phoneNumber = payload.get("phoneNumber");

        if (caregiverUsername == null || caregiverUsername.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Caregiver username is required"));
        }

        final String caregiver= caregiverUsername.toLowerCase();

        // check the caregiver account actually exists before saving
        if (!userRepository.existsByUsername(caregiver)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No account found with that username"));
        }

        return userRepository.findById(userDetails.getId()).map(user -> {
            user.setCaregiverUsername(caregiver);
            user.setPhoneNumber(phoneNumber);
            userRepository.save(user);
            System.out.println("[LINK] " + user.getUsername() + " linked to caregiver: " + caregiver);
            return ResponseEntity.ok(Map.of("message", "Caregiver linked successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // returns the currently linked caregiver username for display in settings
    @GetMapping("/caregiver-link")
    public ResponseEntity<?> getCaregiverLink(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return userRepository.findById(userDetails.getId()).map(user -> {
            String linked = user.getCaregiverUsername();
            return ResponseEntity.ok(Map.of(
                    "caregiverUsername", linked != null ? linked : "",
                    "linked", linked != null && !linked.isBlank()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile/phone")
    public ResponseEntity<?> updatePhone(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String phone = payload.get("phoneNumber");

        return userRepository.findById(userDetails.getId()).map(user -> {
            user.setPhoneNumber(phone);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Phone updated"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fall")
    public ResponseEntity<List<FallAlert>> getUnacknowledgedAlerts() {
        return ResponseEntity.ok(fallAlertRepository.findByAcknowledgedFalse());
    }

    @PutMapping("/fall/{id}/acknowledge")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable Long id) {
        return fallAlertRepository.findById(id).map(alert -> {
            alert.setAcknowledged(true);
            fallAlertRepository.save(alert);
            return ResponseEntity.ok(Map.of("message", "Alert acknowledged"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/latest")
    public ResponseEntity<?> getLatestUserAlert(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<FallAlert> alerts = fallAlertRepository
                .findByUserNameOrderByDetectedAtDesc(userDetails.getUsername());

        if (alerts.isEmpty()) {
            return ResponseEntity.ok(Map.of("active", false));
        }

        FallAlert latest = alerts.get(0);

        return ResponseEntity.ok(Map.of(
                "active", true,
                "status", latest.getStatus(),
                "detectedAt", latest.getDetectedAt()
        ));
    }
}