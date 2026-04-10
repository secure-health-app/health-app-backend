package com.smartguardian.repository;

import com.smartguardian.model.FitbitDailySummary;
import com.smartguardian.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/* ===================== FITBIT DAILY SUMMARY REPOSITORY ===================== */

@Repository
public interface FitbitDailySummaryRepository extends JpaRepository<FitbitDailySummary, Long> {


    /* ===================== DATE RANGE ===================== */

    // get summaries between two dates (ascending)
    List<FitbitDailySummary> findByUserAndDateBetweenOrderByDateAsc(
            User user,
            LocalDate from,
            LocalDate to
    );

    /* ===================== SINGLE DATE ===================== */

    // get summary for specific date
    Optional<FitbitDailySummary> findByUserAndDate(
            User user,
            LocalDate date
    );
}