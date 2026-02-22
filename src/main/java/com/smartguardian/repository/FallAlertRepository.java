package com.smartguardian.repository;

import com.smartguardian.model.FallAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FallAlertRepository extends JpaRepository<FallAlert, Long> {

    // Fetch all unacknowledged alerts - used by the dashboard
    List<FallAlert> findByAcknowledgedFalse();

    // Fetch alerts by device ID - useful for multi-device setups later
    List<FallAlert> findByDeviceId(String deviceId);
}