package dev.tr7zw.mango_companion.util.parser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import dev.tr7zw.mango_companion.Chapter;
import dev.tr7zw.mango_companion.util.FileChecker;

public interface Parser {

    public boolean canParse(String url);
    
    public Iterator<Chapter> getChapters(FileChecker checker, String url) throws IOException;
    
    public List<String> getImages(Chapter chapter) throws IOException;
    
    public int downloadChapter(File target, Chapter chapter) throws IOException;
    
    public String getName(String url) throws IOException;
    
}
