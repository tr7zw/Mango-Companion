package dev.tr7zw.mango_companion;

import dev.tr7zw.mango_companion.parser.Bilibilicomics;
import dev.tr7zw.mango_companion.util.EmptyFileChecker;
import dev.tr7zw.mango_companion.util.parser.Parser;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Validate that Bilibilicomics is working */
public class BilibilicomicsTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public BilibilicomicsTest(String testName) {
        super(testName);
    }

    /** @return the suite of tests being tested */
    public static Test suite() {
        return new TestSuite(BilibilicomicsTest.class);
    }

    public void testUrls() {
        Parser parser = new Bilibilicomics();
        assertTrue(parser.canParse("https://www.bilibilicomics.com/detail/mc113"));
        assertFalse(parser.canParse("https://www.bilibilicomics.com/detail/"));
    }

    public void testName() throws IOException {
        Parser parser = new Bilibilicomics();
        assertEquals(
                "Forced to Be a Princess After Reincarnating in Another World",
                parser.getName("https://www.bilibilicomics.com/detail/mc113"));
    }

    public void testChapters() throws IOException {
        Parser parser = new Bilibilicomics();
        Iterator<Chapter> iterator =
                parser.getChapters(
                        new EmptyFileChecker(), "https://www.bilibilicomics.com/detail/mc113");
        assertTrue(iterator.hasNext());
        Chapter chapter = iterator.next();
        assertNotNull(chapter);
        assertEquals("0", chapter.getChapterId());
        parser.downloadChapter(
                Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
    }
}
