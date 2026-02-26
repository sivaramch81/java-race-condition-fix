package com.claims.kafka;

import com.claims.model.FraudEvent;
import com.claims.service.AlertService;
import com.claims.service.IdempotencyService;
import com.claims.service.RiskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class ClaimsEventListener {

    @Autowired
    private RedisTemplate<String, String> redis;

    @Autowired
    private RiskService riskService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private IdempotencyService idempotencyService;

    /**
     * ❌ BROKEN: Race condition - auto-commit enabled
     * - enable.auto.commit = true
     * - duplicate processing possible on restart
     * - offset not guaranteed to match processing
     */
    @KafkaListener(
        topics = "fraud-events",
        groupId = "fraud-detector-unsafe",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onFraudEventUnsafe(ConsumerRecord<String, FraudEvent> record) {
        log.warn("Processing with UNSAFE listener (race condition risk): {}", record.value().getEventId());
        riskService.evaluate(record.value());
        alertService.fire(record.value());
    }

    /**
     * ✅ FIXED: Race condition prevention with:
     * - Manual offset commit (enable.auto.commit = false)
     * - Idempotency guard using Redis
     * - Concurrency matching partition count
     * - Exception handling with acknowledgment
     */
    @KafkaListener(
        topics = "fraud-events",
        groupId = "fraud-detector-safe",
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "6"  // Matches partition count
    )
    public void onFraudEventFixed(
            ConsumerRecord<String, FraudEvent> record,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {

        FraudEvent event = record.value();
        String eventId = event.getEventId();
        String dedupKey = "dedup:" + eventId;

        try {
            // Idempotency guard: skip if already processed
            if (Boolean.TRUE.equals(redis.hasKey(dedupKey))) {
                log.info("Duplicate event detected, skipping: {}", eventId);
                ack.acknowledge();
                return;
            }

            log.info("Processing fraud event [Partition: {}]: {}", partition, eventId);

            // Process the event
            riskService.evaluate(event);
            alertService.fire(event);

            // Mark as processed in Redis with 24-hour TTL
            redis.opsForValue().set(dedupKey, "1", Duration.ofHours(24));

            // Commit offset only after successful processing
            ack.acknowledge();
            log.info("Event processed and offset committed: {}", eventId);

        } catch (Exception e) {
            log.error("Error processing fraud event: {} - Exception: {}", eventId, e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
}

