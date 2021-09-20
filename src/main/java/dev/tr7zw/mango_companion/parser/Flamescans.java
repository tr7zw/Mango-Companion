package dev.tr7zw.mango_companion.parser;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

import dev.tr7zw.mango_companion.util.APIProxyBuilder;
import dev.tr7zw.mango_companion.util.EmptyEncoder;
import dev.tr7zw.mango_companion.util.HTMLPojoDecoder;
import dev.tr7zw.mango_companion.util.RateLimiter;
import dev.tr7zw.mango_companion.util.StreamUtil;
import dev.tr7zw.mango_companion.util.parser.ParsedChapterEntry;
import dev.tr7zw.mango_companion.util.parser.ParsedChapterPage;
import dev.tr7zw.mango_companion.util.parser.ParsedMangaInfo;
import dev.tr7zw.mango_companion.util.parser.StandardLayoutApi;
import dev.tr7zw.mango_companion.util.parser.StandardLayoutParser;
import feign.Feign;
import feign.Param;
import feign.RequestLine;
import feign.Retryer;
import lombok.Getter;
import pl.droidsonroids.jspoon.annotation.Selector;

public class Flamescans extends StandardLayoutParser {

    @Getter
    private Pattern uriPattern = Pattern.compile("https?://flamescans.org/series/.+");
    @Getter
    private Pattern mangaUriUUIDPattern = Pattern.compile("https?://flamescans.org/series/([a-z0-9]+)");
    @Getter
    private Pattern chapterUriUUIDPattern = Pattern.compile("https?://flamescans.org/([a-z-0-9]+)");
    @Getter
    private RateLimiter limiter = new RateLimiter(5, Duration.ofSeconds(1));
    private FlamescansAPI flamescansApi = Feign.builder().decoder(new HTMLPojoDecoder()).addCapability(limiter)
            .client(StreamUtil.getClient()).encoder(new EmptyEncoder()).retryer(new Retryer.Default(1000, 1000, 3))
            .target(FlamescansAPI.class, "https://flamescans.org");
    @Getter
    private StandardLayoutApi api = APIProxyBuilder.getProxy(flamescansApi::getMangaInfo, flamescansApi::getChapterPage);

    private static interface FlamescansAPI {

        @RequestLine("GET /series/{uuid}/")
        MangaInfo getMangaInfo(@Param("uuid") String uuid);

        @RequestLine("GET /{chapterUUID}/")
        ChapterPage getChapterPage(@Param("mangaUUID") String mangaUUID, @Param("chapterUUID") String chapterUUID);

    }

    @Getter
    private static class MangaInfo implements ParsedMangaInfo {
        @Selector(".entry-title")
        String title;
        @Selector("li[data-num]")
        ChapterEntry[] chapters;
    }

    @Getter
    private static class ChapterEntry implements ParsedChapterEntry {
        @Selector(value = ".chapternum")
        String chapter;
        @Selector(value = "[href]", attr = "href")
        String url;
    }

    @Getter
    private static class ChapterPage implements ParsedChapterPage {
        @Selector(value = ".rdminimal img", attr = "src")
        List<String> imageUrls;
    }

}
