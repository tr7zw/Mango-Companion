package dev.tr7zw.mango_companion.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChapterParser {

    private static Pattern pattern = Pattern.compile("((C|c)hapter|(C|c)h)(\\.|\\s|-)(([0-9]+)((-|\\.)[0-9]+)?)");
    
    public static String getChapterId(String base) {
        Matcher matcher = pattern.matcher(base);
        if(matcher.find()) {
            return extractId(matcher);
        }
        // check for a literal("5", "123.12" etc)
        matcher = pattern.matcher("ch " + base);
        if(matcher.find()) {
            return extractId(matcher);
        }
        return null;
    }
    
    private static String extractId(Matcher matcher) {
        String id = "";
        id = "" + Integer.parseInt(matcher.group(6));
        String subChapter = matcher.group(7);
        if(subChapter != null) {
            id += subChapter;
        }
        
        return id.replace('-', '.');
    }
    
}
