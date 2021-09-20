package dev.tr7zw.mango_companion.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dev.tr7zw.mango_companion.Chapter;
import dev.tr7zw.mango_companion.crawler.ManganatoCrawler;
import dev.tr7zw.mango_companion.util.ChapterParser;
import dev.tr7zw.mango_companion.util.FileChecker;
import dev.tr7zw.mango_companion.util.ZipCreator;
import dev.tr7zw.mango_companion.util.parser.Parser;
import lombok.extern.java.Log;

@Log
public class Manganato implements Parser {

    private ManganatoCrawler crawler = new ManganatoCrawler();
    
    @Override
    public boolean canParse(String url) {
        return url.startsWith("https://readmanganato.com/manga-") || url.startsWith("https://manganato.com/manga-");
    }

    @Override
    public Iterator<Chapter> getChapters(FileChecker checker, String url) throws IOException {
        Document doc =  crawler.getDocument(url);
        Elements chapters = doc.getElementsByClass("chapter-name");
        return new Iterator<Chapter>() {

            int id = chapters.size()-1;
            Chapter next = null;
            
            @Override
            public boolean hasNext() {
                while(id >= 0) { // we start from the bottom and go to the top
                    Element element = chapters.get(id--);
                    next = new Chapter(Manganato.this, element.attr("href"), ChapterParser.getChapterId(element.html()));
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
        for(Element entry : doc.getElementsByAttributeValueContaining("title", "Chapter " + chapter.getChapterId())) {
            if(entry.hasAttr("src")) {
                urls.add(entry.attr("src"));
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
            throw new IOException("Error while downloading Chapter " + chapter.getChapterId(), e);
        }
    }

    @Override
    public String getName(String url) throws IOException {
        Document doc =  crawler.getDocument(url);
        return doc.getElementsByClass("story-info-right").get(0).getAllElements().get(1).html();
    }

}
