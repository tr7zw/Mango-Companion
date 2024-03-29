package dev.tr7zw.mango_companion.util.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.tr7zw.mango_companion.Chapter;
import dev.tr7zw.mango_companion.util.ChapterParser;
import dev.tr7zw.mango_companion.util.FileChecker;
import dev.tr7zw.mango_companion.util.RateLimiter;
import dev.tr7zw.mango_companion.util.StreamUtil;
import dev.tr7zw.mango_companion.util.ZipCreator;
import lombok.extern.java.Log;

@Log
public abstract class StandardLayoutParser implements Parser {

    public abstract Pattern getUriPattern();
    public abstract Pattern getMangaUriUUIDPattern();
    public abstract Pattern getChapterUriUUIDPattern();
    public abstract RateLimiter getLimiter();
    public abstract StandardLayoutApi getApi();
    
    public ParsedChapterEntry[] getChapters(String url) {
        return getApi().getMangaInfo(getMangaUUID(remapUrl(url))).getChapters();
    }

    public List<String> getChapterUrls(String url) {
        return getApi().getChapterPage(getMangaUUID(remapUrl(url)), getChapterUUID(remapUrl(url))).getImageUrls();
    }

    public String getName(String url) throws IOException {
        return getApi().getMangaInfo(getMangaUUID(remapUrl(url))).getTitle();
    }
    
    @Override
    public boolean canParse(String url) {
        return getUriPattern().matcher(remapUrl(url)).find();
    }
    
    public String remapUrl(String url) {
        return url;
    }
    
    @Override
    public Iterator<Chapter> getChapters(FileChecker checker, String url) throws IOException {
        ParsedChapterEntry[] chapters = getChapters(remapUrl(url));
        return new Iterator<Chapter>() {

            int id = chapters.length - 1;
            Chapter next = null;

            @Override
            public boolean hasNext() {
                while (id >= 0) { // we start from the bottom and go to the top
                    ParsedChapterEntry element = chapters[id--];
                    String id = ChapterParser.getChapterId(element.getChapter());
                    if (id == null || id.isEmpty())
                        continue;
                    next = new Chapter(StandardLayoutParser.this, element.getUrl(), id);
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
    public int downloadChapter(File target, Chapter chapter) throws IOException {
        if (chapter.getParser().getClass() != this.getClass())
            throw new RuntimeException("Incompatible Chapter to Parser");
        List<String> urls = getImages(chapter);
        if(urls == null) {
            throw new IOException("Error while downloading Chapter " + chapter.getChapterId() + ": No Image urls found!");
        }
        log.fine("Downloading " + target.getParentFile().getName() + " " + target.getName());
        int page = 1;
        try (ZipCreator zip = new ZipCreator(target)) {
            for (String url : urls) {
                if(url == null || url.trim().isEmpty())continue;
                if(url.startsWith("data:"))continue;
                String fileName = page + url.trim().substring(url.trim().lastIndexOf("."));
                if(fileName.indexOf('?') != -1) {
                    fileName = fileName.substring(0, fileName.indexOf('?'));
                }
                zip.addFile(fileName, getStream(getLimiter(), url));
                page++;
            }
            if(page == 1) {// Nothing got downloaded
                throw new IOException("Unable to locate images in Chapter " + chapter.getChapterId());
            }
        } catch (Exception e) {
            throw new IOException("Error while downloading Chapter " + chapter.getChapterId(), e);
        }
        return page;
    }
    
    @Override
    public List<String> getImages(Chapter chapter) throws IOException {
        return getChapterUrls(chapter.getUrl());
    }
    
    public InputStream getStream(RateLimiter limiter, String url) throws IOException {
        return StreamUtil.getStream(limiter, url);
    }
    
    protected String getMangaUUID(String url) {
        Matcher matcher = getMangaUriUUIDPattern().matcher(remapUrl(url));
        if(!matcher.find())return null;
        return matcher.group(1);
    }
    
    protected String getChapterUUID(String url) {
        Matcher matcher = getChapterUriUUIDPattern().matcher(remapUrl(url));
        if(!matcher.find())return null;
        return matcher.group(1);
    }

}
