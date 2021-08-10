package dev.tr7zw.mango_companion;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.Sets;

import dev.tr7zw.mango_companion.parser.Chapter;
import dev.tr7zw.mango_companion.parser.Mangadex;
import dev.tr7zw.mango_companion.parser.Manganato;
import dev.tr7zw.mango_companion.parser.Mangatx;
import dev.tr7zw.mango_companion.parser.Parser;
import dev.tr7zw.mango_companion.util.FileChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class MangoCompanion implements Runnable {

    private final File workingDir;
    private final Config config;
    private Set<Parser> parsers = Sets.newHashSet(new Mangadex(), new Manganato(), new Mangatx());

    @Override
    public void run() {
        while (true) {
            for (String url : config.getUrls()) {
                try {
                    updateManga(url);
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error while updating manga '" + url + "'!", e);
                }
            }
            try {
                Thread.sleep(Duration.ofMinutes(config.getSleepInMinutes()).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateManga(String url) throws IOException {
        Parser parser = null;
        for (Parser p : parsers) {
            if (p.canParse(url)) {
                parser = p;
                break;
            }
        }
        if (parser == null) {
            log.warning("No parser found for url " + url);
            return;
        }
        String name = config.getFolderOverwrites().containsKey(url) ? config.getFolderOverwrites().get(url)
                : parser.getName(url); // Use overwrite, else parse the name
        name = cleanName(name);
        File targetDir = new File(workingDir, name);
        targetDir.mkdirs();
        log.fine("Updating " + name + "...");
        Iterator<Chapter> chapters = parser.getChapters(new FileChecker(targetDir), url);
        while (chapters.hasNext()) {
            Chapter c = chapters.next();
            File zip = new File(targetDir, "Chapter " + c.getChapterId() + ".zip");
            if (zip.exists())
                continue;
            File zipPart = new File(targetDir, "Chapter " + c.getChapterId() + ".zip_part");
            if (zipPart.exists())
                zipPart.delete();
            parser.downloadChapter(zipPart, c);
            zipPart.renameTo(zip);
            log.info("Downloaded " + zip.getAbsolutePath());
        }
    }
    
    /**
     * Remove invalid chars/trim string length. TODO better method
     * 
     * @param title
     * @return
     */
    private String cleanName(String title) {
        title = title.replace('|', '-');
        title = title.replace(':', '-');
        title = title.replace('\'','-');
        title = title.replace('.', ',');
        title = title.replace('"', '\'');
        title = title.replace('"', '\'');
        title = title.replace('@', 'A');
        if(title.length() > 60)
            title = title.substring(0, 50);
        title = title.trim();
        return title;
    }

}
