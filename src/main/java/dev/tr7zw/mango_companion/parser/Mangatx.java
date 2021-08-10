package dev.tr7zw.mango_companion.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dev.tr7zw.mango_companion.crawler.MangatxCrawler;
import dev.tr7zw.mango_companion.util.FileChecker;
import dev.tr7zw.mango_companion.util.ZipCreator;
import lombok.extern.java.Log;

@Log
public class Mangatx implements Parser{

    private MangatxCrawler crawler = new MangatxCrawler();
    
    @Override
    public boolean canParse(String url) {
        return url.startsWith("https://mangatx.com/manga/");
    }

    @Override
    public Iterator<Chapter> getChapters(FileChecker checker, String url) throws IOException {
        Document doc =  crawler.getDocument(url);
        Elements chapters = doc.getElementsByAttribute("href");
        return new Iterator<Chapter>() {

            int id = chapters.size()-1;
            Chapter next = null;
            
            @Override
            public boolean hasNext() {
                while(id >= 0) { // we start from the bottom and go to the top
                    Element element = chapters.get(id--);
                    if(!element.attr("href").startsWith(url + "chapter-"))continue;
                    String id = element.text().toLowerCase().replace("chapter ", "").split(" ")[0].trim();
                    if(id.isEmpty())continue;
                    next = new Chapter(Mangatx.this, element.attr("href"), id);
                    if(checker.knownChapter(next)) {
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
        Document doc =  crawler.getDocument(chapter.getUrl());
        List<String> urls = new ArrayList<>();
        for(Element entry : doc.getElementsByClass("wp-manga-chapter-img")) {
            if(entry.hasAttr("data-src")) {
                urls.add(entry.attr("data-src").trim());
            }
        }
        log.fine("Downloading " + target.getParentFile().getName() + " " + target.getName());
        int page = 1;
        try (ZipCreator zip = new ZipCreator(target)) {
            for (String url : urls) {
                String fileName = page + url.substring(url.lastIndexOf("."));
                zip.addFile(fileName, crawler.getStream(url));
                page++;
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public String getName(String url) throws IOException {
        return crawler.getDocument(url).getElementsByClass("post-title").get(0).text();
    }

}
