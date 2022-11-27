package dev.tr7zw.mango_companion.util;

import java.time.Duration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

public class RateLimiter {

    private final Bucket rateLimiter;

    public RateLimiter(int requests, Duration time) {
        rateLimiter = Bucket.builder().addLimit(Bandwidth.simple(requests, time)).build();
    }

    public void consume() {
        try {
            rateLimiter.asBlocking().consume(1);
        } catch (InterruptedException e) {
        }
    }

}
