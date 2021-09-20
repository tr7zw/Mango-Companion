package dev.tr7zw.mango_companion.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
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
        return Failsafe.with(retryPolicy).get(() -> {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0");
            connection.addRequestProperty("Referer", url);
            return connection.getInputStream(); 
        });
    }
    
}
