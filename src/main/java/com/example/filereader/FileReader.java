package com.example.filereader;

import com.example.logging.CriticalErrorLogger;
import com.example.logging.DataValidLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileReader {
    private static final DataValidLogger errorDataLogger = new DataValidLogger();
    private static final CriticalErrorLogger criticalLogger = new CriticalErrorLogger();

    public static List<String> readSbFiles(Path directoryPath) throws IOException {
        if (!Files.exists(directoryPath)) {

            criticalLogger.logCriticalError("Critical error: Directory does not exist at: " + directoryPath.toAbsolutePath() + ". Execution terminated.");
            throw new IOException("Directory does not exist: " + directoryPath);
        }

        if (!Files.isDirectory(directoryPath)) {
            criticalLogger.logCriticalError("Critical error: Path is not a directory: " + directoryPath.toAbsolutePath() + ". Execution terminated.");
            throw new IOException("Path is not a directory: " + directoryPath);
        }

        List<String> allLines = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(directoryPath, "*.sb")) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    try {
                        List<String> lines = Files.readAllLines(file);
                        allLines.addAll(lines);
                    } catch (IOException e) {
                        errorDataLogger.logDataValidation("Failed to read file: " + file.toString() + ": " + e.getMessage() + ". Proceeding with partial execution.");
                    }
                }
            }
        } catch (IOException e) {
            criticalLogger.logCriticalError("Critical error walking directory: " + e.getMessage() + ". Execution terminated.");
            throw e;
        }
        return allLines;
    }

    public static String[] parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            errorDataLogger.logDataValidation("Skipping empty or null line");
            return null;
        }

        String[] fields = line.trim().split(",");
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        return fields;
    }
}