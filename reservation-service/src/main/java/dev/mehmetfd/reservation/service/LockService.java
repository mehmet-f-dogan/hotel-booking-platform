package dev.mehmetfd.reservation.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import dev.mehmetfd.common.exception.TryLaterException;

import java.util.concurrent.TimeUnit;

@Service
public class LockService {

    private final StringRedisTemplate redisTemplate;

    public LockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String key, long ttlSeconds) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
    }

    public void executeWithLock(String key, Runnable action) {
        executeWithLock(key, action, 10, 3, 200);
    }

    public void executeWithLock(String key, Runnable action, long ttlSeconds, int maxRetries, long retryDelayMs) {
        int attempts = 0;
        boolean locked = false;

        while (attempts < maxRetries) {
            if (tryLock(key, ttlSeconds)) {
                locked = true;
                break;
            }
            attempts++;
            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TryLaterException();
            }
        }

        if (!locked) {
            throw new TryLaterException();
        }

        try {
            action.run();
        } finally {
            unlock(key);
        }
    }
}
