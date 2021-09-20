package dev.tr7zw.mango_companion.util;

import java.io.File;
import java.io.FilenameFilter;

import dev.tr7zw.mango_companion.Chapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FileChecker {

    private File folder;

    public boolean knownChapter(Chapter chapter) {
        return folder.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith(".zip_part")) {
                    new File(dir, name).delete();
                    return false;
                }
                String ch = ChapterParser.getChapterId(chapter.getChapterId());
                if(ch == null)return false;
                return ch.equals(ChapterParser.getChapterId(name));
            }
        }).length != 0;
    }

}
