package com.smartguardian.repository;

import com.smartguardian.model.FallAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FallAlertRepository extends JpaRepository<FallAlert, Long> {

    // Fetch all unacknowledged alerts - used by the dashboard
    List<FallAlert> findByAcknowledgedFalse();

    // Find alerts by status, newest first
    List<FallAlert> findByStatusOrderByDetectedAtDesc(String status);

    // used by the caregiver endpoint - only returns alerts assigned to this caregiver
    List<FallAlert> findByStatusAndAssignedCaregiverOrderByDetectedAtDesc(String status, String assignedCaregiver);
}