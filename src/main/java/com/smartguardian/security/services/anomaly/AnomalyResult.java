package com.smartguardian.security.services.anomaly;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnomalyResult {
    private boolean anomalyDetected;
    private String message;
    private List<String> flags;
}