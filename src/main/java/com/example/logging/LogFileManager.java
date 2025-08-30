package com.example.logging;

import com.example.directoriesManager.DirectoryManager;
import com.example.enums.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LogFileManager {
    private static final Logger logger = LoggerFactory.getLogger(LogFileManager.class);
    private static boolean isFirstCleanup = true;
    private static final Path errorLog;
    private static final Path detailLog;

    static {
        DirectoryManager.initializeDirectories();
        Path logPackage = DirectoryManager.getLogPackage();
        errorLog = logPackage.resolve(AppConstants.Error_Path.getValue());
        detailLog = DirectoryManager.getLogPackage().resolve(AppConstants.ErrorDetail_Path.getValue());

    }

    public LogFileManager() {
    }

    public static void initializeLogs() {
        if (isFirstCleanup) {
            try {
                if (Files.exists(errorLog)) {
                    Files.delete(errorLog);
                    logger.info("Cleared log file: {}", errorLog.toAbsolutePath());

                }
                if (Files.exists(detailLog)) {
                    Files.delete(detailLog);
                    logger.info("Cleared log file: {}", detailLog.toAbsolutePath());
                }
                isFirstCleanup = false;
            } catch (IOException e) {
                logger.warn("Failed to clear log files: {}", e.getMessage());

            }
        }
    }

    public static void writeToLog(Path logPath, String entry) throws IOException {
        Path parentDir = logPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            logger.info("Created directory: {}", parentDir.toAbsolutePath());
        }
        Files.writeString(logPath, entry + System.lineSeparator(), Files.exists(logPath) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
    }
}