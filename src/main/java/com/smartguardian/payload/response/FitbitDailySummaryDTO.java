package com.smartguardian.payload.response;

import lombok.Data;
import java.time.LocalDate;


/* ===================== FITBIT DAILY SUMMARY DTO ===================== */

// Lightweight API response model for frontend dashboard/history views
@Data
public class FitbitDailySummaryDTO {
    private Long id;
    private LocalDate date;
    private Integer restingHeartRate;
    private Integer steps;
    private Integer sleepMinutes;
    private Integer activeMinutes;
}