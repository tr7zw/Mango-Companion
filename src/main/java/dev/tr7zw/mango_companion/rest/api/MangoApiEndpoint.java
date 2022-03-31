package dev.tr7zw.mango_companion.rest.api;

import dev.tr7zw.mango_companion.App;
import dev.tr7zw.mango_companion.Chapter;
import dev.tr7zw.mango_companion.util.EmptyFileChecker;
import dev.tr7zw.mango_companion.util.FileChecker;
import dev.tr7zw.mango_companion.util.parser.Parser;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.apache.pdfbox.util.Hex;
import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.RequestParam;
import org.wicketstuff.rest.contenthandling.json.webserialdeserial.GsonWebSerialDeserial;
import org.wicketstuff.rest.resource.AbstractRestResource;

@ResourcePath("/api/mango")
public class MangoApiEndpoint extends AbstractRestResource<GsonWebSerialDeserial> {

  private static final long serialVersionUID = 1L;
  private FileChecker checker = new EmptyFileChecker();

  public MangoApiEndpoint() {
    super(new GsonWebSerialDeserial());
  }

  @MethodMapping("/listChapters")
  public ChapterResponse getChapters(@RequestParam(value = "url", required = false) String url) {
    ChapterResponse resp = new ChapterResponse();
    if (url == null) {
      resp.error = "No Url provided!";
      return resp;
    }
    Optional<Parser> matchingParser =
        App.getCompanion().getParsers().stream().filter(p -> p.canParse(url)).findFirst();
    if (!matchingParser.isPresent()) {
      resp.error = "No parser found for url '" + url + "'";
      return resp;
    }
    Parser parser = matchingParser.get();
    try {
      resp.title = parser.getName(url);
      Iterator<Chapter> chapters = parser.getChapters(checker, url);
      resp.chapters = new ArrayList<>();
      while (chapters.hasNext()) {
        ChapterData data = new ChapterData();
        resp.chapters.add(data);
        Chapter ch = chapters.next();
        data.title = "Chapter " + ch.getChapterId();
        data.id = Hex.getString((url + "|" + ch.getChapterId()).getBytes());
      }
      return resp;
    } catch (Exception ex) {
      ex.printStackTrace();
      resp = new ChapterResponse();
      resp.error = "Exception while parsing the chapter data!";
      return resp;
    }
  }

  @Data
  private static class ChapterResponse {
    String error;
    String title;
    List<ChapterData> chapters;
  }

  @Data
  private static class ChapterData {
    String id;
    String title;
  }

  @MethodMapping("/selectChapter")
  public ChapterDataResponse selectChapter(
      @RequestParam(value = "id", required = false) String id) {
    ChapterDataResponse resp = new ChapterDataResponse();
    if (id == null) {
      resp.error = "No ID provided!";
      return resp;
    }
    try {
      id = new String(Hex.decodeHex(id));
      String chapterid = id.split("\\|")[1];
      String url = id.split("\\|")[0];
      Optional<Parser> matchingParser =
          App.getCompanion().getParsers().stream().filter(p -> p.canParse(url)).findFirst();
      if (!matchingParser.isPresent()) {
        resp.error = "No parser found for url '" + url + "'";
        return resp;
      }
      Parser parser = matchingParser.get();
      resp.setTitle("Chapter " + chapterid);
      Iterator<Chapter> chapters = parser.getChapters(checker, url);
      Chapter chapter = null;
      while (chapters.hasNext()) {
        Chapter ch = chapters.next();
        if (ch.getChapterId().equals(chapterid)) {
          chapter = ch;
          break;
        }
      }
      if (chapter == null) {
        resp.error = "Chapter not found!";
        return resp;
      }
      resp.setPages(parser.getImages(chapter));
      return resp;
    } catch (Exception ex) {
      ex.printStackTrace();
      resp = new ChapterDataResponse();
      resp.error = "Exception while parsing the chapter data!";
      return resp;
    }
  }

  @Data
  private static class ChapterDataResponse {
    String error;
    String title;
    List<String> pages;
  }
}
