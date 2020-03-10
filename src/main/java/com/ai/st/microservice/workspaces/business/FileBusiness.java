package com.ai.st.microservice.workspaces.business;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileBusiness {

	@Value("${st.temporalDirectory}")
	private String stTemporalDirectory;

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

}
