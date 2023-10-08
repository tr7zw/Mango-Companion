package dev.tr7zw.mango_companion;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import dev.tr7zw.mango_companion.parser.Webtoon;
import dev.tr7zw.mango_companion.util.EmptyFileChecker;
import dev.tr7zw.mango_companion.util.parser.Parser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Validate that Mangagreat is working
 *
 */
public class WebtoonTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public WebtoonTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(WebtoonTest.class);
    }

    public void testUrls() {
        Parser parser = new Webtoon();
        assertTrue(parser.canParse("https://www.webtoon.xyz/read/overpowered-sword/"));
        assertFalse(parser.canParse("https://www.webtoon.xyz"));
    }

    public void testName() throws IOException {
        Parser parser = new Webtoon();
        assertEquals("Overpowered Sword", parser.getName("https://www.webtoon.xyz/read/overpowered-sword/"));
    }

    public void testChapters() throws IOException {
        Parser parser = new Webtoon();
        Iterator<Chapter> iterator = parser.getChapters(new EmptyFileChecker(),
                "https://www.webtoon.xyz/read/overpowered-sword/");
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        assertNotNull(chapter);
        assertEquals("1", chapter.getChapterId());
        assertEquals(13, parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter));
    }

}
