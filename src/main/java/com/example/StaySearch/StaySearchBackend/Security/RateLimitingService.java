package com.example.StaySearch.StaySearchBackend.Security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    // --- BUCKET4J LOGIC (Throttling) ---
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    // Normal User: 5 tries, refills every 20 mins
    public Bucket resolveBucket(String ip) {
        return bucketCache.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofMinutes(20))
                        .build())
                .build());
    }

    // Admin User: 3 tries, refills every 15 mins
    public Bucket resolveAdminBucket(String ip) {
        return bucketCache.computeIfAbsent("ADMIN_" + ip, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillIntervally(3, Duration.ofMinutes(15))
                        .build())
                .build());
    }

    // --- LOCKOUT LOGIC (Hard Block after failures) ---
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedIps = new ConcurrentHashMap<>();

    private final int MAX_STRIKES = 5;
    private final long LOCKOUT_DURATION = 15 * 60 * 1000; // 15 Minutes

    public boolean isIpBlocked(String ip) {
        if (blockedIps.containsKey(ip)) {
            if (System.currentTimeMillis() < blockedIps.get(ip)) {
                return true;
            }
            // Block expired, clean up
            blockedIps.remove(ip);
            failedAttempts.remove(ip);
        }
        return false;
    }

    public void recordFailedAttempt(String ip) {
        int strikes = failedAttempts.getOrDefault(ip, 0) + 1;
        failedAttempts.put(ip, strikes);
        if (strikes >= MAX_STRIKES) {
            blockedIps.put(ip, System.currentTimeMillis() + LOCKOUT_DURATION);
        }
    }

    public void resetAttempts(String ip) {
        failedAttempts.remove(ip);
        blockedIps.remove(ip);
        // Also reset the bucket so they can try again immediately after successful reset
        bucketCache.remove(ip);
        bucketCache.remove("ADMIN_" + ip);
    }


}
