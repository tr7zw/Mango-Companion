package dev.tr7zw.mango_companion;

import dev.tr7zw.mango_companion.parser.ReadManganato;
import dev.tr7zw.mango_companion.util.EmptyFileChecker;
import dev.tr7zw.mango_companion.util.parser.Parser;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Validate that Manganato is working */
public class ReadManganatoTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ReadManganatoTest(String testName) {
        super(testName);
    }

    /** @return the suite of tests being tested */
    public static Test suite() {
        return new TestSuite(ReadManganatoTest.class);
    }

    public void testUrls() {
        Parser parser = new ReadManganato();
        assertTrue(parser.canParse("https://readmanganato.com/manga-dr980474"));
        assertTrue(parser.canParse("https://readmanganato.com/manga-dr980474/"));
        assertFalse(parser.canParse("https://readmanganato.com/"));
        assertFalse(parser.canParse("https://manganato.com/genre-all"));
        assertFalse(parser.canParse("https://manganato.com/genre-all?type=newest"));
    }

    public void testName() throws IOException {
        Parser parser = new ReadManganato();
        assertEquals("Solo Leveling", parser.getName("https://readmanganato.com/manga-dr980474/"));
    }

    public void testChapters() throws IOException {
        Parser parser = new ReadManganato();
        Iterator<Chapter> iterator =
                parser.getChapters(
                        new EmptyFileChecker(), "https://readmanganato.com/manga-dr980474/");
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        assertNotNull(chapter);
        assertEquals("0", chapter.getChapterId());
        parser.downloadChapter(
                Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
    }

    public void testChapters2() throws IOException {
        Parser parser = new ReadManganato();
        Iterator<Chapter> iterator =
                parser.getChapters(
                        new EmptyFileChecker(), "https://readmanganato.com/manga-jn986622");
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        while (!chapter.getChapterId().equals("12.1")) {
            if (iterator.hasNext()) {
                chapter = iterator.next();
            } else {
                fail();
            }
        }
        assertNotNull(chapter);
        assertEquals("12.1", chapter.getChapterId());
        parser.downloadChapter(
                Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
    }
}
