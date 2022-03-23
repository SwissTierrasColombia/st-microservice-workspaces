package com.ai.st.microservice.workspaces.utils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class ZipUtil {

    private final static Logger log = LoggerFactory.getLogger(ZipUtil.class);

    public static boolean zipContainsFile(String filePathZip, List<String> extensionsToSearch) {

        boolean fileFound = false;

        try {

            ZipFile zipFile = new ZipFile(filePathZip);

            for (String extension : extensionsToSearch) {

                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (FilenameUtils.getExtension(entry.getName()).equalsIgnoreCase(extension)) {
                        fileFound = true;
                        break;
                    }
                }

                if (fileFound) {
                    break;
                }

            }

            zipFile.close();
        } catch (IOException e) {
            fileFound = false;
            log.error("Error unzipping archive: " + e.getMessage());
        }

        return fileFound;
    }

    public static List<String> getExtensionsFromZip(String filePathZip) {

        List<String> extensions = new ArrayList<>();

        try {

            ZipFile zipFile = new ZipFile(filePathZip);

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                extensions.add(FilenameUtils.getExtension(entry.getName()));
            }

            zipFile.close();
        } catch (IOException e) {
            log.error("Error unzipping archive: " + e.getMessage());
        }

        return extensions;

    }

    public static boolean unzipping(String filePathZip) {

        try {

            ZipFile zipFile = new ZipFile(filePathZip);

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream stream = zipFile.getInputStream(entry);

                String fileEntryOutName = FilenameUtils.getFullPath(filePathZip) + entry.getName();
                FileOutputStream outputStream = new FileOutputStream(new File(fileEntryOutName));

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = stream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                stream.close();
                outputStream.close();

            }

            zipFile.close();

            return true;
        } catch (IOException e) {
            log.error("Error unzipping archive: " + e.getMessage());
        }

        return false;
    }

    public static boolean zipMustContains(String filePathZip, List<String> extensionsToSearch) {
        ZipFile zipFile = null;
        try {

            zipFile = new ZipFile(filePathZip);

            for (String extension : extensionsToSearch) {

                boolean fileFound = false;

                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (FilenameUtils.getExtension(entry.getName()).equalsIgnoreCase(extension)) {
                        fileFound = true;
                        break;
                    }
                }

                if (!fileFound) {
                    return false;
                }

            }

        } catch (IOException e) {
            log.error("Error unzipping archive: " + e.getMessage());
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public static String zipping(MultipartFile file, String zipName, String fileName, String namespace) {

        try {

            String path = namespace + File.separatorChar + zipName + ".zip";

            new File(namespace).mkdirs();
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            ZipOutputStream o = new ZipOutputStream(new FileOutputStream(f));
            ZipEntry e = new ZipEntry(fileName);
            o.putNextEntry(e);
            byte[] data = file.getBytes();
            o.write(data, 0, data.length);
            o.closeEntry();
            o.close();

            return path;

        } catch (IOException e) {
            log.error("Error zipping archive: " + e.getMessage());
        }

        return null;
    }

    public static String zipping(List<File> files, String zipName, String namespace) {

        try {

            String path = namespace + File.separatorChar + zipName + ".zip";

            new File(namespace).mkdirs();
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }

            byte[] buffer = new byte[1024];

            FileOutputStream fos = new FileOutputStream(f);
            ZipOutputStream o = new ZipOutputStream(fos);

            for (File file : files) {
                ZipEntry e = new ZipEntry(file.getName());
                o.putNextEntry(e);
                FileInputStream in = new FileInputStream(file.getAbsolutePath());
                int len;
                while ((len = in.read(buffer)) > 0) {
                    o.write(buffer, 0, len);
                }
                in.close();
                o.closeEntry();
            }

            o.close();
            fos.close();

            return path;

        } catch (IOException e) {
            log.error("Error zipping archive (I): " + e.getMessage());
        } catch (Exception e) {
            log.error("Error zipping archive (II): " + e.getMessage());
        }

        return null;
    }

    public static boolean hasGDBDatabase(String filePathZip) {

        boolean fileFound = false;

        try {

            ZipFile zipFile = new ZipFile(filePathZip);

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory() && entry.getName().contains(".gdb")) {
                    fileFound = true;
                    break;
                }
            }

            zipFile.close();
        } catch (IOException e) {
            fileFound = false;
            log.error("Error hasGDBDatabase: " + e.getMessage());
        }

        return fileFound;
    }

}
