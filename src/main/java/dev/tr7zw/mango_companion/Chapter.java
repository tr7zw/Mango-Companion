package dev.tr7zw.mango_companion;

import dev.tr7zw.mango_companion.util.parser.Parser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@Getter
public class Chapter implements Comparable<Chapter> {

    private final Parser parser;
    private final String url;
    private final String chapterId;
    
    @Setter
    private Object parserData;

    @Override
    public int compareTo(Chapter o) {
        return Double.compare(extractDouble(chapterId), extractDouble(o.getChapterId()));
    }
    
    private static double extractDouble(String s) {
        String num = s.replaceAll("[^\\d.]", "");
        // return 0 if no digits found
        return num.isEmpty() ? 0 : Double.parseDouble(num);
    }
    
}
