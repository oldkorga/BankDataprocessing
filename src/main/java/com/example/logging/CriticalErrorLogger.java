package com.example.logging;

import com.example.directoriesManager.DirectoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.enums.AppConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CriticalErrorLogger {
    private static final Logger logger = LoggerFactory.getLogger(CriticalErrorLogger.class);
    private static final Path detailLog = DirectoryManager.getLogPackage().resolve(AppConstants.ErrorDetail_Path.getValue());


    public void logCriticalError(String reason) {
        try {
            String detailEntry = String.format("[CRITICAL ERROR] Reason: %s ", reason);
            Files.writeString(detailLog, detailEntry, Files.exists(detailLog) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            logger.error("Failed to log critical error: " + e.getMessage());
        }
    }
}