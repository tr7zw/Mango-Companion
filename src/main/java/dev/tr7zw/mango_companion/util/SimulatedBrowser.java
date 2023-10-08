package dev.tr7zw.mango_companion.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.BrowserType.LaunchOptions;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;
import feign.Response.Builder;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;

public class SimulatedBrowser implements Client {

    private boolean initialized = false;
    private Browser browser;
    private BrowserContext context;

    public void init() {
        if (initialized) {
            return;
        }
        try {
            Playwright playwright = Playwright.create();
            browser = playwright.firefox().launch(new LaunchOptions().setHeadless(false)
                    .setFirefoxUserPrefs(Map.of("dom.image-lazy-loading.enabled", false)).setTimeout(60000));
            context = browser.newContext(new NewContextOptions()
                    .setJavaScriptEnabled(true));
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("Playwright unable to initialize", e);
        }
    }

    public String getPageContent(String url) throws IOException {
        init();
        Page page = context.newPage();
        page.setViewportSize(1920, 1080);
        page.navigate(url);
        int tries = 0;
        while (true) {
            page.waitForLoadState();
            try {
                if (page.content().toLowerCase().contains("cloudflare")) {
                    tries++;
                    if (tries > 10) {
                        page.close();
                        throw new FileNotFoundException("Cloudflare is not letting us through.");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        page.close();
                        throw new IOException();
                    }
                } else {
                    break;
                }
            } catch (PlaywrightException ex) {
                tries++;
            }
        }
        String content = page.content();
        page.close();
        return content;
    }

    public String getPageTitle(String url) throws IOException {
        init();
        Page page = context.newPage();
        page.setViewportSize(1920, 1080);
        page.navigate(url);
        int tries = 0;
        while (true) {
            page.waitForLoadState();
            if (page.content().toLowerCase().contains("cloudflare")) {
                tries++;
                if (tries > 10) {
                    page.close();
                    throw new FileNotFoundException("Cloudflare is not letting us through.");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    page.close();
                    throw new IOException();
                }
            } else {
                break;
            }
        }
        String content = page.title();
        page.close();
        return content;
    }

    public void downloadImages(String url, AtomicInteger counter, ZipCreator zip) throws IOException {
        init();
        Page page = context.newPage();
        page.setViewportSize(1920, 1080);
        Set<String> requestedImages = new HashSet<String>();
        Set<String> foundImages = new HashSet<String>();
        page.onRequest(request -> {
            if (request.url().endsWith(".jpg")
                    && request.url().substring(request.url().lastIndexOf('/') + 1).matches("[0-9]+\\.jpg")) {
                requestedImages.add(request.url());
            }
        });
        page.onRequestFinished(request -> {
            if (request.url().endsWith(".jpg") && !foundImages.contains(request.url())
                    && request.url().substring(request.url().lastIndexOf('/') + 1).matches("[0-9]+\\.jpg")) {
                foundImages.add(request.url());
                System.out.println(request.url());
                counter.incrementAndGet();
                try {
                    zip.addFile(request.url().substring(request.url().lastIndexOf('/') + 1), request.response().body());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        page.navigate(url);
        int tries = 0;
        while (true) {
            page.waitForLoadState();
            if (page.content().toLowerCase().contains("cloudflare")) {
                tries++;
                if (tries > 10) {
                    page.close();
                    throw new FileNotFoundException("Cloudflare is not letting us through.");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    page.close();
                    throw new IOException();
                }
            } else {
                break;
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            page.close();
            throw new IOException();
        }
        page.waitForLoadState(LoadState.LOAD);
        String downloadTrigger = "// Replace 'img' with your desired tag\r\n"
                + "var elements = document.getElementsByTagName('img');\r\n" + "\r\n"
                + "for (var i = 0; i < elements.length; i++) {\r\n"
                + "    // Check if the element has the \"lazyloaded\" class and the src attribute is not set\r\n"
                + "    if (!elements[i].src) {\r\n" + "        // Remove the \"lazyloaded\" class\r\n"
                + "        elements[i].classList.remove('lazyloaded');\r\n" + "\r\n"
                + "        // Trigger the download by setting the src attribute\r\n"
                + "        elements[i].src = elements[i].getAttribute('data-src');\r\n" + "    }\r\n" + "}";
        page.evaluate(downloadTrigger);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            page.close();
            throw new IOException();
        }
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.close();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            page.close();
            throw new IOException();
        }
        try {
            tries = 0;
            while (requestedImages.size() != foundImages.size()) {
                tries++;
                if (tries >= 10) {
                    page.close();
                    throw new IOException("Download took too long! Missing "
                            + (requestedImages.size() - foundImages.size()) + " images.");
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            page.close();
            throw new IOException();
        }
    }

    @Override
    public Response execute(Request request, Options options) throws IOException {
        init();
        Page page = context.newPage();
        page.setViewportSize(1920, 1080);
        AtomicReference<com.microsoft.playwright.Request> requestInfo = new AtomicReference<>(null);
        page.onRequestFinished(req -> {
            if (request.url().equals(req.url())) {
                requestInfo.set(req);
            }
        });
        page.navigate(request.url());
        int tries = 0;
        while (true) {
            page.waitForLoadState();
            if (page.content().toLowerCase().contains("cloudflare")) {
                tries++;
                if (tries > 10) {
                    page.close();
                    throw new FileNotFoundException("Cloudflare is not letting us through.");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    page.close();
                    throw new IOException();
                }
            } else {
                break;
            }
        }
        String content = page.content();
        Builder builder = Response.builder().body(content, StandardCharsets.UTF_8);
        builder.request(request);
        if (requestInfo.get() != null) {
            builder.status(requestInfo.get().response().status());
        } else {
            System.out.println("Assuming 200 response");
            builder.status(200);
        }
        page.close();
        return builder.build();
    }

}
