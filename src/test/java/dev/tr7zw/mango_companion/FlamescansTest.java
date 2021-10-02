package dev.tr7zw.mango_companion;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import dev.tr7zw.mango_companion.parser.Flamescans;
import dev.tr7zw.mango_companion.util.EmptyFileChecker;
import dev.tr7zw.mango_companion.util.parser.Parser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Validate that Flamescans is working
 *
 */
public class FlamescansTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public FlamescansTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(FlamescansTest.class);
    }

    public void testUrls() {
        Parser parser = new Flamescans();
        assertTrue(parser.canParse("https://flamescans.org/series/player-starting-from-today/"));
        assertTrue(parser.canParse("http://flamescans.org/series/player-starting-from-today"));
        assertFalse(parser.canParse("https://flamescans.org/"));
        assertFalse(parser.canParse("https://flamescans.org/series/"));
    }
    
    public void testName() throws IOException {
        Parser parser = new Flamescans();
        assertEquals("Player Starting from Today", parser.getName("https://flamescans.org/series/player-starting-from-today/"));
    }
    
    public void testChapters() throws IOException {
        Parser parser = new Flamescans();
        Iterator<Chapter> iterator = parser.getChapters(new EmptyFileChecker(), "https://flamescans.org/series/player-starting-from-today/"); // Different manga because the other one has broken images
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        assertNotNull(chapter);
        assertEquals("1", chapter.getChapterId());
        parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
    }

}
