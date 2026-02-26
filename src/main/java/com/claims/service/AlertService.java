package com.claims.service;

import com.claims.model.FraudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertService {

    @Autowired
    private RiskService riskService;

    public void fire(FraudEvent event) {
        log.info("Firing alert for event: {}", event.getEventId());

        if ("HIGH".equals(event.getRiskLevel())) {
            sendCriticalAlert(event);
        } else if ("MEDIUM".equals(event.getRiskLevel())) {
            sendWarningAlert(event);
        } else {
            log.debug("Low risk event - no alert needed");
        }
    }

    private void sendCriticalAlert(FraudEvent event) {
        log.error("CRITICAL ALERT: Potential fraud detected - Event ID: {}, Amount: {}, Risk Level: HIGH",
                  event.getEventId(), event.getAmount());
        // Send to alerting system (email, SMS, Slack, etc.)
    }

    private void sendWarningAlert(FraudEvent event) {
        log.warn("WARNING: Suspicious transaction - Event ID: {}, Amount: {}, Risk Level: MEDIUM",
                 event.getEventId(), event.getAmount());
        // Send to alerting system
    }
}

