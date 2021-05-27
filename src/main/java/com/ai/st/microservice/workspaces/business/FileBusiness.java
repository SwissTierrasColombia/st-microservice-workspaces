package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.workspaces.utils.ZipUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class FileBusiness {

    @Value("${st.temporalDirectory}")
    private String stTemporalDirectory;

    @Value("${st.filesDirectory}")
    private String stFilesDirectory;

    private final static Logger log = LoggerFactory.getLogger(FileBusiness.class);

    public String loadFileToSystem(MultipartFile file, String fileName) {

        try {

            if (fileName == null) {
                String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
                fileName = RandomStringUtils.random(14, true, false) + "." + fileExtension;
            }

            String temporalFile = stTemporalDirectory + File.separatorChar + StringUtils.cleanPath(fileName);
            FileUtils.writeByteArrayToFile(new File(temporalFile), file.getBytes());
            return temporalFile;
        } catch (IOException e) {
            log.error("Error saving file: " + e.getMessage());
        }

        return null;
    }

    public void deleteFile(String path) {

        try {
            FileUtils.forceDelete(FileUtils.getFile(path));
        } catch (Exception e) {
            log.error("It has not been possible delete the file: " + e.getMessage());
        }

    }

    public String saveFileToSystem(MultipartFile file, String namespace, Boolean zip) {

        try {

            String pathFile;

            String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
            String fileName = RandomStringUtils.random(20, true, false) + "." + fileExtension;

            namespace = stFilesDirectory + namespace;

            if (zip) {
                String zipName = RandomStringUtils.random(20, true, false);
                pathFile = ZipUtil.zipping(file, zipName, fileName, namespace);
            } else {
                pathFile = namespace + File.separatorChar + StringUtils.cleanPath(fileName);
                FileUtils.writeByteArrayToFile(new File(pathFile), file.getBytes());
            }

            return pathFile;

        } catch (IOException e) {
            log.error("Error saving file: " + e.getMessage());
        }

        return null;
    }

}
