package dev.tr7zw.mango_companion.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import dev.tr7zw.mango_companion.util.parser.ParsedChapterPage;
import dev.tr7zw.mango_companion.util.parser.ParsedMangaInfo;
import dev.tr7zw.mango_companion.util.parser.StandardLayoutApi;

public class APIProxyBuilder {

    public static StandardLayoutApi getProxy(Function<String, ParsedMangaInfo> getMangaInfoFunction, BiFunction<String, String, ParsedChapterPage> getChapterPageFunction) {
        return new StandardLayoutApi() {
            
            @Override
            public ParsedMangaInfo getMangaInfo(String uuid) {
                return getMangaInfoFunction.apply(uuid);
            }
            
            @Override
            public ParsedChapterPage getChapterPage(String mangaUUID, String chapterUUID) {
                return getChapterPageFunction.apply(mangaUUID, chapterUUID);
            }
        };
    }
    
}
