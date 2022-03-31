package dev.tr7zw.mango_companion.parser;

import dev.tr7zw.mango_companion.util.APIProxyBuilder;
import dev.tr7zw.mango_companion.util.EmptyEncoder;
import dev.tr7zw.mango_companion.util.HTMLPojoDecoder;
import dev.tr7zw.mango_companion.util.RateLimiter;
import dev.tr7zw.mango_companion.util.StreamUtil;
import dev.tr7zw.mango_companion.util.parser.ParsedChapterEntry;
import dev.tr7zw.mango_companion.util.parser.ParsedChapterPage;
import dev.tr7zw.mango_companion.util.parser.ParsedMangaInfo;
import dev.tr7zw.mango_companion.util.parser.StandardLayoutApi;
import dev.tr7zw.mango_companion.util.parser.StandardLayoutParser;
import feign.Feign;
import feign.Param;
import feign.RequestLine;
import feign.Retryer;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Getter;
import pl.droidsonroids.jspoon.annotation.Selector;

public class ReadManganato extends StandardLayoutParser {

  @Getter private Pattern uriPattern = Pattern.compile("https?://readmanganato.com/manga-.+");

  @Getter
  private Pattern mangaUriUUIDPattern =
      Pattern.compile("https?://readmanganato.com/manga-([a-z0-9]+)");

  @Getter
  private Pattern chapterUriUUIDPattern =
      Pattern.compile("https?://readmanganato.com/manga-[a-z0-9]+/([a-z-0-9\\.]+)");

  @Getter private RateLimiter limiter = new RateLimiter(5, Duration.ofSeconds(1));
  private ReadManganatoAPI asuraApi =
      Feign.builder()
          .decoder(new HTMLPojoDecoder())
          .client(StreamUtil.getClient(limiter))
          .encoder(new EmptyEncoder())
          .retryer(new Retryer.Default(1000, 1000, 3))
          .target(ReadManganatoAPI.class, "https://readmanganato.com");

  @Getter
  private StandardLayoutApi api =
      APIProxyBuilder.getProxy(asuraApi::getMangaInfo, asuraApi::getChapterPage);

  private static interface ReadManganatoAPI {

    @RequestLine("GET /manga-{uuid}/")
    MangaInfo getMangaInfo(@Param("uuid") String uuid);

    @RequestLine("GET /manga-{mangaUUID}/{chapterUUID}")
    ChapterPage getChapterPage(
        @Param("mangaUUID") String mangaUUID, @Param("chapterUUID") String chapterUUID);
  }

  @Getter
  private static class MangaInfo implements ParsedMangaInfo {
    @Selector(".story-info-right > h1:nth-child(1)")
    String title;

    @Selector(".chapter-name")
    ChapterEntry[] chapters;
  }

  @Getter
  private static class ChapterEntry implements ParsedChapterEntry {
    @Selector(value = "[href]")
    String chapter;

    @Selector(value = "[href]", attr = "href")
    String url;
  }

  @Getter
  private static class ChapterPage implements ParsedChapterPage {
    @Selector(value = ".container-chapter-reader > img", attr = "src")
    List<String> imageUrls;
  }
}
