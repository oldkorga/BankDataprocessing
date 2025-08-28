package com.example.logging;

import com.example.enums.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import com.example.directoriesManager.DirectoryManager;

public class FileErrorLogger implements DataError {
    private static final Logger logger = LoggerFactory.getLogger(FileErrorLogger.class);
    private static final Path errorLog;
    private static final Path detailLog;
    private Boolean isFirst = true;

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
            cleaningLogFiles(errorLog, detailLog);

            Files.writeString(errorLog, line + "\n", Files.exists(errorLog) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);

            String detailEntry = String.format("[ERROR] Reason: %s - Line: %s%n", reason, line);
            Files.writeString(detailLog, detailEntry, Files.exists(detailLog) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);

        } catch (IOException e) {
            logger.error("Failed to write error log: {}", e.getMessage());
        }
    }

    private void cleaningLogFiles(Path errorLog, Path detailLog) {
        if (isFirst) {
            try {
                if (Files.exists(errorLog)) {
                    Files.delete(errorLog);
                }
                if (Files.exists(detailLog)) {
                    Files.delete(detailLog);
                }
            } catch (IOException e) {
                logger.warn("Failed to clear log files: {}", e.getMessage());
            } finally {
                isFirst = false;
            }
        }
    }
}