/**
 * Copyright 2012-2020 The Feign Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package dev.tr7zw.mango_companion.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import feign.Client;
import feign.Request.HttpMethod;
import lombok.extern.java.Log;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * This module directs Feign's http requests to
 * <a href="http://square.github.io/okhttp/">OkHttp</a>, which enables SPDY and better network
 * control. Ex.
 *
 * <pre>
 * GitHub github = Feign.builder().client(new OkHttpClient()).target(GitHub.class,
 * "https://api.github.com");
 */
@Log
public final class CachedOkHttpClient implements Client {

    private static CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    
    private static Cache<String, feign.Response> myCache = cacheManager.createCache("okhttpCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, feign.Response.class,
                                          ResourcePoolsBuilder.heap(50)).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10))).build());
    
  private final okhttp3.OkHttpClient delegate;

  public CachedOkHttpClient() {
    this(new okhttp3.OkHttpClient());
  }

  public CachedOkHttpClient(okhttp3.OkHttpClient delegate) {
    this.delegate = delegate;
  }

  static Request toOkHttpRequest(feign.Request input) {
    Request.Builder requestBuilder = new Request.Builder();
    requestBuilder.url(input.url());

    MediaType mediaType = null;
    boolean hasAcceptHeader = false;
    for (String field : input.headers().keySet()) {
      if (field.equalsIgnoreCase("Accept")) {
        hasAcceptHeader = true;
      }

      for (String value : input.headers().get(field)) {
        requestBuilder.addHeader(field, value);
        if (field.equalsIgnoreCase("Content-Type")) {
          mediaType = MediaType.parse(value);
          if (input.charset() != null) {
            mediaType.charset(input.charset());
          }
        }
      }
    }
    // Some servers choke on the default accept string.
    if (!hasAcceptHeader) {
      requestBuilder.addHeader("Accept", "*/*");
    }

    byte[] inputBody = input.body();
    boolean isMethodWithBody =
        HttpMethod.POST == input.httpMethod() || HttpMethod.PUT == input.httpMethod()
            || HttpMethod.PATCH == input.httpMethod();
    if (isMethodWithBody) {
      requestBuilder.removeHeader("Content-Type");
      if (inputBody == null) {
        // write an empty BODY to conform with okhttp 2.4.0+
        // http://johnfeng.github.io/blog/2015/06/30/okhttp-updates-post-wouldnt-be-allowed-to-have-null-body/
        inputBody = new byte[0];
      }
    }

    RequestBody body = inputBody != null ? RequestBody.create(mediaType, inputBody) : null;
    requestBuilder.method(input.httpMethod().name(), body);
    return requestBuilder.build();
  }

  private static feign.Response toFeignResponse(Response response, feign.Request request)
      throws IOException {
    return feign.Response.builder()
        .status(response.code())
        .reason(response.message())
        .request(request)
        .headers(toMap(response.headers()))
        .body(toBody(response.body()))
        .build();
  }

  private static Map<String, Collection<String>> toMap(Headers headers) {
    return (Map) headers.toMultimap();
  }

  private static feign.Response.Body toBody(final ResponseBody inputBody) throws IOException {
    if (inputBody == null || inputBody.contentLength() == 0) {
      if (inputBody != null) {
          inputBody.close();
      }
      return null;
    }
    final Integer length = inputBody.contentLength() >= 0 && inputBody.contentLength() <= Integer.MAX_VALUE
        ? (int) inputBody.contentLength()
        : null;

    byte[] data = inputBody.bytes();
    inputBody.close();
    
    return new feign.Response.Body() {

      @Override
      public void close() throws IOException {
        
      }

      @Override
      public Integer length() {
        return length;
      }

      @Override
      public boolean isRepeatable() {
        return true;
      }

      @Override
      public InputStream asInputStream() throws IOException {
        return new ByteArrayInputStream(data);
      }

      @Override
      public Reader asReader() throws IOException {
        return new InputStreamReader(asInputStream());
      }

      @Override
      public Reader asReader(Charset charset) throws IOException {
        return asReader();
      }
    };
  }

  @Override
  public feign.Response execute(feign.Request input, feign.Request.Options options)
      throws IOException {
      if(myCache.containsKey(input.url()) && input.length() == 0 && input.httpMethod() == HttpMethod.GET) {
          log.log(Level.FINE, "Cached response for " +  input.url());
          return myCache.get(input.url());
      }
    okhttp3.OkHttpClient requestScoped;
    if (delegate.connectTimeoutMillis() != options.connectTimeoutMillis()
        || delegate.readTimeoutMillis() != options.readTimeoutMillis()
        || delegate.followRedirects() != options.isFollowRedirects()) {
      requestScoped = delegate.newBuilder()
          .connectTimeout(options.connectTimeoutMillis(), TimeUnit.MILLISECONDS)
          .readTimeout(options.readTimeoutMillis(), TimeUnit.MILLISECONDS)
          .followRedirects(options.isFollowRedirects())
          .build();
    } else {
      requestScoped = delegate;
    }
    Request request = toOkHttpRequest(input);
    Response response = requestScoped.newCall(request).execute();
    feign.Response resp = toFeignResponse(response, input).toBuilder().request(input).build();
    if(input.length() == 0 && input.httpMethod() == HttpMethod.GET) {
        myCache.put(input.url(), resp);
    }
    return resp;
  }
}