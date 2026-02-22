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

    // Acknowledged by caregiver in the dashboard
    private boolean acknowledged = false;

    public FallAlert(Instant detectedAt, Double peakAcceleration, String detectionPhase, String deviceId) {
        this.detectedAt = detectedAt;
        this.peakAcceleration = peakAcceleration;
        this.detectionPhase = detectionPhase;
        this.deviceId = deviceId;
    }
}