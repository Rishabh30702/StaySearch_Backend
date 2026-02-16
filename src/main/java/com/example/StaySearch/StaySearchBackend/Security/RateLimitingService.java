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

    // Store buckets for each IP address
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // Replace the deprecated Refill.intervally() with the modern builder
        Bandwidth limit = Bandwidth.builder()
                .capacity(5) // Max 5 tokens
                .refillIntervally(5, Duration.ofMinutes(20)) // Refill 5 tokens every 2 minutes
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, k -> createNewBucket());
    }

    public Bucket resolveAdminBucket(String ip) {
        // Create a unique key for admin path + IP
        return cache.computeIfAbsent("ADMIN_" + ip, k -> createStrictBucket());
    }

    private Bucket createStrictBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(3) // Only 3 tries for admins!
                        .refillIntervally(3, Duration.ofMinutes(15)) // 15 minute lockout
                        .build())
                .build();
    }





}
