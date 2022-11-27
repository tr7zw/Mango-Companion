package dev.tr7zw.mango_companion.parser;

import java.util.List;
import java.util.regex.Matcher;

import dev.tr7zw.mango_companion.util.APIProxyBuilder;
import dev.tr7zw.mango_companion.util.EmptyEncoder;
import dev.tr7zw.mango_companion.util.HTMLPojoDecoder;
import dev.tr7zw.mango_companion.util.StreamUtil;
import dev.tr7zw.mango_companion.util.parser.AbstractMadaraParser;
import dev.tr7zw.mango_companion.util.parser.AbstractMadaraParser.MadaraSite;
import dev.tr7zw.mango_companion.util.parser.ParsedChapterEntry;
import dev.tr7zw.mango_companion.util.parser.ParsedChapterPage;
import dev.tr7zw.mango_companion.util.parser.ParsedMangaInfo;
import dev.tr7zw.mango_companion.util.parser.StandardLayoutApi;
import feign.Body;
import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Retryer;
import lombok.Getter;
import pl.droidsonroids.jspoon.annotation.Selector;

public class Mangagreat extends AbstractMadaraParser implements MadaraSite {

    private MangaGreatAPI baseApi = Feign.builder().decoder(new HTMLPojoDecoder())
            .client(StreamUtil.getClient(getLimiter())).encoder(new EmptyEncoder())
            .retryer(new Retryer.Default(1000, 1000, 3)).target(MangaGreatAPI.class, url());
    @Getter
    private StandardLayoutApi api = APIProxyBuilder.getProxy(baseApi::getMangaInfo, baseApi::getChapterPage);

    @Override
    public MadaraSite getTarget() {
        return this;
    }

    @Override
    public String name() {
        return "Mangagreat";
    }

    @Override
    public String url() {
        return "https://mangagreat.com";
    }

    private static interface MangaGreatAPI {

        @RequestLine("GET /manga/{uuid}/")
        MangaInfo getMangaInfo(@Param("uuid") String uuid);

        @RequestLine("GET /manga/{mangaUUID}/{chapterUUID}")
        ChapterPage getChapterPage(@Param("mangaUUID") String mangaUUID, @Param("chapterUUID") String chapterUUID);

        @RequestLine("POST /wp-admin/admin-ajax.php")
        @Body("action=manga_get_reading_nav&manga={shortid}&volume_id=0&type=content")
        @Headers({ "Content-Type: application/x-www-form-urlencoded; charset=UTF-8"})
        MangaInfo getChapterList(@Param("shortid") String shortid);

    }

    @Override
    public ParsedChapterEntry[] getChapters(String url) {
        MangaInfo info = baseApi.getMangaInfo(getMangaUUID(url));
//        if (info.chaptersList != null)
//            return info.getChapters();
        Matcher matcher = getMangaShortUUIDPattern().matcher(info.getShortLink());
        matcher.find();
        String mangaShortId = matcher.group(1);
        return baseApi.getChapterList(mangaShortId).getChapters();
    }

    @Getter
    private static class MangaInfo implements ParsedMangaInfo {
        @Selector(".post-title h1")
        String rawtitle;
        @Selector(value = "head > link[rel=shortlink]", attr = "href")
        String shortLink;
        @Selector(value = ".short")
        List<ChapterEntry> chaptersList;

        @Override
        public ParsedChapterEntry[] getChapters() {
            return chaptersList.toArray(new ParsedChapterEntry[0]);
        }

        @Override
        public String getTitle() {
            return rawtitle.replace(" – Mangagreat", ""); // Mangagreat adds their name to each title
        }

    }

    @Getter
    private static class ChapterEntry implements ParsedChapterEntry {
        @Selector(value = "*")
        String chapter;
        @Selector(value = "*", attr = "data-redirect")
        String url;
    }

    @Getter
    private static class ChapterPage implements ParsedChapterPage {
        @Selector(value = ".reading-content img", attr = "src")
        List<String> imageUrlsGallery;
        @Selector(value = ".wp-manga-chapter-img", attr = "data-src")
        List<String> imageUrlsMangaChapter;

        @Override
        public List<String> getImageUrls() {
            if (imageUrlsMangaChapter != null && !imageUrlsMangaChapter.isEmpty())
                return imageUrlsMangaChapter;
            if (imageUrlsGallery != null && !imageUrlsGallery.isEmpty())
                return imageUrlsGallery;
            throw new NullPointerException("No image links where found!");
        }

    }

}
