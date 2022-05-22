package dev.tr7zw.mango_companion.converter;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.wicket.util.file.Files;

import dev.tr7zw.mango_companion.util.ZipCreator;
import dev.tr7zw.mango_companion.util.external.ResmuchitUtil;

public class ZipCompressor {

    private static final File tmp = new File("tmp.zip");
    private static long totatalSavedSize = 0;

    /**
     * Not fully working, may create corrupted zips or crash at some exception. Better to wait for jxl support.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        processFile(new File(args[0]));
        System.out.println("Total saved space: " + formatFileSize(totatalSavedSize));
    }
    
    public static void processFile(File file) throws Exception {
        if(file.isDirectory()) {
            for(File f : file.listFiles())
                processFile(f);
        }else if(file.isFile() && (file.getName().toLowerCase().endsWith(".zip") || file.getName().toLowerCase().endsWith(".cbz"))) {
            processZip(file);
            System.out.println("Total saved space: " + formatFileSize(totatalSavedSize));
        }
    }

    private static void processZip(File zipIn) throws Exception {
        if (tmp.exists())
            tmp.delete();
        try (ZipFile zipFile = new ZipFile(zipIn)) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().equals("compressedv1")) {
                    System.out.println("File is already compressed!");
                    return;
                }
            }
            // unprocessed Zip
            entries = zipFile.entries();
            try (ZipCreator zipCreator = new ZipCreator(tmp)) {
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        InputStream compressed = ResmuchitUtil.compressImage(70, stream, entry.getName());
                        zipCreator.addFile(entry.getName(), compressed != null ? compressed : zipFile.getInputStream(entry));
                    }
                }
                zipCreator.addFile("compressedv1", new byte[0]);
            }
        }
        long dif = (zipIn.length() - tmp.length());
        totatalSavedSize += dif;
        System.out.println("Final size difference: " + dif);
        zipIn.delete();
        Files.copy(tmp, zipIn);
        tmp.delete();
    }

    private static String formatFileSize(long fileSize) {
        String unit = "B";
        double size = 0;

        if (fileSize < 1024) {
            unit = "B";
            size = (double) fileSize;
        } else if (fileSize < (1024 * 1024L)) {
            unit = "KB";
            size = (double) fileSize / (1024L);
        } else if (fileSize < (1024 * 1024 * 1024L)) {
            unit = "MB";
            size = (double) fileSize / (1024 * 1024L);
        } else {
            unit = "GB";
            size = (double) fileSize / (1024 * 1024 * 1024L);
        }

        return String.format("%1$.2f", size) + " " + unit;
    }
    
}
