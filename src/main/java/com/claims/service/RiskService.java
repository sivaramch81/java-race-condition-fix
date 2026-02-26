package com.claims.service;

import com.claims.model.FraudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RiskService {

    public void evaluate(FraudEvent event) {
        log.info("Evaluating fraud risk for event: {}", event.getEventId());

        // Risk evaluation logic
        if (event.getAmount() > 10000) {
            event.setRiskLevel("HIGH");
            log.warn("High risk event detected: {}", event.getEventId());
        } else if (event.getAmount() > 5000) {
            event.setRiskLevel("MEDIUM");
        } else {
            event.setRiskLevel("LOW");
        }

        log.info("Risk evaluation completed. Risk Level: {}", event.getRiskLevel());
    }
}

