package dev.tr7zw.mango_companion;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import dev.tr7zw.mango_companion.parser.Mangagreat;
import dev.tr7zw.mango_companion.util.EmptyFileChecker;
import dev.tr7zw.mango_companion.util.parser.Parser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Validate that Mangagreat is working
 *
 */
public class MangagreatTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MangagreatTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(MangagreatTest.class);
    }

    public void testUrls() {
        Parser parser = new Mangagreat();
        assertTrue(parser.canParse("https://mangagreat.com/manga/one-sword-reigns-supreme/"));
        assertTrue(parser.canParse("http://mangagreat.com/manga/one-sword-reigns-supreme"));
        assertFalse(parser.canParse("https://mangagreat.com/manga"));
        assertFalse(parser.canParse("https://mangagreat.com/"));
        assertFalse(parser.canParse("https://mangagreat.com/manga-genre/comic/"));
    }
    
    public void testName() throws IOException {
        Parser parser = new Mangagreat();
        assertEquals("One Sword Reigns Supreme", parser.getName("https://mangagreat.com/manga/one-sword-reigns-supreme/"));
    }
    
    public void testChapters() throws IOException {
        Parser parser = new Mangagreat();
        Iterator<Chapter> iterator = parser.getChapters(new EmptyFileChecker(), "https://mangagreat.com/manga/one-sword-reigns-supreme/");
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        assertNotNull(chapter);
        assertEquals("0", chapter.getChapterId());
        parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
    }

}
