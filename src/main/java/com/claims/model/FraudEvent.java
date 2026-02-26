package com.claims.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudEvent {
    private String eventId;
    private String claimId;
    private double amount;
    private String eventType;
    private LocalDateTime timestamp;
    private String riskLevel;
}

