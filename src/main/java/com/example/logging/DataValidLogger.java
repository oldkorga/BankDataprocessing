package com.example.logging;

import com.example.directoriesManager.DirectoryManager;
import com.example.enums.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DataValidLogger implements DataValidError {
    private static final Logger logger = LoggerFactory.getLogger(DataValidLogger.class);
    private static final Path detailLog = DirectoryManager.getLogPackage().resolve(AppConstants.ErrorDetail_Path.getValue());

    @Override
    public void logDataValid(String reason) {
        try {
            String detailEntry = String.format("[ERROR] Reason: %s", reason);
            writeToDetailLog(detailEntry);
        } catch (IOException e) {
            logger.error("Failed to write to detailLog: {}", e.getMessage());
        }
    }

    public void logDataValid(String format, Object... args) {
        try {
            Path parentDir = detailLog.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                System.out.println("Created directory: " + parentDir.toAbsolutePath());
            }
            String detailEntry = String.format("[ERROR] Reason: " + format, args) + System.lineSeparator();
            System.out.println("Attempting to write to: " + detailLog.toAbsolutePath() + ", Entry: " + detailEntry);
            Files.writeString(detailLog, detailEntry,
                    Files.exists(detailLog) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            logger.error("Failed to write to detailLog: {}", e.getMessage());
        }
    }

    private void writeToDetailLog(String detailEntry) throws IOException {
        Path parentDir = detailLog.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            System.out.println("Created directory: " + parentDir.toAbsolutePath());
        }
        Files.writeString(detailLog, detailEntry,
                Files.exists(detailLog) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
    }
}