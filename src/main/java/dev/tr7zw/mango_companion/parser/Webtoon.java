package dev.tr7zw.mango_companion.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.tr7zw.mango_companion.Chapter;
import dev.tr7zw.mango_companion.util.ChapterParser;
import dev.tr7zw.mango_companion.util.FileChecker;
import dev.tr7zw.mango_companion.util.SimulatedBrowser;
import dev.tr7zw.mango_companion.util.ZipCreator;
import dev.tr7zw.mango_companion.util.parser.Parser;
import lombok.Getter;

public class Webtoon implements Parser {

    @Getter
    private Pattern uriPattern = Pattern.compile("https?://www.webtoon.xyz/read/.+");
    @Getter
    private Pattern mangaUriUUIDPattern = Pattern.compile("https?://www.webtoon.xyz/read/([a-z-0-9]+)");
    @Getter
    private Pattern chapterUriUUIDPattern = Pattern.compile("https?://www.webtoon.xyz/read/([a-z-0-9]+)/([a-z-0-9]+)");
    private SimulatedBrowser browser = new SimulatedBrowser();

    @Override
    public boolean canParse(String url) {
        return getUriPattern().matcher(url).find();
    }

    @Override
    public Iterator<Chapter> getChapters(FileChecker checker, String url) throws IOException {
        String content = browser.getPageContent(url);
        Pattern chapterPattern = Pattern.compile(url + "(chapter-[a-z-0-9]+)/");
        Matcher matcher = chapterPattern.matcher(content);
        List<Chapter> chapterList = new ArrayList<>();
        while(matcher.find()) {
            Chapter chapter = new Chapter(this, matcher.group(), ChapterParser.getChapterId(matcher.group(1)));
            if(!checker.knownChapter(chapter)) {
                chapterList.add(chapter);
            }
        }
        chapterList.sort(null);
        return chapterList.iterator();
    }

    @Override
    public List<String> getImages(Chapter chapter) throws IOException {
        // not required for this Parser
        return null;
    }

    @Override
    public int downloadChapter(File target, Chapter chapter) throws IOException {
        AtomicInteger counter = new AtomicInteger();
        try (ZipCreator zip = new ZipCreator(target)) {
            browser.downloadImages(chapter.getUrl(), counter, zip);
            if(counter.get() == 0) {// Nothing got downloaded
                throw new IOException("Unable to locate images in Chapter " + chapter.getChapterId());
            }
        } catch (Exception e) {
            throw new IOException("Error while downloading Chapter " + chapter.getChapterId(), e);
        }
        return counter.get();
    }

    @Override
    public String getName(String url) throws IOException {
        String title = browser.getPageTitle(url);
        title = title.substring(0, title.lastIndexOf(':')).replace("Manhwa", "").trim();
        return title;
    }

}