package dev.tr7zw.mango_companion.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import lombok.Synchronized;

public class ZipCreator implements AutoCloseable{

    private ZipOutputStream out;
    
    public ZipCreator(File outputFile) throws IOException {
        out = new ZipOutputStream(new FileOutputStream(outputFile));
    }
    
    @Synchronized
    public void addFile(String name, InputStream stream) throws IOException {
        out.putNextEntry(new ZipEntry(name)); 
        // buffer size
        byte[] b = new byte[1024];
        int count;

        while ((count = stream.read(b)) > 0) {
            out.write(b, 0, count);
        }
        stream.close();
    }
    
    @Synchronized
    public void addFile(String name, byte[] data) throws IOException {
        out.putNextEntry(new ZipEntry(name)); 
        out.write(data);
    }
    
    @Synchronized
    public void addFile(String name, BufferedImage image) throws IOException {
        out.putNextEntry(new ZipEntry(name)); 
        ImageIO.write(image, "PNG", out);
    }

    @Synchronized
    @Override
    public void close() throws Exception {
        out.close();
    }
    
}
