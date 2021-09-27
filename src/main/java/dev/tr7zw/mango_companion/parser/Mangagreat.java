package dev.tr7zw.mango_companion.parser;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
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
import feign.Body;
import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Retryer;
import lombok.Getter;
import pl.droidsonroids.jspoon.annotation.Selector;

public class Mangagreat extends StandardLayoutParser {

    @Getter
    private Pattern uriPattern = Pattern.compile("https?://mangagreat.com/manga/.+");
    @Getter
    private Pattern mangaUriUUIDPattern = Pattern.compile("https?://mangagreat.com/manga/([a-z-0-9]+)");
    @Getter
    private Pattern chapterUriUUIDPattern = Pattern.compile("https?://mangagreat.com/manga/[a-z-0-9]+/([a-z-0-9]+)");
    private Pattern mangaShortUUIDPattern = Pattern.compile("https?://mangagreat.com/\\?p=([0-9]+)");
    @Getter
    private RateLimiter limiter = new RateLimiter(5, Duration.ofSeconds(1));
    private MangagreatAPI baseApi = Feign.builder().decoder(new HTMLPojoDecoder()).addCapability(limiter)
            .client(StreamUtil.getClient()).encoder(new EmptyEncoder()).retryer(new Retryer.Default(1000, 1000, 3))
            .target(MangagreatAPI.class, "https://mangagreat.com");
    @Getter
    private StandardLayoutApi api = APIProxyBuilder.getProxy(baseApi::getMangaInfo, baseApi::getChapterPage);
    
    @Override
    public InputStream getStream(RateLimiter limiter, String url) throws IOException {
        return StreamUtil.getStreamNoReferer(limiter, url);
    }

    private static interface MangagreatAPI {

        @RequestLine("GET /manga/{uuid}/")
        MangaInfo getMangaInfo(@Param("uuid") String uuid);

        @RequestLine("GET /manga/{mangaUUID}/{chapterUUID}")
        ChapterPage getChapterPage(@Param("mangaUUID") String mangaUUID, @Param("chapterUUID") String chapterUUID);
        
        @RequestLine("POST /wp-admin/admin-ajax.php")
        @Body("action=manga_get_chapters&manga={shortid}")
        @Headers({"Content-Type: application/x-www-form-urlencoded; charset=UTF-8"})
        MangaInfo getChapterList(@Param("shortid") String shortid);
        
    }

    @Override
    public ParsedChapterEntry[] getChapters(String url) {
        MangaInfo info = baseApi.getMangaInfo(getMangaUUID(url));
        if(info.chaptersList != null)return info.getChapters();
        Matcher matcher = mangaShortUUIDPattern.matcher(info.getShortLink());
        matcher.find();
        String mangaShortId = matcher.group(1);
        return baseApi.getChapterList(mangaShortId).getChapters();
    }

    @Getter
    private static class MangaInfo implements ParsedMangaInfo {
        @Selector(".post-title h1")
        String title;
        @Selector(value = "head > link[rel=shortlink]", attr = "href")
        String shortLink;
        @Selector("li.wp-manga-chapter")
        List<ChapterEntry> chaptersList;
        
        @Override
        public ParsedChapterEntry[] getChapters() {
            return chaptersList.toArray(new ParsedChapterEntry[0]);
        }
        
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
        @Selector(value = ".blocks-gallery-item img", attr = "src")
        List<String> imageUrls;
    }

}
