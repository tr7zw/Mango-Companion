package dev.tr7zw.mango_companion.parser;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.tr7zw.mango_companion.Chapter;
import dev.tr7zw.mango_companion.util.ChapterParser;
import dev.tr7zw.mango_companion.util.FileChecker;
import dev.tr7zw.mango_companion.util.HTMLPojoDecoder;
import dev.tr7zw.mango_companion.util.RateLimiter;
import dev.tr7zw.mango_companion.util.StreamUtil;
import dev.tr7zw.mango_companion.util.ZipCreator;
import feign.Feign;
import feign.Param;
import feign.RequestLine;
import feign.Retryer;
import lombok.extern.java.Log;
import pl.droidsonroids.jspoon.annotation.Selector;

@Log
public class AsuraScans implements Parser {

    private Pattern uriPattern = Pattern.compile("https?://www.asurascans.com/comics/.+");
    private Pattern mangaUriUUIDPattern = Pattern.compile("https?://www.asurascans.com/comics/([a-z-0-9]+)");
    private Pattern chapterUriUUIDPattern = Pattern.compile("https?://www.asurascans.com/([a-z-0-9]+)");
    private RateLimiter limiter = new RateLimiter(5, Duration.ofSeconds(1));
    private AsuraScansAPI api = Feign.builder()
            .decoder(new HTMLPojoDecoder())
            .addCapability(limiter)
            .client(StreamUtil.getClient())
            .retryer(new Retryer.Default(1000, 1000, 3))
            .target(AsuraScansAPI.class, "https://www.asurascans.com");
    
    
    @Override
    public boolean canParse(String url) {
        return uriPattern.matcher(url).find();
    }

    @Override
    public Iterator<Chapter> getChapters(FileChecker checker, String url) throws IOException {
        List<ChapterEntry> chapters = api.getMangaInfo(getUUID(url)).chapters;
        return new Iterator<Chapter>() {

            int id = chapters.size() - 1;
            Chapter next = null;

            @Override
            public boolean hasNext() {
                while (id >= 0) { // we start from the bottom and go to the top
                    ChapterEntry element = chapters.get(id--);
                    String id = ChapterParser.getChapterId(element.chapter);
                    if (id == null || id.isEmpty())
                        continue;
                    next = new Chapter(AsuraScans.this, element.url, id);
                    if (checker.knownChapter(next)) {
                        next = null;
                    } else {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Chapter next() {
                Chapter tmp = next;
                next = null;
                return tmp;
            }
        };
    }

    @Override
    public void downloadChapter(File target, Chapter chapter) throws IOException {
        if (chapter.getParser().getClass() != this.getClass())
            throw new RuntimeException("Incompatible Chapter to Parser");
        List<String> urls = api.getChapterPage(getChapterUUID(chapter.getUrl())).imageUrls;
        log.fine("Downloading " + target.getParentFile().getName() + " " + target.getName());
        int page = 1;
        try (ZipCreator zip = new ZipCreator(target)) {
            for (String url : urls) {
                String fileName = page + url.substring(url.lastIndexOf("."));
                zip.addFile(fileName, StreamUtil.getStream(limiter, url));
                page++;
            }
        } catch (Exception e) {
            throw new IOException("Error while downloading Chapter " + chapter.getChapterId(), e);
        }
    }

    @Override
    public String getName(String url) throws IOException {
        return api.getMangaInfo(getUUID(url)).title;
    }
    
    private String getUUID(String url) {
        Matcher matcher = mangaUriUUIDPattern.matcher(url);
        matcher.find();
        return matcher.group(1);
    }
    
    private String getChapterUUID(String url) {
        Matcher matcher = chapterUriUUIDPattern.matcher(url);
        matcher.find();
        return matcher.group(1);
    }
    
    private static interface AsuraScansAPI {
        
        @RequestLine("GET /comics/{uuid}/")
        MangaInfo getMangaInfo(@Param("uuid") String uuid);
        @RequestLine("GET /{uuid}/")
        ChapterPage getChapterPage(@Param("uuid") String uuid);
        
    }
    
    private static class MangaInfo{
        @Selector("h1.entry-title")
        public String title;
        @Selector(".chbox")
        public List<ChapterEntry> chapters;
    }
    
    private static class ChapterEntry {
        @Selector(value = "[href]")
        public String chapter;
        @Selector(value = "[href]", attr = "href")
        public String url;
    }
    
    private static class ChapterPage {
        @Selector(value = ".size-full[loading]", attr = "src")
        public List<String> imageUrls;
    }

}
