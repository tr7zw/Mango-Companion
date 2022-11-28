package dev.tr7zw.mango_companion;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import dev.tr7zw.mango_companion.parser.AsuraScans;
import dev.tr7zw.mango_companion.parser.Bilibilicomics;
import dev.tr7zw.mango_companion.parser.Flamescans;
import dev.tr7zw.mango_companion.parser.Mangadex;
import dev.tr7zw.mango_companion.parser.Mangatx;
import dev.tr7zw.mango_companion.parser.ReadManganato;
import dev.tr7zw.mango_companion.util.FileChecker;
import dev.tr7zw.mango_companion.util.parser.Parser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.Timeout;

@Log
@RequiredArgsConstructor
public class MangoCompanion implements Runnable {

    private static boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
    private final File workingDir;
    @Getter
    private Set<Parser> parsers = new HashSet<>(Arrays.asList(new Mangadex(), new ReadManganato(), new Mangatx(), new AsuraScans(), new Flamescans(), new Bilibilicomics()));
    private static Timeout<Object> timeoutPolicy = Timeout.of(Duration.ofMinutes(1)).withInterrupt(true);
    
    @Override
    public void run() {
        
        while (true) {
            List<String> updated = new ArrayList<>();
            log.info("Starting check...");
            for (String url : App.getConfig().getUrls()) {
                try {
                    log.info("Checking '" + url + "'");
                    Failsafe.with(timeoutPolicy).run(() -> {
                        updateManga(url, updated);
                    });
                    if(String.join("\n", updated).length() > 950) { // Loaded so much that it just fits into a discord message
                        sendDiscordAndClear(updated);
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error while updating manga '" + url + "'!", e);
                }
            }
            sendDiscordAndClear(updated);
            log.info("Done with checks. Sleeping for " + App.getConfig().getSleepInMinutes() + " minutes.");
            try {
                Thread.sleep(Duration.ofMinutes(App.getConfig().getSleepInMinutes()).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void sendDiscordAndClear(List<String> updated) {
        String text = String.join("\n", updated);
        if(text.length() > 1000) {
            text = text.substring(0, 1000);
        }
        if(!text.isEmpty())
            App.getDiscord().sendUpdateMessage(new EmbedBuilder().setTitle("Downloaded Chapters").addField("New", text, false));
        updated.clear();
    }

    private void updateManga(String url, List<String> updated) throws IOException {
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
        String name = App.getConfig().getFolderOverwrites().containsKey(url) ? App.getConfig().getFolderOverwrites().get(url)
                : parser.getName(url); // Use overwrite, else parse the name
        name = cleanName(name);
        File targetDir = new File(workingDir, name);
        targetDir.mkdirs();
        log.fine("Updating " + name + "...");
        Iterator<Chapter> chapters = parser.getChapters(new FileChecker(targetDir), url);
        while (chapters.hasNext()) {
            Chapter c = chapters.next();
            try { // it's save to skip broken chapters for now
                File zip = new File(targetDir, "Chapter " + c.getChapterId() + ".zip");
                if (zip.exists())
                    continue;
                File zipPart = new File(targetDir, "Chapter " + c.getChapterId() + ".zip_part");
                if (zipPart.exists())
                    zipPart.delete();
                parser.downloadChapter(zipPart, c);
                zipPart.renameTo(zip);
                log.info("Downloaded " + zip.getAbsolutePath());
                updated.add(name + " - Chapter " + c.getChapterId());
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error while downloading manga '" + url + "' Chapter " + c.getChapterId() + "!", e);
            }
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
        if(windows) {
            title = title.replace('?', '-');
            if(title.length() > 60)
                title = title.substring(0, 50);
        }

        title = title.trim();
        return title;
    }

}
