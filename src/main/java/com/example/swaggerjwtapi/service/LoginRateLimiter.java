package com.example.swaggerjwtapi.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiter {

    private final Map<String, Bucket> buckets
            = new ConcurrentHashMap<>();

    public boolean allowLogin(String username) {
        Bucket bucket =
                buckets.computeIfAbsent(
                        username,
                        k -> createBucket()
                );

        return bucket.tryConsume(1);
    }

    private Bucket createBucket() {
        Bandwidth limit =
                Bandwidth.classic(
                        5,
                        Refill.intervally(
                                5,
                                Duration.ofMinutes(15)
                        )
                );

        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}
