package com.smartguardian.repository;

import com.smartguardian.model.FallAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/* ===================== FALL ALERT REPOSITORY ===================== */

@Repository
public interface FallAlertRepository extends JpaRepository<FallAlert, Long> {


    /* ===================== DASHBOARD ===================== */

    // fetch all unacknowledged alerts
    List<FallAlert> findByAcknowledgedFalse();


    /* ===================== STATUS ===================== */

    // Find alerts by status, newest first
    List<FallAlert> findByStatusOrderByDetectedAtDesc(String status);


    /* ===================== CAREGIVER ===================== */

    // alerts assigned to a specific caregiver
    List<FallAlert> findByStatusAndAssignedCaregiverOrderByDetectedAtDesc(String status, String assignedCaregiver);


    /* ===================== USER ===================== */

    // alerts for a specific user
    List<FallAlert> findByUserNameOrderByDetectedAtDesc(String userName);
}