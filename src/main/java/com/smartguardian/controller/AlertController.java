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


    // Config
    @Value("${smartguardian.device.apiKey}")
    private String deviceApiKey;


    /* ===================== PI ENDPOINT ===================== */

    // Raspberry Pi sends fall alert here
    @PostMapping("/fall")
    public ResponseEntity<?> receiveFallAlert(
            @RequestHeader("X-Device-Key") String apiKey,
            @RequestBody Map<String, Object> payload) {

        // check device API key
        if (!deviceApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid device API key"));
        }

        // read values from payload
        String deviceId = (String) payload.getOrDefault("deviceId", "unknown");

        Double peakAcceleration = payload.get("peakAcceleration") != null
                ? ((Number) payload.get("peakAcceleration")).doubleValue()
                : null;

        String detectionPhase =
                (String) payload.getOrDefault("detectionPhase", "UNKNOWN");

        // create alert (starts as pending)
        FallAlert alert =
                new FallAlert(Instant.now(), peakAcceleration, detectionPhase, deviceId);

        fallAlertRepository.save(alert);

        System.out.println("[ALERT] Fall from Pi: " + deviceId
                + " | Phase: " + detectionPhase
                + " | Peak: " + peakAcceleration + "g");

        return ResponseEntity.ok(
                Map.of(
                        "message", "Fall alert received",
                        "alertId", alert.getId()
                )
        );
    }


    /* ===================== MANUAL SOS ===================== */

    // user pressed SOS button
    @PostMapping("/manual")
    public ResponseEntity<?> createManualAlert(
            @RequestBody(required = false) Map<String, Object> payload,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        FallAlert alert = new FallAlert();

        alert.setDetectedAt(Instant.now());
        alert.setDetectionPhase("MANUAL_SOS");
        alert.setDeviceId("patient-app");
        alert.setStatus("CONFIRMED"); // manual press

        // optional GPS location
        if (payload != null) {

            if (payload.get("latitude") != null)
                alert.setLatitude(
                        ((Number) payload.get("latitude")).doubleValue()
                );

            if (payload.get("longitude") != null)
                alert.setLongitude(
                        ((Number) payload.get("longitude")).doubleValue()
                );
        }

        // attach user + caregiver
        if (userDetails != null) {

            alert.setUserName(userDetails.getUsername());

            userRepository.findById(userDetails.getId()).ifPresent(user -> {
                if (user.getCaregiverUsername() != null) {
                    alert.setAssignedCaregiver(
                            user.getCaregiverUsername().toLowerCase()
                    );
                }
            });
        }

        fallAlertRepository.save(alert);

        System.out.println(
                "[ALERT] Manual SOS from " + alert.getUserName()
                        + " - assigned to caregiver: "
                        + alert.getAssignedCaregiver()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "SOS alert sent",
                        "alertId", alert.getId()
                )
        );
    }


    /* ===================== USER APP POLLING ===================== */

    // user dashboard polls for pending fall
    @GetMapping("/fall/latest")
    public ResponseEntity<?> getLatestPendingAlert() {

        List<FallAlert> pending =
                fallAlertRepository.findByStatusOrderByDetectedAtDesc("PENDING");

        if (pending.isEmpty()) {
            return ResponseEntity.ok(Map.of("pending", false));
        }

        FallAlert latest = pending.get(0);

        return ResponseEntity.ok(
                Map.of(
                        "pending", true,
                        "alertId", latest.getId(),
                        "detectedAt", latest.getDetectedAt().toString(),
                        "detectionPhase",
                        latest.getDetectionPhase() != null
                                ? latest.getDetectionPhase()
                                : "UNKNOWN",
                        "peakAcceleration",
                        latest.getPeakAcceleration() != null
                                ? latest.getPeakAcceleration()
                                : 0.0
                )
        );
    }


    /* ===================== USER ACTIONS ===================== */

    // user pressed "I'm OK"
    @PostMapping("/fall/{id}/cancel")
    public ResponseEntity<?> cancelAlert(@PathVariable Long id) {

        return fallAlertRepository.findById(id).map(alert -> {

            alert.setStatus("CANCELLED");
            alert.setAcknowledged(true);

            fallAlertRepository.save(alert);

            System.out.println(
                    "[ALERT] Alert " + id + " cancelled (false alarm)"
            );

            return ResponseEntity.ok(
                    Map.of("message", "Alert cancelled")
            );

        }).orElse(ResponseEntity.notFound().build());
    }


    // user pressed "Send Help"
    @PostMapping("/fall/{id}/confirm")
    public ResponseEntity<?> confirmAlert(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Optional<FallAlert> alertOpt =
                fallAlertRepository.findById(id);

        if (alertOpt.isEmpty())
            return ResponseEntity.notFound().build();

        FallAlert alert = alertOpt.get();

        alert.setStatus("CONFIRMED");
        alert.setAcknowledged(true);

        // optional GPS location
        if (payload.get("latitude") != null)
            alert.setLatitude(
                    ((Number) payload.get("latitude")).doubleValue()
            );

        if (payload.get("longitude") != null)
            alert.setLongitude(
                    ((Number) payload.get("longitude")).doubleValue()
            );

        // attach caregiver
        if (userDetails != null) {

            alert.setUserName(userDetails.getUsername());

            userRepository.findById(userDetails.getId())
                    .ifPresent(user ->
                            alert.setAssignedCaregiver(
                                    user.getCaregiverUsername() != null
                                            ? user.getCaregiverUsername().toLowerCase()
                                            : null
                            ));
        }

        fallAlertRepository.save(alert);

        System.out.println(
                "[ALERT] Alert " + id + " confirmed by "
                        + alert.getUserName()
                        + " — assigned to caregiver: "
                        + alert.getAssignedCaregiver()
        );

        return ResponseEntity.ok(
                Map.of("message", "Alert confirmed")
        );
    }


    /* ===================== CAREGIVER APP POLLING ===================== */

    // caregiver checks for confirmed alerts
    @GetMapping("/caregiver/latest")
    public ResponseEntity<?> getLatestConfirmedAlert(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String caregiverUsername =
                userDetails.getUsername().toLowerCase();

        List<FallAlert> confirmed =
                fallAlertRepository
                        .findByStatusAndAssignedCaregiverOrderByDetectedAtDesc(
                                "CONFIRMED",
                                caregiverUsername
                        );

        if (confirmed.isEmpty()) {
            return ResponseEntity.ok(Map.of("active", false));
        }

        FallAlert latest = confirmed.get(0);

        String phone =
                userRepository.findByUsername(latest.getUserName())
                        .map(user -> user.getPhoneNumber())
                        .orElse("");

        User user =
                userRepository.findByUsername(latest.getUserName())
                        .orElse(null);

        String name =
                user != null
                        ? user.getName()
                        : latest.getUserName();

        return ResponseEntity.ok(
                Map.of(
                        "active", true,
                        "alertId", latest.getId(),
                        "detectedAt", latest.getDetectedAt().toString(),
                        "detectionPhase",
                        latest.getDetectionPhase() != null
                                ? latest.getDetectionPhase()
                                : "Sensor",
                        "peakAcceleration",
                        latest.getPeakAcceleration() != null
                                ? latest.getPeakAcceleration()
                                : 0.0,
                        "latitude",
                        latest.getLatitude() != null
                                ? latest.getLatitude()
                                : "",
                        "longitude",
                        latest.getLongitude() != null
                                ? latest.getLongitude()
                                : "",
                        "userName", latest.getUserName(),
                        "name", name,
                        "phoneNumber", phone
                )
        );
    }


    /* ===================== CAREGIVER ACTIONS ===================== */

    // caregiver responds to alert
    @PostMapping("/caregiver/{id}/resolve")
    public ResponseEntity<?> resolveAlert(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        return fallAlertRepository.findById(id).map(alert -> {

            String action = payload.get("action");

            if ("onway".equals(action)) {
                alert.setStatus("CAREGIVER_ON_THE_WAY");
            } else if ("emergency".equals(action)) {
                alert.setStatus("EMERGENCY_SERVICES_CALLED");
            }

            alert.setAcknowledged(true);

            fallAlertRepository.save(alert);

            System.out.println(
                    "[ALERT] Alert " + id
                            + " responded: "
                            + alert.getStatus()
            );

            return ResponseEntity.ok(
                    Map.of("message", "Alert response recorded")
            );

        }).orElse(ResponseEntity.notFound().build());
    }


    /* ===================== PROFILE SETTINGS ===================== */

    // link caregiver username
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

        final String caregiver = caregiverUsername.toLowerCase();

        // check caregiver exists
        if (!userRepository.existsByUsername(caregiver)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No account found with that username"));
        }

        return userRepository.findById(userDetails.getId()).map(user -> {

            user.setCaregiverUsername(caregiver);
            user.setPhoneNumber(phoneNumber);

            userRepository.save(user);

            System.out.println(
                    "[LINK] "
                            + user.getUsername()
                            + " linked to caregiver: "
                            + caregiver
            );

            return ResponseEntity.ok(
                    Map.of("message", "Caregiver linked successfully")
            );

        }).orElse(ResponseEntity.notFound().build());
    }
}