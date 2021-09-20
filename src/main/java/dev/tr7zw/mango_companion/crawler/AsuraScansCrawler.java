package dev.tr7zw.mango_companion.crawler;

import java.io.IOException;
import java.time.Duration;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import lombok.extern.java.Log;

@Log
public class AsuraScansCrawler {

    private Bucket rateLimiter = Bucket4j.builder().addLimit(Bandwidth.simple(5, Duration.ofSeconds(1))).build();
    
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
    
}
