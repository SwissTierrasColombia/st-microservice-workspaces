package com.ai.st.microservice.workspaces.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTool {

	private final static Logger log = LoggerFactory.getLogger(FileTool.class);

	public static File createSimpleFile(String content, String filename) {

		File file = new File(filename);

		try (Writer writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(content);
		} catch (IOException e) {
			log.error("Error creando archivo: " + e.getMessage());
		}

		return file;
	}

}
