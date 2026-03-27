package com.smartguardian.repository;

import com.smartguardian.model.FitbitDailySummary;
import com.smartguardian.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FitbitDailySummaryRepository extends JpaRepository<FitbitDailySummary, Long> {

    List<FitbitDailySummary> findByUserAndDateBetweenOrderByDateAsc(
            User user, LocalDate from, LocalDate to
    );

    Optional<FitbitDailySummary> findByUserAndDate(User user, LocalDate date);
}