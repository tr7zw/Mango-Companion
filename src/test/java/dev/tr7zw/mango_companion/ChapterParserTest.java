package dev.tr7zw.mango_companion;

import dev.tr7zw.mango_companion.util.ChapterParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the chapter id parsing
 */
public class ChapterParserTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ChapterParserTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ChapterParserTest.class);
    }

    /**
     * Not valid chapter names
     */
    public void testInvalidNames() {
        assertNull(ChapterParser.getChapterId("  Invalid 300  "));
        assertNull(ChapterParser.getChapterId("Invalid 300"));
        assertNull(ChapterParser.getChapterId("Invalid 300.1"));
        assertNull(ChapterParser.getChapterId("Invalid-300"));
        assertNull(ChapterParser.getChapterId("Invalid.300"));
        assertNull(ChapterParser.getChapterId("Invalid.300.1"));
        assertNull(ChapterParser.getChapterId("Not a chapter"));
    }

    public void testDirectNames() {
        assertEquals("300", ChapterParser.getChapterId("  Chapter 300  "));
        assertEquals("300", ChapterParser.getChapterId("  Ch 300  "));
        assertEquals("300", ChapterParser.getChapterId("  chapter 300  "));
        assertEquals("300", ChapterParser.getChapterId("  ch 300  "));
        assertEquals("300", ChapterParser.getChapterId("  ch-300  "));
        assertEquals("300", ChapterParser.getChapterId("  ch.300  "));
        assertEquals("300", ChapterParser.getChapterId("  Chapter-300  "));
        assertEquals("300", ChapterParser.getChapterId("  Chapter.300  "));
        assertEquals("300", ChapterParser.getChapterId("Chapter 300.zip"));
        assertEquals("300", ChapterParser.getChapterId("Ch 300.zip"));
        assertEquals("300", ChapterParser.getChapterId("chapter 300.zip"));
        assertEquals("300", ChapterParser.getChapterId("ch 300.zip"));
        assertEquals("300", ChapterParser.getChapterId("ch-300.zip"));
        assertEquals("300", ChapterParser.getChapterId("ch.300.zip"));
        assertEquals("300", ChapterParser.getChapterId("Chapter-300.zip"));
        assertEquals("300", ChapterParser.getChapterId("Chapter.300.zip"));
    }

    public void testDirectNamesWithSubchapters() {
        assertEquals("300.12", ChapterParser.getChapterId("  Chapter 300.12  "));
        assertEquals("300.12", ChapterParser.getChapterId("  Ch 300.12  "));
        assertEquals("300.12", ChapterParser.getChapterId("  chapter 300.12  "));
        assertEquals("300.12", ChapterParser.getChapterId("  ch 300.12  "));
        assertEquals("300.12", ChapterParser.getChapterId("  ch-300.12  "));
        assertEquals("300.12", ChapterParser.getChapterId("  ch.300.12  "));
        assertEquals("300.12", ChapterParser.getChapterId("  Chapter-300.12  "));
        assertEquals("300.12", ChapterParser.getChapterId("  Chapter.300.12  "));
        assertEquals("300.12", ChapterParser.getChapterId("Chapter 300.12.zip"));
        assertEquals("300.12", ChapterParser.getChapterId("Ch 300.12.zip"));
        assertEquals("300.12", ChapterParser.getChapterId("chapter 300.12.zip"));
        assertEquals("300.12", ChapterParser.getChapterId("ch 300.12.zip"));
        assertEquals("300.12", ChapterParser.getChapterId("ch-300.12.zip"));
        assertEquals("300.12", ChapterParser.getChapterId("ch.300.12.zip"));
        assertEquals("300.12", ChapterParser.getChapterId("Chapter-300.12.zip"));
        assertEquals("300.12", ChapterParser.getChapterId("Chapter.300.12.zip"));
    }
    
    public void testWeirdFormat() {
        assertEquals("1.1", ChapterParser.getChapterId("001-1"));
        assertEquals("1.2", ChapterParser.getChapterId("001-2"));
    }

    public void testUrls() {
        assertEquals("180.1", ChapterParser.getChapterId("https://example.com/manga/manga-name/chapter-180-1/"));
        assertEquals("180.123", ChapterParser.getChapterId("https://example.com/manga/manga-name/chapter-180-123/"));
        assertEquals("180", ChapterParser.getChapterId("https://example.com/manga/manga-name/chapter-180/"));
        assertEquals("123", ChapterParser.getChapterId("https://example.com/manga/manga-name/chapter-123/"));
        assertEquals("180.1", ChapterParser.getChapterId("https://example.com/manga/manga-name/ch-180-1/"));
        assertEquals("180.123", ChapterParser.getChapterId("https://example.com/manga/manga-name/ch-180-123/"));
        assertEquals("180", ChapterParser.getChapterId("https://example.com/manga/manga-name/ch-180/"));
        assertEquals("123", ChapterParser.getChapterId("https://example.com/manga/manga-name/ch-123/"));
    }

    public void testLiterals() {
        assertEquals("1", ChapterParser.getChapterId("1"));
        assertEquals("123.45", ChapterParser.getChapterId("123.45"));
    }

    public void testLeadingZeros() {
        assertEquals("1", ChapterParser.getChapterId("001"));
        assertEquals("1", ChapterParser.getChapterId("Chapter 001"));
        assertEquals("1.1", ChapterParser.getChapterId("Chapter 001.1"));
        assertEquals(ChapterParser.getChapterId("1"), ChapterParser.getChapterId("001"));
        assertEquals(ChapterParser.getChapterId("1.1"), ChapterParser.getChapterId("001-1"));
    }

}
