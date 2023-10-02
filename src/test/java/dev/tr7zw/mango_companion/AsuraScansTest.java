package dev.tr7zw.mango_companion;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import dev.tr7zw.mango_companion.parser.AsuraScans;
import dev.tr7zw.mango_companion.util.EmptyFileChecker;
import dev.tr7zw.mango_companion.util.parser.Parser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Validate that AsuraScans is working
 *
 */
public class AsuraScansTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AsuraScansTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AsuraScansTest.class);
    }

    public void testUrls() {
        Parser parser = new AsuraScans();
        assertTrue(parser.canParse("https://asuratoon.com/manga/3787011421-damn-reincarnation/"));
        assertTrue(parser.canParse("https://asuratoon.com/manga/3787011421-i-grow-stronger-by-eating/"));
        assertFalse(parser.canParse("http://asuratoon.com/comics/"));
        assertFalse(parser.canParse("https://asuratoon.com/manga"));
        assertFalse(parser.canParse("http://asuratoon.com/random"));
    }
    
    public void testName() throws IOException {
        Parser parser = new AsuraScans();
        assertEquals("I Grow Stronger By Eating!", parser.getName("https://asuratoon.com/manga/3787011421-i-grow-stronger-by-eating/"));
        assertEquals("Damn Reincarnation", parser.getName("https://asuratoon.com/manga/3787011421-damn-reincarnation/"));
    }
    
    public void testChapters() throws IOException {
        Parser parser = new AsuraScans();
        Iterator<Chapter> iterator = parser.getChapters(new EmptyFileChecker(), "https://asuratoon.com/manga/3787011421-i-grow-stronger-by-eating/");
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        assertNotNull(chapter);
        assertEquals("1", chapter.getChapterId());
        parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
    }
    
    public void testChaptersManga() throws IOException {
        Parser parser = new AsuraScans();
        Iterator<Chapter> iterator = parser.getChapters(new EmptyFileChecker(), "https://asuratoon.com/manga/3787011421-damn-reincarnation/");
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        assertNotNull(chapter);
        assertEquals("1", chapter.getChapterId());
        parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
    }
    
    public void testSpecial() throws IOException {
        Parser parser = new AsuraScans();
        Iterator<Chapter> iterator = parser.getChapters(new EmptyFileChecker(), "https://asuratoon.com/manga/3787011421-reincarnation-of-the-suicidal-battle-god/");
        while(iterator.hasNext()) {
            Chapter chapter = iterator.next();
            if("47".equals(chapter.getChapterId())) {
                int pages = parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
                assertTrue("Expected more than 5 pages. Pages: " + pages,pages > 5);
            }
        }
    }
    
    public void testSpecial2() throws IOException {
        Parser parser = new AsuraScans();
        Iterator<Chapter> iterator = parser.getChapters(new EmptyFileChecker(), "https://asuratoon.com/manga/3787011421-the-game-that-i-came-from/");
        while(iterator.hasNext()) {
            Chapter chapter = iterator.next();
            if("92".equals(chapter.getChapterId())) {
                int pages = parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
                assertTrue("Expected more than 5 pages. Pages: " + pages,pages > 5);
            }
        }
    }


}
