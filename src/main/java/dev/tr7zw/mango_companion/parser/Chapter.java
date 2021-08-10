package dev.tr7zw.mango_companion.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@Getter
public class Chapter {

    private final Parser parser;
    private final String mangaUrl;
    private final String chapterId;
    
    @Setter
    private Object parserData;
    
}
