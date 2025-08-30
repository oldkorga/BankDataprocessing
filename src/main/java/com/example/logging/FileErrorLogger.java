package com.example.logging;

import com.example.enums.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import com.example.directoriesManager.DirectoryManager;

public class FileErrorLogger implements DataError {
    private static final Logger logger = LoggerFactory.getLogger(FileErrorLogger.class);
    private static final Path errorLog;
    private static final Path detailLog;

    static {
        DirectoryManager.initializeDirectories();
        Path logPackage = DirectoryManager.getLogPackage();
        errorLog = logPackage.resolve(AppConstants.Error_Path.getValue());
        detailLog = logPackage.resolve(AppConstants.ErrorDetail_Path.getValue());
    }

    public FileErrorLogger() {
    }

    @Override
    public void logError(String line, String reason) {
        try {

            LogFileManager.writeToLog(errorLog, line + "\n");

            String detailEntry = String.format("[ERROR] Reason: %s - Line: %s%n", reason, line);
            LogFileManager.writeToLog(detailLog, detailEntry);
        } catch (IOException e) {
            logger.error("Failed to write error log: {}", e.getMessage());
        }
    }
}