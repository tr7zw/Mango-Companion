package dev.tr7zw.mango_companion;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;

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

    private static boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
    private final File workingDir;
    private final Config config;
    private Set<Parser> parsers = Sets.newHashSet(new Mangadex(), new Manganato(), new Mangatx());

    // Discord
    private static DiscordApi api = null;
    private static TextChannel targetChannel = null;
    
    @Override
    public void run() {
        if(config.getDiscordApiToken() != null && !config.getDiscordApiToken().isBlank() && config.getChannelId() != -1) {
            try {
                api = new DiscordApiBuilder().setToken(config.getDiscordApiToken()).login().join();
                log.info("Url to add the bot to your Discord: " + api.createBotInvite());
                Optional<TextChannel> channel = api.getTextChannelById(config.getChannelId());
                if(channel.isPresent()) {
                    targetChannel = channel.get();    
                }else {
                    throw new IllegalArgumentException("Discord channel not found! Is the bot on the correct server?");
                }
            }catch(Exception ex) {
                log.log(Level.SEVERE, "Error while setting up the Discord bot!", ex);
                System.exit(1);
            }
        }
        
        while (true) {
            List<String> updated = new ArrayList<>();
            for (String url : config.getUrls()) {
                try {
                    updateManga(url, updated);
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error while updating manga '" + url + "'!", e);
                }
            }
            if(targetChannel != null && !updated.isEmpty()) {
                try {
                    targetChannel.sendMessage(new EmbedBuilder().setTitle("Downloaded Chapters").addField("New", String.join("\n", updated))).get();
                }catch(Exception ex) {
                    log.log(Level.WARNING, "Error sending Discord message!", ex);
                }
            }
            try {
                Thread.sleep(Duration.ofMinutes(config.getSleepInMinutes()).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
            updated.add(name + " - Chapter " + c.getChapterId());
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
        if(windows &&  title.length() > 60)
            title = title.substring(0, 50);
        title = title.trim();
        return title;
    }

}
