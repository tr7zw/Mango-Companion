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

public class AsuraScans extends StandardLayoutParser {

    @Getter
    private Pattern uriPattern = Pattern.compile("https?://asuratoon.com/(?:comics|manga)/.+");
    @Getter
    private Pattern mangaUriUUIDPattern = Pattern.compile("https?://asuratoon.com/(?:comics|manga)/([a-z-0-9]+)");
    @Getter
    private Pattern chapterUriUUIDPattern = Pattern.compile("https?://asuratoon.com/([a-z-0-9]+)");
    @Getter
    private RateLimiter limiter = new RateLimiter(5, Duration.ofSeconds(1));
    private AsuraScansAPI asuraApi = Feign.builder().decoder(new HTMLPojoDecoder())
            .client(StreamUtil.getClient(limiter)).encoder(new EmptyEncoder()).retryer(new Retryer.Default(1000, 1000, 3))
            .target(AsuraScansAPI.class, "https://asuratoon.com");
    @Getter
    private StandardLayoutApi api = APIProxyBuilder.getProxy(asuraApi::getMangaInfo, asuraApi::getChapterPage);

    private static interface AsuraScansAPI {

        @RequestLine("GET /comics/{uuid}/")
        MangaInfo getMangaInfo(@Param("uuid") String uuid);

        @RequestLine("GET /{chapterUUID}/")
        ChapterPage getChapterPage(@Param("mangaUUID") String mangaUUID, @Param("chapterUUID") String chapterUUID);

    }

    @Override
    public String remapUrl(String url) {
        return super.remapUrl(url).replace("www.asurascans.com", "asura.gg");
    }

    @Getter
    private static class MangaInfo implements ParsedMangaInfo {
        @Selector("h1.entry-title")
        String title;
        @Selector(".chbox")
        ChapterEntry[] chapters;
    }

    @Getter
    private static class ChapterEntry implements ParsedChapterEntry {
        @Selector(value = "[href]")
        String chapter;
        @Selector(value = "[href]", attr = "href")
        String url;
    }

    @Getter
    private static class ChapterPage implements ParsedChapterPage {
        @Selector(value = ".alignnone , .aligncenter", attr = "src")
        List<String> imageUrls;
    }

}
