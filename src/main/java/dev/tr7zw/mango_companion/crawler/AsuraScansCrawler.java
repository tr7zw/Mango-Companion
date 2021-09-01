package dev.tr7zw.mango_companion.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import lombok.extern.java.Log;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

@Log
public class AsuraScansCrawler {

    private Bucket rateLimiter = Bucket4j.builder().addLimit(Bandwidth.simple(5, Duration.ofSeconds(1))).build();
    private RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
            .handle(IOException.class)
            .withDelay(Duration.ofSeconds(1))
            .onRetry(e -> log.warning("Error while opening connection. Retrying!"))
            .withMaxRetries(3);
    
    public Document getDocument(String url) throws IOException {
        try {
            rateLimiter.asScheduler().consume(1);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        Connection con = Jsoup.connect(url).userAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0")
                .timeout(20000);
        return con.get();
    }
    
    public InputStream getStream(String url) throws IOException {
        try {
            rateLimiter.asScheduler().consume(1);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        return Failsafe.with(retryPolicy).get(() -> {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0");
            connection.addRequestProperty("Accept", "image/webp,*/*");
            connection.addRequestProperty("Referer", url);
            return connection.getInputStream(); 
        });
    }
    
    
}
