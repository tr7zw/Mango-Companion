package dev.tr7zw.mango_companion.util;

import java.time.Duration;

import feign.Capability;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import lombok.Getter;

public class RateLimiter implements Capability {

    @Getter
    private final Bucket rateLimiter;
    
    public RateLimiter(int requests, Duration time) {
        rateLimiter = Bucket4j.builder().addLimit(Bandwidth.simple(requests, time)).build();
    }

    @Override
    public RequestInterceptor enrich(RequestInterceptor requestInterceptor) {
        
        return new RequestInterceptor() {
            
            @Override
            public void apply(RequestTemplate template) {
                try {
                    rateLimiter.asScheduler().consume(1);
                } catch (InterruptedException e) {
                }
                requestInterceptor.apply(template);
            }
        };
                
    }
    
}
