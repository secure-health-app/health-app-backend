package com.smartguardian.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


/* ===================== FITBIT DAILY SUMMARY ===================== */

@Entity
@Table(name = "fitbit_daily_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FitbitDailySummary {


    /* ===================== PRIMARY KEY ===================== */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /* ===================== RELATIONSHIP ===================== */

    // user this summary belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    // date of Fitbit data
    @Column(nullable = false)
    private LocalDate date;

    // resting heart rate for the day
    private Integer restingHeartRate;

    // step count for the day
    private Integer steps;

    // total sleep minutes
    private Integer sleepMinutes;

    // when this data was fetched from Fitbit
    @Column(updatable = false)
    private LocalDateTime fetchedAt = LocalDateTime.now();
}