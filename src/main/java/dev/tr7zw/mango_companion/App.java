package dev.tr7zw.mango_companion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import dev.tr7zw.mango_companion.rest.WebserverManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import lombok.Getter;

public class App {

  @Getter private static Config config = new Config();
  private static File configFile;
  @Getter private static MangoCompanion companion;
  @Getter private static WebserverManager webserver = new WebserverManager();
  @Getter private static DiscordProvider discord = new DiscordProvider();

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Root path to the library is required!");
      return;
    }
    File workingDir = new File(args[0]);
    if (!workingDir.exists() || !workingDir.isDirectory()) {
      System.out.println("Not a valid directory!");
      return;
    }
    configFile = new File(workingDir, "config.json");
    loadConfigFile();
    saveConfigFile();

    discord.init();

    webserver.init();

    companion = new MangoCompanion(workingDir);
    companion.run();
  }

  public static void loadConfigFile()
      throws JsonSyntaxException, JsonIOException, FileNotFoundException {
    if (configFile.exists()) {
      config =
          new Gson().fromJson(new InputStreamReader(new FileInputStream(configFile)), Config.class);
    }
  }

  public static void saveConfigFile() throws IOException {
    Files.write(
        configFile.toPath(),
        new GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(config)
            .getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }
}
