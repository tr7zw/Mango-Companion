package dev.tr7zw.mango_companion;

import dev.tr7zw.mango_companion.parser.Mangatx;
import dev.tr7zw.mango_companion.util.EmptyFileChecker;
import dev.tr7zw.mango_companion.util.parser.Parser;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Validate that Mangatx is working */
public class MangatxTest extends TestCase {

  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public MangatxTest(String testName) {
    super(testName);
  }

  /** @return the suite of tests being tested */
  public static Test suite() {
    return new TestSuite(MangatxTest.class);
  }

  public void testUrls() {
    Parser parser = new Mangatx();
    assertTrue(parser.canParse("https://mangatx.com/manga/the-player-that-cant-level-up/"));
    assertTrue(parser.canParse("https://mangatx.com/manga/the-player-that-cant-level-up"));
    assertFalse(parser.canParse("https://mangatx.com/manga"));
    assertFalse(parser.canParse("https://mangatx.com/"));
    assertFalse(parser.canParse("https://mangatx.com/manga-genre/comic/"));
  }

  public void testName() throws IOException {
    Parser parser = new Mangatx();
    assertEquals(
        "The Player that can’t Level Up",
        parser.getName("https://mangatx.com/manga/the-player-that-cant-level-up/"));
  }

  public void testChapters() throws IOException {
    Parser parser = new Mangatx();
    Iterator<Chapter> iterator =
        parser.getChapters(
            new EmptyFileChecker(),
            "https://mangatx.com/manga/reformation-of-the-deadbeat-noble/"); // Different manga
                                                                             // because the other
                                                                             // one has broken
                                                                             // images
    assertTrue(iterator.hasNext());
    Chapter chapter = iterator.next();
    assertNotNull(chapter);
    assertEquals("1", chapter.getChapterId());
    parser.downloadChapter(Files.createTempFile("unittest", "chapterdownload").toFile(), chapter);
  }
}
