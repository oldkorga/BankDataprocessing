package com.example.logging;

import com.example.directoriesManager.DirectoryManager;
import com.example.enums.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class DataValidLogger implements DataValidError {
    private static final Logger logger = LoggerFactory.getLogger(DataValidLogger.class);
    private static final Path detailLog = DirectoryManager.getLogPackage().resolve(AppConstants.ErrorDetail_Path.getValue());

    @Override
    public void logDataValidation(String reason) {
        try {
            String detailEntry = String.format("[ERROR] Reason: %s", reason);
            LogFileManager.writeToLog(detailLog, detailEntry);
        } catch (IOException e) {
            logger.error("Failed to write to detailLog: {}", e.getMessage());
        }
    }

    public void logDataValidation(String format, Object... args) {
        try {
            String detailEntry = String.format("[ERROR] Reason: " + format, args) + System.lineSeparator();
            LogFileManager.writeToLog(detailLog, detailEntry);
        } catch (IOException e) {
            logger.error("Failed to write to detailLog: {}", e.getMessage());
        }
    }
}