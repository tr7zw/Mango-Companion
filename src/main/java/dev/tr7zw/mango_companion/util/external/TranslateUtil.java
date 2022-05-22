package dev.tr7zw.mango_companion.util.external;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import dev.tr7zw.mango_companion.util.FileUtil;

public class TranslateUtil {

    private static final String translateServer = "http://127.0.0.1:5003/run";
    private static final File dataFolder = new File("E:/mangatest/result");
    private static final HttpClient client = HttpClientBuilder.create().build(); 
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static void main( String[] args ) throws Exception
    {
        File in = new File("C:/Users/tr7zw/Downloads/98159891_p1.png");
        File out = new File("C:/Users/tr7zw/Downloads/translated.png");
        try(FileInputStream fileIn = new FileInputStream(in)){
            try(FileOutputStream fileOut = new FileOutputStream(out)){
                getTranslation(fileIn).transferTo(fileOut);
            }
        }
    }
    
    public static FileInputStream getTranslation(InputStream imageStream) throws ClientProtocolException, IOException {
        for(File f : dataFolder.listFiles())
            FileUtil.delete(f);
        HttpPost post = new HttpPost(translateServer);
        // 
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", new InputStreamBody(imageStream, "file"));
        builder.addPart("translator", new StringBody("papago"));
        builder.addPart("tgt_lang", new StringBody("ENG"));
        HttpEntity entity = builder.build();
        //
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        JsonObject json = gson.fromJson(new InputStreamReader(response.getEntity().getContent()), JsonObject.class);
        System.out.println(json);
        if(!json.get("status").getAsString().equals("successful")) {
            throw new IOException("Translation was not successful!");
        }
        return new FileInputStream(new File(dataFolder, json.get("task_id").getAsString() + "/final.png"));
    }
    
}
