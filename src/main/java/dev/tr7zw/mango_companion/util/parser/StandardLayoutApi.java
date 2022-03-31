package dev.tr7zw.mango_companion.util.parser;

public interface StandardLayoutApi {
    ParsedMangaInfo getMangaInfo(String uuid);

    ParsedChapterPage getChapterPage(String mangaUUID, String chapterUUID);;
}
