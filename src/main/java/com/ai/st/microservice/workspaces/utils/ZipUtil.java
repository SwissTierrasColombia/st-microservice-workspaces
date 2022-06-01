package com.ai.st.microservice.workspaces.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.ai.st.microservice.workspaces.services.tracing.SCMTracing;
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
            String messageError = String.format("Error descomprimiendo el archivo %s : %s", filePathZip,
                    e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
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
            String messageError = String.format("Error descomprimiendo el archivo %s : %s", filePathZip,
                    e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
        }

        return extensions;

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
            String messageError = String.format("Error descomprimiendo el archivo %s : %s", filePathZip,
                    e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                String messageError = String.format("Error cerrando archivo zip %s : %s", filePathZip, e.getMessage());
                SCMTracing.sendError(messageError);
                log.error(messageError);
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
            String messageError = String.format("Error comprimiendo el archivo %s : %s", zipName, e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
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

        } catch (Exception e) {
            String messageError = String.format("Error comprimiendo el archivo %s : %s", zipName, e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
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
            String messageError = String.format("Error comprobando si el archivo zip tiene un archivo GDB : %s",
                    e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
        }

        return fileFound;
    }

}
