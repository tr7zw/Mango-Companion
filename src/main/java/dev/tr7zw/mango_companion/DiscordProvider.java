package dev.tr7zw.mango_companion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import lombok.extern.java.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Log
public class DiscordProvider {

    private static JDA api = null;
    private static TextChannel targetChannel = null;
    private ExecutorService pool = Executors.newSingleThreadExecutor();
    
    public void init() {
        if(App.getConfig().getDiscordApiToken() != null && !App.getConfig().getDiscordApiToken().isEmpty() && App.getConfig().getChannelId() != -1) {
            try {
                api = JDABuilder.createDefault(App.getConfig().getDiscordApiToken()).setActivity(Activity.watching("Manga")).build();
                api.awaitReady();
                log.info("Url to add the bot to your Discord: " + api.getInviteUrl());
                targetChannel = api.getTextChannelById(App.getConfig().getChannelId());
                if(targetChannel == null) {
                    log.warning("Guilds: " + api.getGuilds());
                    log.warning("Channels: " + api.getTextChannels());
                    throw new IllegalArgumentException("Discord channel not found! Is the bot on the correct server?");
                }
            }catch(Exception ex) {
                log.log(Level.SEVERE, "Error while setting up the Discord bot!", ex);
                System.exit(1);
            }
        }
    }
    
    public void sendUpdateMessage(EmbedBuilder builder) {
        if(targetChannel != null) {
            pool.submit( () -> {
                try {
                    targetChannel.sendMessageEmbeds(builder.build()).submit();
                }catch(Exception ex) {
                    log.log(Level.WARNING, "Error sending Discord message!", ex);
                }
            });
        }
    }
    
}
