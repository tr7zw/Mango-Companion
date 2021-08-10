package dev.tr7zw.mango_companion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class App 
{
    public static void main( String[] args ) throws IOException
    {
        File workingDir = new File(".");
        File configFile = new File(workingDir, "config.json");
        Config config = new Config();
        if(configFile.exists()) {
            config = new Gson().fromJson(new InputStreamReader(new FileInputStream(configFile)), Config.class);
        }
        Files.write(configFile.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(config).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        MangoCompanion companion = new MangoCompanion(workingDir, config);
        companion.run();
    }
    

}
