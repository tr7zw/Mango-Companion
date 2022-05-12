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
        assertTrue(parser.canParse("https://www.asurascans.com/comics/i-grow-stronger-by-eating/"));
        assertTrue(parser.canParse("http://www.asurascans.com/comics/i-grow-stronger-by-eating"));
        assertFalse(parser.canParse("http://www.asurascans.com/comics/"));
        assertFalse(parser.canParse("https://www.asurascans.com/comics"));
        assertFalse(parser.canParse("http://www.asurascans.com/random"));
    }
    
    public void testName() throws IOException {
        Parser parser = new AsuraScans();
        assertEquals("I Grow Stronger By Eating!", parser.getName("https://www.asurascans.com/comics/i-grow-stronger-by-eating/"));
    }
    
    public void testChapters() throws IOException {
        Parser parser = new AsuraScans();
        Iterator<Chapter> iterator = parser.getChapters(new EmptyFileChecker(), "https://www.asurascans.com/comics/i-grow-stronger-by-eating/");
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        assertNotNull(chapter);
        assertEquals("1", chapter.getChapterId());
        int pages = parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
    }
    
    public void testSpecial() throws IOException {
        Parser parser = new AsuraScans();
        Iterator<Chapter> iterator = parser.getChapters(new EmptyFileChecker(), "https://www.asurascans.com/comics/367-reincarnation-of-the-suicidal-battle-god/");
        while(iterator.hasNext()) {
            Chapter chapter = iterator.next();
            if("47".equals(chapter.getChapterId())) {
                int pages = parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
                assertTrue("Expected more than 5 pages. Pages: " + pages,pages > 5);
            }
        }
    }

}
