package com.example.directoriesManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryManager {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryManager.class);
    private static final Path OUTPUT_DIR = Paths.get("src/main/resources/output").toAbsolutePath();
    private static final Path DEPARTMENTS_DIR = Paths.get("src/main/resources/output/departaments").toAbsolutePath();
    private static final Path LOG_PACKAGE = OUTPUT_DIR.resolve("logs").toAbsolutePath().normalize();

    private DirectoryManager() {
    }

    public static void initializeDirectories() {
        try {
            if (!Files.exists(OUTPUT_DIR)) {
                Files.createDirectories(OUTPUT_DIR);
            }
            if (!Files.exists(DEPARTMENTS_DIR)) {
                Files.createDirectories(DEPARTMENTS_DIR);
            }
            if (!Files.exists(LOG_PACKAGE)) {
                Files.createDirectories(LOG_PACKAGE);
            }
        } catch (IOException e) {
            logger.error("Failed to create directories: {}", e.getMessage());
            throw new RuntimeException("Unable to initialize directories: " + e.getMessage(), e);
        }
    }

    public static Path getOUTPUT_DIR() {
        return OUTPUT_DIR;
    }

    public static Path getLogPackage() {
        return LOG_PACKAGE;
    }

    public static Path getDepartmentsDir() {
        return DEPARTMENTS_DIR;
    }
}