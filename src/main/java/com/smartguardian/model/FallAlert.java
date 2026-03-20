package com.smartguardian.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "fall_alerts")
@Data
@NoArgsConstructor
public class FallAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // When the fall was detected on the Pi
    private Instant detectedAt;

    // Peak acceleration magnitude that triggered the alert (in g)
    private Double peakAcceleration;

    // Detection phase that confirmed the fall e.g. "IMPACT", "STILLNESS"
    private String detectionPhase;

    // Which device sent the alert e.g. "raspberry-pi-sense-hat"
    private String deviceId;

    private boolean seenByUser = false;

    // PENDING   = Pi detected fall, waiting for patient to respond
    // CONFIRMED = patient confirmed / SOS pressed or countdown expired - shows on caregiver view
    // CANCELLED = patient said false alarm
    // RESOLVED  = caregiver acknowledged and responded
    @Column(nullable = false)
    private String status = "PENDING";

    // Acknowledged by caregiver in the dashboard
    private boolean acknowledged = false;

    // GPS coordinates captured from the user's browser when they confirm
    private Double latitude;
    private Double longitude;

    // stored so the caregiver view can display who needs help
    private String userName;

    // which caregiver this alert is routed to
    // copied from user.caregiverUsername at the time the alert is confirmed
    private String assignedCaregiver;

    public FallAlert(Instant detectedAt, Double peakAcceleration, String detectionPhase, String deviceId) {
        this.detectedAt = detectedAt;
        this.peakAcceleration = peakAcceleration;
        this.detectionPhase = detectionPhase;
        this.deviceId = deviceId;
        this.status = "PENDING";
        this.acknowledged = false;
    }
}