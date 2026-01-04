package com.nexus.docking.config;

import com.nexus.docking.service.DockingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.time.Duration;

/**
 * Configuration for Redis keyspace notifications to handle ship return scheduling.
 *
 * When a ship undocks, a Redis key with TTL is set. When the key expires,
 * the keyspace notification triggers transitionShipToIncoming().
 *
 * This approach is:
 * - Stateless: survives pod restarts
 * - Scalable: works with multiple replicas (only one processes via distributed lock)
 * - Idle-friendly: no polling, events only on expiration
 */
@Configuration
public class RedisKeyExpirationConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisKeyExpirationConfig.class);
    private static final String SHIP_RETURN_PREFIX = "ship:return:";
    private static final String SHIP_RETURN_LOCK_PREFIX = "ship:return:lock:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            DockingService dockingService,
            RedisTemplate<String, String> redisTemplate) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Listen for expired key events on database 0
        container.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                String expiredKey = new String(message.getBody());

                // Only process ship return keys
                if (expiredKey.startsWith(SHIP_RETURN_PREFIX)) {
                    String shipIdStr = expiredKey.substring(SHIP_RETURN_PREFIX.length());
                    try {
                        Long shipId = Long.parseLong(shipIdStr);

                        // Use distributed lock to prevent duplicate processing across replicas
                        String lockKey = SHIP_RETURN_LOCK_PREFIX + shipId;
                        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, lockKey, LOCK_TTL);

                        if (lock.tryAcquire()) {
                            log.info("Processing ship return for ship ID: {} (acquired lock)", shipId);
                            try {
                                dockingService.transitionShipToIncoming(shipId);
                            } finally {
                                // Safe release - only deletes if we own the lock
                                lock.release();
                            }
                        } else {
                            log.debug("Skipping ship return for ship ID: {} (lock held by another instance)", shipId);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ship ID in expired key: {}", expiredKey);
                    } catch (Exception e) {
                        log.error("Failed to process ship return for key {}: {}", expiredKey, e.getMessage());
                    }
                }
            }
        }, new PatternTopic("__keyevent@0__:expired"));

        return container;
    }
}
