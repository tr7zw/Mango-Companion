package dev.tr7zw.mango_companion.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

import lombok.extern.java.Log;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

@Log
public class StreamUtil {

    private static RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
            .handle(IOException.class)
            .withDelay(Duration.ofSeconds(1))
            .onRetry(e -> log.warning("Error while opening connection. Retrying!"))
            .withMaxRetries(3);
    
    public static InputStream getStream(RateLimiter limiter, String url) throws IOException {
        try {
            limiter.getRateLimiter().asScheduler().consume(1);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        return Failsafe.with(retryPolicy).get(() -> new URL(url).openStream());
    }
    
}
