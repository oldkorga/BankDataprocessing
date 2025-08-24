package com.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class FileReader {
    private static final Logger logger = LoggerFactory.getLogger(FileReader.class);


    public static List<String> readSbFiles(Path directoryPath) throws IOException {
        if (!Files.exists(directoryPath)) {
            logger.error("Directory does not exist: {}", directoryPath);
            throw new IOException("Directory does not exist: " + directoryPath);
        }
        if (!Files.isDirectory(directoryPath)) {
            logger.error("Path is not a directory: {}", directoryPath);
            throw new IOException("Path is not a directory: " + directoryPath);
        }

        List<String> allLines = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(directoryPath, "*.sb")) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    try {
                        List<String> lines = Files.readAllLines(file);
                        allLines.addAll(lines);
                        logger.info("Successfully read {} lines from file: {}", lines.size(), file.toAbsolutePath());
                    } catch (IOException e) {
                        logger.error("Error reading file {}: {}", file, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error walking directory {}: {}", directoryPath, e.getMessage());
            throw e;
        }
        logger.info("Total lines read: {}", allLines.size());
        return allLines;
    }


    public static String[] parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            logger.warn("Skipping empty or null line");
            return null;
        }

        String[] fields = line.trim().split(",");
        if (fields.length < 3) { // Минимальное количество полей: ID, Name, Salary (для Employee/Manager)
            logger.warn("Skipping line with insufficient fields (less than 3): {}", line);
            return null;
        }


        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        return fields;
    }
}