package dev.tr7zw.mango_companion.util.external;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;

public class ResmuchitUtil {

    private static final String apiEndpoint = "http://api.resmush.it/?qlty=";
    private static final HttpClient client = HttpClientBuilder.create().build(); 
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static InputStream compressImage(int quality, InputStream imageStream, String filename) throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost(apiEndpoint + quality);
        // 
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("files", new InputStreamBody(imageStream, filename));
        HttpEntity entity = builder.build();
        //
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        CompressOutput output = gson.fromJson(new InputStreamReader(response.getEntity().getContent()), CompressOutput.class);
        if(output.getPercent() <= 0) {
            System.out.println("Size unchanged, returning");
            return null;
        }
        System.out.println("Compressed file by " + output.getPercent() +"%. Downloading...");
        return new URL(output.getDest()).openStream();
    }
    
    @Getter
    private static class CompressOutput {
        private String dest;
        private int percent;
    }
    
}
