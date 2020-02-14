package com.ai.st.microservice.workspaces.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {

	private final static Logger log = LoggerFactory.getLogger(ZipUtil.class);

	public static boolean zipContainsFile(String filePathZip, List<String> extensionsToSearch) {

		Boolean fileFound = false;

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

}
