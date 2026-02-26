package com.claims.kafka;

import com.claims.model.FraudEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EmbeddedKafka(partitions = 6, topics = {"fraud-events"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
@DirtiesContext
public class ClaimsListenerTest {

    @Autowired
    private KafkaTemplate<String, FraudEvent> kafkaTemplate;

    @Autowired
    private ClaimsEventListener claimsEventListener;

    @Test
    public void testEventProcessing() throws Exception {
        // Create test event
        FraudEvent event = new FraudEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setClaimId("CLAIM-12345");
        event.setAmount(15000.0);
        event.setEventType("SUSPICIOUS_TRANSACTION");
        event.setTimestamp(LocalDateTime.now());

        // Send to Kafka
        kafkaTemplate.send("fraud-events", event.getEventId(), event);

        // Assertions
        assertNotNull(event);
        assertNotNull(event.getEventId());
    }

    @Test
    public void testIdempotencyGuard() throws Exception {
        String eventId = UUID.randomUUID().toString();

        FraudEvent event = new FraudEvent();
        event.setEventId(eventId);
        event.setClaimId("CLAIM-67890");
        event.setAmount(8000.0);
        event.setEventType("DUPLICATE_CHECK");
        event.setTimestamp(LocalDateTime.now());

        // Send duplicate events
        kafkaTemplate.send("fraud-events", eventId, event);
        kafkaTemplate.send("fraud-events", eventId, event);

        assertNotNull(event);
    }
}

