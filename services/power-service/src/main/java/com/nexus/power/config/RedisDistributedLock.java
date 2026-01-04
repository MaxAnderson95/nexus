package com.nexus.power.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * A distributed lock implementation using Redis.
 *
 * This lock is safe for multi-replica deployments:
 * - Uses SETNX for atomic lock acquisition
 * - Uses Lua script for atomic check-and-delete on release
 * - Includes TTL to prevent deadlocks from crashed instances
 *
 * Redis Key Prefixes used in this application:
 * - init:lock:{service} - DataInitializer locks to prevent duplicate seed data
 * - ship:return:{shipId} - Ship return scheduling keys (TTL triggers return)
 * - ship:return:lock:{shipId} - Lock for processing ship return events
 */
public class RedisDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    // Lua script for atomic check-and-delete
    // Only deletes the key if the value matches (i.e., we own the lock)
    private static final String RELEASE_LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    private final RedisTemplate<String, String> redisTemplate;
    private final String lockKey;
    private final String lockValue;
    private final Duration ttl;

    /**
     * Creates a new distributed lock.
     *
     * @param redisTemplate Redis template for operations
     * @param lockKey The key to use for the lock
     * @param ttl Time-to-live for the lock (prevents deadlocks)
     */
    public RedisDistributedLock(RedisTemplate<String, String> redisTemplate, String lockKey, Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.lockValue = UUID.randomUUID().toString();
        this.ttl = ttl;
    }

    /**
     * Attempts to acquire the lock.
     *
     * @return true if lock was acquired, false otherwise
     */
    public boolean tryAcquire() {
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, ttl);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("Failed to acquire Redis lock '{}': {}", lockKey, e.getMessage());
            return false;
        }
    }

    /**
     * Releases the lock if owned by this instance.
     * Uses a Lua script for atomic check-and-delete to prevent
     * releasing a lock that was acquired by another instance
     * (e.g., after our lock expired and was re-acquired).
     */
    public void release() {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(RELEASE_LOCK_SCRIPT);
            script.setResultType(Long.class);

            Long result = redisTemplate.execute(script,
                    Collections.singletonList(lockKey),
                    lockValue);

            if (result != null && result == 1) {
                log.debug("Released lock '{}'", lockKey);
            } else {
                log.debug("Lock '{}' was not owned by this instance, skipping release", lockKey);
            }
        } catch (Exception e) {
            log.warn("Failed to release Redis lock '{}': {}", lockKey, e.getMessage());
        }
    }

    /**
     * Checks if the lock key exists (regardless of owner).
     *
     * @return true if lock exists, false otherwise
     */
    public boolean isLocked() {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
        } catch (Exception e) {
            log.warn("Failed to check Redis lock '{}': {}", lockKey, e.getMessage());
            return false;
        }
    }

    /**
     * Gets the lock value (owner identifier) for this instance.
     *
     * @return the unique lock value
     */
    public String getLockValue() {
        return lockValue;
    }
}
