package dev.tr7zw.mango_companion.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCreator implements AutoCloseable{

    private ZipOutputStream out;
    
    public ZipCreator(File outputFile) throws IOException {
        out = new ZipOutputStream(new FileOutputStream(outputFile));
    }
    
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

    @Override
    public void close() throws Exception {
        out.close();
    }
    
}
