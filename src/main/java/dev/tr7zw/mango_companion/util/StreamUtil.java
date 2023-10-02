package dev.tr7zw.mango_companion.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.logging.Level;

import feign.Client;
import lombok.extern.java.Log;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Log
public class StreamUtil {

    private final static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/118.0";
    private static RetryPolicy<Object> retryPolicy = new RetryPolicy<>().handle(IOException.class)
            .withDelay(Duration.ofSeconds(1)).onRetry(e -> log.log(Level.WARNING, "Error while opening connection. Retrying!", e))
            .withMaxRetries(3);

    public static InputStream getStream(RateLimiter limiter, String url) throws IOException {
        limiter.consume();
        return Failsafe.with(retryPolicy).get(() -> {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            connection.addRequestProperty("Referer", url);
            connection.setConnectTimeout((int) Duration.ofSeconds(20).toMillis());
            connection.setReadTimeout((int) Duration.ofSeconds(20).toMillis());
            return connection.getInputStream();
        });
    }

    public static InputStream getStreamNoReferer(RateLimiter limiter, String url) throws IOException {
        limiter.consume();
        return Failsafe.with(retryPolicy).get(() -> {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setConnectTimeout((int) Duration.ofSeconds(20).toMillis());
            connection.setReadTimeout((int) Duration.ofSeconds(20).toMillis());
            return connection.getInputStream();
        });
    }

    public static Client getClient(RateLimiter rateLimiter) {
        return new CachedOkHttpClient(rateLimiter, new okhttp3.OkHttpClient(new okhttp3.OkHttpClient.Builder()
                .addNetworkInterceptor(new UserAgentInterceptor(userAgent)).connectTimeout(Duration.ofSeconds(20)).readTimeout(Duration.ofSeconds(60))));
    }

    private static class UserAgentInterceptor implements Interceptor {

        private final String userAgent;

        public UserAgentInterceptor(String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder().header("User-Agent", userAgent).build();
            return chain.proceed(requestWithUserAgent);
        }
    }

}
