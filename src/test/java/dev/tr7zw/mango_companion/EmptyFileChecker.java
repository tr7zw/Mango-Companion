package dev.tr7zw.mango_companion;

import dev.tr7zw.mango_companion.util.FileChecker;

public class EmptyFileChecker extends FileChecker {

    public EmptyFileChecker() {
        super(null);
    }

    @Override
    public boolean knownChapter(Chapter chapter) {
        return false;
    }

}
