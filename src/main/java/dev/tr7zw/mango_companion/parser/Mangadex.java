package dev.tr7zw.mango_companion.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.tr7zw.mango_companion.crawler.MangadexCrawler;
import dev.tr7zw.mango_companion.util.FileChecker;
import dev.tr7zw.mango_companion.util.ZipCreator;
import lombok.Data;
import lombok.extern.java.Log;

@Log
public class Mangadex implements Parser {

    private MangadexCrawler crawler = new MangadexCrawler();
    private String lang = "en";
    private int pageSize = 100;

    @Override
    public boolean canParse(String url) {
        return url.startsWith("https://mangadex.org/title/");
    }

    @Override
    public String getName(String url) throws IOException {
        String uri = url.replace("https://mangadex.org/title/", "");
        if(uri.contains("/")) {
            uri = uri.substring(0, uri.indexOf('/'));
        }
        JsonObject manga = crawler.getJson("https://api.mangadex.org/manga/" + uri);
        return manga.getAsJsonObject("data").getAsJsonObject("attributes").getAsJsonObject("title").get(lang)
                .getAsString();
    }

    @Override
    public Iterator<Chapter> getChapters(FileChecker checker, String url) throws IOException {
        String tmp = url.replace("https://mangadex.org/title/", "");
        if(tmp.contains("/")) {
            tmp = tmp.substring(0, tmp.indexOf('/'));
        }
        String uri = tmp;
        return new Iterator<Chapter>() {

            int offset = 0;
            boolean atEnd = false;
            JsonArray chapterArray = null;
            int arrayEntry = 0;
            Chapter next = null;

            @Override
            public boolean hasNext() {
                try {
                    if (next != null)
                        return true;
                    if (chapterArray != null) {
                        for (; arrayEntry < chapterArray.size(); arrayEntry++) {
                            JsonObject el = chapterArray.get(arrayEntry).getAsJsonObject();
                            JsonObject data = el.getAsJsonObject();
                            if (!"chapter".equals(data.get("type").getAsString()))
                                continue;
                            JsonObject attributes = data.getAsJsonObject("attributes");
                            if (!lang.equals(attributes.get("translatedLanguage").getAsString()))
                                continue;
                            Chapter chapter = new Chapter(Mangadex.this, url, attributes.get("chapter").isJsonNull() ? "1" : attributes.get("chapter").getAsString());
                            if (checker.knownChapter(chapter)) {
                                log.fine("Skipping known chapter " + chapter.getChapterId());
                                continue;
                            }
                            ChapterData chapterData = new ChapterData();

                            String serverUrl = crawler
                                    .getJson("https://api.mangadex.org/at-home/server/" + data.get("id").getAsString())
                                    .get("baseUrl").getAsString();
                            String chapterHash = attributes.get("hash").getAsString();
                            for (JsonElement page : attributes.getAsJsonArray("data")) {
                                String pageUrl = serverUrl + "/data/" + chapterHash + "/" + page.getAsString();
                                chapterData.pages.add(pageUrl);
                            }
                            chapter.setParserData(chapterData);
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
                    JsonObject base = crawler.getJson("https://api.mangadex.org/chapter?manga=" + uri + "&offset="
                            + offset + "&limit=" + pageSize);
                    chapterArray = base.get("data").getAsJsonArray();
                    if (offset + pageSize >= base.get("total").getAsInt()) {
                        atEnd = true;
                    } else {
                        offset += pageSize;
                    }
                    return hasNext(); // now that chapterArray is filled again, ask it for new data
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Chapter next() {
                Chapter tmp = next;
                next = null;
                return tmp;
            }
        };

    }

    @Data
    private class ChapterData {
        private List<String> pages = new ArrayList<>();
    }

    @Override
    public void downloadChapter(File target, Chapter chapter) throws IOException {
        if (chapter.getParser().getClass() != this.getClass())
            throw new RuntimeException("Incompatible Chapter to Parser");
        log.fine("Downloading " + target.getParentFile().getName() + " " + target.getName());
        ChapterData data = (ChapterData) chapter.getParserData();
        int page = 1;
        try (ZipCreator zip = new ZipCreator(target)) {
            for (String url : data.pages) {
                String fileName = page + url.substring(url.lastIndexOf("."));
                zip.addFile(fileName, crawler.getStream(url));
                page++;
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
