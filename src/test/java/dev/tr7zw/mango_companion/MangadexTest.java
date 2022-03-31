package dev.tr7zw.mango_companion;

import dev.tr7zw.mango_companion.parser.Mangadex;
import dev.tr7zw.mango_companion.util.EmptyFileChecker;
import dev.tr7zw.mango_companion.util.parser.Parser;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Validate that Mangadex is working */
public class MangadexTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MangadexTest(String testName) {
        super(testName);
    }

    /** @return the suite of tests being tested */
    public static Test suite() {
        return new TestSuite(MangadexTest.class);
    }

    public void testUrls() {
        Parser parser = new Mangadex();
        assertTrue(
                parser.canParse(
                        "https://mangadex.org/title/a1c7c817-4e59-43b7-9365-09675a149a6f/one-piece"));
        assertTrue(
                parser.canParse(
                        "http://mangadex.org/title/a1c7c817-4e59-43b7-9365-09675a149a6f/one-piece/"));
        assertTrue(
                parser.canParse(
                        "https://mangadex.org/title/a1c7c817-4e59-43b7-9365-09675a149a6f/"));
        assertTrue(
                parser.canParse("http://mangadex.org/title/a1c7c817-4e59-43b7-9365-09675a149a6f"));
        assertFalse(parser.canParse("https://mangadex.org/about"));
        assertFalse(parser.canParse("http://mangadex.org/titles/latest"));
        assertFalse(parser.canParse("https://mangadex.org/title/random"));
        assertFalse(parser.canParse("http://mangadex.org/title/some-thing-ran-dom/"));
    }

    public void testName() throws IOException {
        Parser parser = new Mangadex();
        assertEquals(
                "One Piece",
                parser.getName("https://mangadex.org/title/a1c7c817-4e59-43b7-9365-09675a149a6f"));
    }

    public void testChapters() throws IOException {
        Parser parser = new Mangadex();
        Iterator<Chapter> iterator =
                parser.getChapters(
                        new EmptyFileChecker(),
                        "https://mangadex.org/title/a31a3214-1e1c-4079-b216-e894d20e26cd/d-c-ii-da-capo-ii-imaginary-future"); // pressed random till something came up that probably wont get updates
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        assertNotNull(chapter);
        assertEquals("0", chapter.getChapterId());
        parser.downloadChapter(
                Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
    }
}
