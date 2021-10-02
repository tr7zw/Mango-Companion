package dev.tr7zw.mango_companion.parser;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dev.tr7zw.mango_companion.util.APIProxyBuilder;
import dev.tr7zw.mango_companion.util.RateLimiter;
import dev.tr7zw.mango_companion.util.StreamUtil;
import dev.tr7zw.mango_companion.util.parser.ParsedChapterEntry;
import dev.tr7zw.mango_companion.util.parser.ParsedChapterPage;
import dev.tr7zw.mango_companion.util.parser.ParsedMangaInfo;
import dev.tr7zw.mango_companion.util.parser.StandardLayoutApi;
import dev.tr7zw.mango_companion.util.parser.StandardLayoutParser;
import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Bilibilicomics extends StandardLayoutParser {

    @Getter
    private Pattern uriPattern = Pattern.compile("https?://www.bilibilicomics.com/detail/mc.+");
    @Getter
    private Pattern mangaUriUUIDPattern = Pattern.compile("https://www.bilibilicomics.com/detail/mc([a-z0-9]+)");
    @Getter
    private Pattern chapterUriUUIDPattern = Pattern.compile("https://www.bilibilicomics.com/mc[0-9]+/([0-9]+)");

    private static RateLimiter limiter = new RateLimiter(1, Duration.ofSeconds(1));
    private static BilibilicomicsAPI bilibilicomicsApi = Feign.builder().decoder(new GsonDecoder()).addCapability(limiter)
            .client(StreamUtil.getClient()).encoder(new GsonEncoder()).retryer(new Retryer.Default(1000, 1000, 3))
            .target(BilibilicomicsAPI.class, "https://www.bilibilicomics.com");
    @Getter
    private StandardLayoutApi api = APIProxyBuilder.getProxy(bilibilicomicsApi::getMangaInfo, bilibilicomicsApi::getChapterPage);

    private static interface BilibilicomicsAPI {

        default MangaInfo getMangaInfo(String uuid){
            return getMangaInfoRequest(new ComicRequest(Integer.parseInt(uuid)));
        }
        
        @RequestLine("POST twirp/comic.v1.Comic/ComicDetail?device=pc&platform=web")
        @Headers({"Content-Type: application/json;charset=utf-8"})
        MangaInfo getMangaInfoRequest(ComicRequest uuid);

        default ChapterPage getChapterPage(@Param("mangaUUID") String mangaUUID, @Param("chapterUUID") String chapterUUID) {
            return getChapterPageRequest(new EpRequest(Integer.parseInt(chapterUUID)));
        }
        
        @RequestLine("POST twirp/comic.v1.Comic/GetImageIndex?device=pc&platform=web")
        @Headers({"Content-Type: application/json;charset=utf-8"})
        ChapterPage getChapterPageRequest(EpRequest epRequest);
        
        @RequestLine("POST twirp/comic.v1.Comic/ImageToken?device=pc&platform=web")
        @Headers({"Content-Type: application/json;charset=utf-8"})
        ImageTokenResponse getImageToken(ImageTokenRequest epRequest);

    }
    
    @Override
    public RateLimiter getLimiter() {
        return limiter;
    }
    
    @AllArgsConstructor
    private static class ComicRequest{
        int comic_id;
    }
    
    @AllArgsConstructor
    private static class EpRequest{
        int ep_id;
    }
    
    @AllArgsConstructor
    private static class ImageTokenRequest{
        String urls;
    }
    
    private static class ImageTokenResponse {
        List<ImageTokenResponseData> data;
    }
    
    
    private static class ImageTokenResponseData {
        String url;
        String token;
    }
    
    
    @Getter
    private static class MangaInfo implements ParsedMangaInfo {
        MangaData data;
        @Override
        public String getTitle() {
            return data.title;
        }
        @Override
        public ParsedChapterEntry[] getChapters() {
            ParsedChapterEntry[] list = new ParsedChapterEntry[data.ep_list.size()];
            for(int i = 0; i < list.length; i++) {
                Episode ep = data.ep_list.get(i);
                ParsedChapterEntry entry = new ParsedChapterEntry() {
                    
                    @Override
                    public String getUrl() {
                        return "https://www.bilibilicomics.com/mc" + data.id + "/" + ep.id;
                    }
                    
                    @Override
                    public String getChapter() {
                        return ep.short_title;
                    }
                };
                list[i] = entry;
            }
            return list;
        }
    }
    
    private static class MangaData {
        String title;
        int id;
        List<Episode> ep_list;
    }

    private static class Episode {
        int id;
        String short_title;
    }

    @Getter
    private static class ChapterPage implements ParsedChapterPage {

        ImageIndexData data;
        
        @Override
        public List<String> getImageUrls() {
            List<String> urls = new ArrayList<>();
            for(ImageData i : data.images) {
                ImageTokenResponse resp = bilibilicomicsApi.getImageToken(new ImageTokenRequest("[\"" + i.path + "\"]"));
                urls.add(resp.data.get(0).url + "?token=" + resp.data.get(0).token);
            }
            return urls;
        }
        
    }
    
    private static class ImageIndexData {
        List<ImageData> images;
    }
    
    private static class ImageData {
        String path;
    }

}
