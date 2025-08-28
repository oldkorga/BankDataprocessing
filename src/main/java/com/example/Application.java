package com.example;

import com.example.processor.EmployeeProcessor;
import com.example.directoriesManager.DirectoryManager;
import com.example.logging.CriticalErrorLogger;

public class Application {
    private static final CriticalErrorLogger criticalLogger = new CriticalErrorLogger();

    public static void main(String[] args) {
        try {
            DirectoryManager.initializeDirectories();

            EmployeeProcessor processor = new EmployeeProcessor();
            processor.processFiles(args);
        } catch (Exception e) {
            criticalLogger.logCriticalError("Application failed due to critical error: " + e.getMessage() + ". Execution terminated.");
        }
    }
}