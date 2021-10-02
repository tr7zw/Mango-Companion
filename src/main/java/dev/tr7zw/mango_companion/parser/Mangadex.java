package dev.tr7zw.mango_companion.parser;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.tr7zw.mango_companion.Chapter;
import dev.tr7zw.mango_companion.util.FileChecker;
import dev.tr7zw.mango_companion.util.RateLimiter;
import dev.tr7zw.mango_companion.util.StreamUtil;
import dev.tr7zw.mango_companion.util.ZipCreator;
import dev.tr7zw.mango_companion.util.parser.Parser;
import feign.Feign;
import feign.Param;
import feign.RequestLine;
import feign.Retryer;
import feign.gson.GsonDecoder;
import lombok.extern.java.Log;

@Log
public class Mangadex implements Parser {

    private String lang = "en";
    private int pageSize = 100;
    private Pattern uuidPattern = Pattern.compile("([0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12})");
    private Pattern uriPattern = Pattern.compile("https?://mangadex.org/title/[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}");
    private RateLimiter limiter = new RateLimiter(1, Duration.ofSeconds(1));
    private MangadexAPI api = Feign.builder()
            .decoder(new GsonDecoder())
            .addCapability(limiter)
            .retryer(new Retryer.Default(1000, 1000, 3))
            .client(StreamUtil.getClient())
            .target(MangadexAPI.class, "https://api.mangadex.org");
    
    private String getUUID(String url) {
        Matcher matcher = uuidPattern.matcher(url);
        if(!matcher.find())return null;
        return matcher.group(1);
    }
    
    @Override
    public boolean canParse(String url) {
        return uriPattern.matcher(url).find();
    }

    @Override
    public String getName(String url) throws IOException {
        return api.getMangaInfo(getUUID(url)).data.attributes.title.get(lang);
    }

    @Override
    public Iterator<Chapter> getChapters(FileChecker checker, String url) throws IOException {
        String uuid = getUUID(url);
        return new Iterator<Chapter>() {

            int offset = 0;
            boolean atEnd = false;
            ChapterData[] chapterArray = null;
            int arrayEntry = 0;
            Chapter next = null;

            @Override
            public boolean hasNext() {
                if (next != null)
                    return true;
                if (chapterArray != null) {
                    for (; arrayEntry < chapterArray.length; arrayEntry++) {
                        ChapterData data = chapterArray[arrayEntry];
                        if (!"chapter".equals(data.type))
                            continue;
                        if (!lang.equals(data.attributes.translatedLanguage))
                            continue;
                        Chapter chapter = new Chapter(Mangadex.this, url, data.attributes.chapter == null ? "1" : data.attributes.chapter);
                        if (checker.knownChapter(chapter)) {
                            log.fine("Skipping known chapter " + chapter.getChapterId());
                            continue;
                        }
                        List<String> images = new ArrayList<>();
                        String serverUrl = api.getHost(data.id).baseUrl;
                        for (String page : data.attributes.data) {
                            images.add(serverUrl + "/data/" + data.attributes.hash + "/" + page);
                        }
                        chapter.setParserData(images);
                        next = chapter;
                        arrayEntry++;// next run start with the next entry, not this one
                        return true; // Found a new chapter, set as next
                    }
                    // Went through the array without a new one
                    chapterArray = null;
                    arrayEntry = 0;
                }
                if (atEnd)
                    return false; // reached the last page and completed its data
                // get the next page data end then restart the new chapter search
                ChapterResponse resp = api.getChapterInfo(uuid, offset, pageSize);
                chapterArray = resp.data;
                if (offset + pageSize >= resp.total) {
                    atEnd = true;
                } else {
                    offset += pageSize;
                }
                return hasNext(); // now that chapterArray is filled again, ask it for new data
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
        log.fine("Downloading " + target.getParentFile().getName() + " " + target.getName());
        @SuppressWarnings("unchecked")
        List<String> data = (List<String>) chapter.getParserData();
        int page = 1;
        try (ZipCreator zip = new ZipCreator(target)) {
            for (String url : data) {
                String fileName = page + url.substring(url.lastIndexOf("."));
                zip.addFile(fileName, StreamUtil.getStream(limiter, url));
                page++;
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public List<String> getImages(Chapter chapter) throws IOException {
        List<String> data = (List<String>) chapter.getParserData();
        return data;
    }

    private static interface MangadexAPI {
        
        @RequestLine("GET /at-home/server/{uuid}")
        ServerLocation getHost(@Param("uuid") String uuid);
        
        @RequestLine("GET /manga/{uuid}")
        MangaResponse getMangaInfo(@Param("uuid") String uuid);
        
        @RequestLine("GET /chapter?manga={uuid}&offset={offset}&limit={limit}")
        ChapterResponse getChapterInfo(@Param("uuid") String uuid, @Param("offset") int offset, @Param("limit") int limit);
        
    }
    
    private static class ServerLocation {
        public String baseUrl;
    }
    
    private static class MangaResponse {
        //public Result result;
        public MangaData data;
    }
    
    private static class MangaData {
        //public String id;
        //public String type;
        public Attributes attributes;
    }
    
    private static class Attributes {
        public Map<String, String> title;
    }
    
    //private static enum Result{
    //    ok, error
    //}
    
    private static class ChapterResponse {
        //public Result result;
        //public String response;
        public ChapterData[] data;
        //public int limit;
        //public int offset;
        public int total;
    }
    
    private static class ChapterData {
        public String id;
        public String type;
        public ChapterAttributes attributes;
    }
    
    private static class ChapterAttributes {
        //public String volume;
        public String chapter;
        //public String title;
        public String translatedLanguage;
        public String[] data;
        public String hash;
    }
    
}
