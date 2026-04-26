package com.smartguardian.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


/* ===================== FALL ALERT ENTITY ===================== */

@Entity
@Table(name = "fall_alerts")
@Data
@NoArgsConstructor
public class FallAlert {


    /* ===================== PRIMARY KEY ===================== */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /* ===================== DETECTION DATA ===================== */

    // when the fall was detected on the Pi
    private Instant detectedAt;

    // peak acceleration magnitude that triggered the alert (in g)
    private Double peakAcceleration;

    // detection phase that confirmed the fall e.g. IMPACT/STILLNESS
    private String detectionPhase;

    // device that sent the alert e.g. "raspberry-pi-sense-hat"
    private String deviceId;


    /* ===================== USER FLAGS ===================== */

    // prevents repeat caregiver status banners once viewed
    private boolean seenByUser = false;


    /* ===================== STATUS ===================== */

    // PENDING = Pi detected fall, waiting for user response
    // CONFIRMED = help required and visible to caregiver
    // CANCELLED = false alarm dismissed by user
    // CAREGIVER_ON_THE_WAY = caregiver acknowledged and is travelling
    // EMERGENCY_SERVICES_CALLED = caregiver escalated to emergency services
    @Column(nullable = false)
    private String status = "PENDING";


    /* ===================== CAREGIVER FLAGS ===================== */

    // tracks whether caregiver has responded to active alert
    private boolean acknowledged = false;

    // GPS coordinates captured from the user's browser when they confirm
    private Double latitude;
    private Double longitude;

    // stored so the caregiver view can display who needs help
    private String userName;

    // which caregiver this alert is routed to
    // copied from user.caregiverUsername at the time the alert is confirmed
    private String assignedCaregiver;

    public FallAlert(
            Instant detectedAt,
            Double peakAcceleration,
            String detectionPhase,
            String deviceId) {

        this.detectedAt = detectedAt;
        this.peakAcceleration = peakAcceleration;
        this.detectionPhase = detectionPhase;
        this.deviceId = deviceId;
        this.status = "PENDING";
        this.acknowledged = false;
    }
}