package dev.tr7zw.mango_companion;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import dev.tr7zw.mango_companion.parser.Manganato;
import dev.tr7zw.mango_companion.util.parser.Parser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Validate that Manganato is working
 *
 */
public class ManganatoTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ManganatoTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ManganatoTest.class);
    }

    public void testUrls() {
        Parser parser = new Manganato();
        assertTrue(parser.canParse("https://readmanganato.com/manga-dr980474"));
        assertTrue(parser.canParse("https://readmanganato.com/manga-dr980474/"));
        assertFalse(parser.canParse("https://readmanganato.com/"));
        assertFalse(parser.canParse("https://manganato.com/genre-all"));
        assertFalse(parser.canParse("https://manganato.com/genre-all?type=newest"));
    }
    
    public void testName() throws IOException {
        Parser parser = new Manganato();
        assertEquals("Solo Leveling", parser.getName("https://readmanganato.com/manga-dr980474/"));
    }
    
    public void testChapters() throws IOException {
        Parser parser = new Manganato();
        Iterator<Chapter> iterator = parser.getChapters(new EmptyFileChecker(), "https://readmanganato.com/manga-dr980474/");
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        assertNotNull(chapter);
        assertEquals("0", chapter.getChapterId());
        parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
    }

}
