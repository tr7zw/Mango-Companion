package dev.tr7zw.mango_companion;

import java.util.Optional;
import java.util.logging.Level;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import lombok.extern.java.Log;

@Log
public class DiscordProvider {

    private static DiscordApi api = null;
    private static TextChannel targetChannel = null;
    
    public void init() {
        if(App.getConfig().getDiscordApiToken() != null && !App.getConfig().getDiscordApiToken().isEmpty() && App.getConfig().getChannelId() != -1) {
            try {
                api = new DiscordApiBuilder().setToken(App.getConfig().getDiscordApiToken()).login().join();
                log.info("Url to add the bot to your Discord: " + api.createBotInvite());
                Optional<TextChannel> channel = api.getTextChannelById(App.getConfig().getChannelId());
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
    }
    
    public void sendUpdateMessage(EmbedBuilder builder) {
        if(targetChannel != null) {
            try {
                targetChannel.sendMessage(builder).get();
            }catch(Exception ex) {
                log.log(Level.WARNING, "Error sending Discord message!", ex);
            }
        }
    }
    
}
