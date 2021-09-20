package dev.tr7zw.mango_companion.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;

import feign.okhttp.OkHttpClient;
import lombok.extern.java.Log;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Log
public class StreamUtil {

    private final static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0";
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
            connection.setRequestProperty("User-Agent", userAgent);
            connection.addRequestProperty("Referer", url);
            return connection.getInputStream(); 
        });
    }
    
    public static OkHttpClient getClient() {
        return new OkHttpClient(new okhttp3.OkHttpClient(new okhttp3.OkHttpClient.Builder().addNetworkInterceptor(new UserAgentInterceptor(userAgent))));
    }
    
    private static class UserAgentInterceptor implements Interceptor {

        private final String userAgent;

        public UserAgentInterceptor(String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", userAgent)
                .build();
            return chain.proceed(requestWithUserAgent);
        }
    }
    
}
