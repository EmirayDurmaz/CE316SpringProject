package com.example.iae;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {

    public List<String> extract(String directoryPath) {
        List<String> extractedFolders = new ArrayList<>();
        File directory = new File(directoryPath);
        File[] zipFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".zip"));
        byte[] buffer = new byte[1024];

        if (zipFiles == null) {
            System.err.println("No ZIP files found in: " + directoryPath);
            return extractedFolders;
        }

        for (File zipFile : zipFiles) {
            String studentFolder = zipFile.getName().replace(".zip", "");
            File outputDir = new File(directory, studentFolder);
            outputDir.mkdirs();

            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry zipEntry;

                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    File outputFile = new File(outputDir, new File(zipEntry.getName()).getName());


                    if (zipEntry.isDirectory()) {
                        outputFile.mkdirs();
                    } else {
                        outputFile.getParentFile().mkdirs();
                        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                            int length;
                            while ((length = zipInputStream.read(buffer)) > 0) {
                                fileOutputStream.write(buffer, 0, length);
                            }
                        }
                    }
                }

                extractedFolders.add(studentFolder);

            } catch (IOException e) {
                System.err.println("Error extracting " + zipFile.getName());
                e.printStackTrace();
            }
        }

        System.out.println("Extracted folders: " + extractedFolders);
        return extractedFolders;
    }
}