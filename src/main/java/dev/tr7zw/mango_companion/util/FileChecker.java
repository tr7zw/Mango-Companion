package dev.tr7zw.mango_companion.util;

import java.io.File;
import java.io.FilenameFilter;

import dev.tr7zw.mango_companion.parser.Chapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FileChecker {

    private File folder;

    public boolean knownChapter(Chapter chapter) {
        return folder.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.contains("Chapter " + chapter.getChapterId()) && !name.endsWith("_part"); // FIXME Currently
                                                                                                      // can confuse
                                                                                                      // Chapter 100 for
                                                                                                      // Chapter 1, this
                                                                                                      // is only an
                                                                                                      // issue if you
                                                                                                      // download past
                                                                                                      // chapters?
            }
        }).length != 0;
    }

}
