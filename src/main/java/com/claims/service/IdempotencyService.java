package com.claims.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class IdempotencyService {

    @Autowired
    private RedisTemplate<String, String> redis;

    private static final String DEDUP_PREFIX = "dedup:";
    private static final Duration TTL = Duration.ofHours(24);

    public boolean isDuplicate(String eventId) {
        String key = DEDUP_PREFIX + eventId;
        return Boolean.TRUE.equals(redis.hasKey(key));
    }

    public void markAsProcessed(String eventId) {
        String key = DEDUP_PREFIX + eventId;
        redis.opsForValue().set(key, "1", TTL);
        log.debug("Event marked as processed: {}", eventId);
    }

    public void cleanup(String eventId) {
        String key = DEDUP_PREFIX + eventId;
        redis.delete(key);
    }
}

