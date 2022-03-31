package dev.tr7zw.mango_companion.util;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import java.time.Duration;

public class RateLimiter {

  private final Bucket rateLimiter;

  public RateLimiter(int requests, Duration time) {
    rateLimiter = Bucket4j.builder().addLimit(Bandwidth.simple(requests, time)).build();
  }

  public void consume() {
    try {
      rateLimiter.asScheduler().consume(1);
    } catch (InterruptedException e) {
    }
  }
}
