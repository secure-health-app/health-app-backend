package com.smartguardian.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fitbit_daily_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FitbitDailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    private Integer restingHeartRate;
    private Integer steps;
    private Integer sleepMinutes;

    @Column(updatable = false)
    private LocalDateTime fetchedAt = LocalDateTime.now();
}