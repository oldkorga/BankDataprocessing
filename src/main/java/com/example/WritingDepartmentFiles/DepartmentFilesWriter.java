package com.example.WritingDepartmentFiles;

import com.example.logging.DataValidLogger;
import com.example.logging.FileErrorLogger;
import com.example.directoriesManager.DirectoryManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DepartmentFilesWriter implements FilesWriter {
    private static final FileErrorLogger errorLogger = new FileErrorLogger();
    private static final DataValidLogger errorDataLogger = new DataValidLogger();
    private static final Path OUTPUT_DIR = DirectoryManager.getDepartmentsDir();



    @Override
    public void writeDepartmentFile(String department, List<String[]> data) throws IOException {
        if (data == null || department == null) {
            String reason = "Invalid input: " + (data == null ? "data is null" : "department is null");
            errorDataLogger.logDataValid(reason);
            return;
        }
        if (data.isEmpty()) {
            errorDataLogger.logDataValid("Data is empty for department: " + department);
            return;
        }

        if (data.stream().anyMatch(Objects::isNull)) {
            errorLogger.logError("Data validation", "List contains null elements for department: " + department);
            return;
        }

        String cleanedDepartment = cleanDepartmentName(department);
        if (cleanedDepartment.isEmpty()) {
            errorDataLogger.logDataValid("Empty department name: " + department);
            return;
        }

        Path outputFile = OUTPUT_DIR.resolve(cleanedDepartment + ".sb");
        List<String> lines = convertDataToLines(data);
        writeToFile(outputFile, lines);
    }

    private String cleanDepartmentName(String department) {
        return department.replaceAll("[^a-zA-Z0-9-_.]", "").trim();
    }

    private List<String> convertDataToLines(List<String[]> data) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : data) {
            lines.add(String.join(",", fields).trim());
        }
        return lines;
    }

    private void writeToFile(Path outputFile, List<String> lines) throws IOException {
        try {
            Files.write(outputFile, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            errorDataLogger.logDataValid("Failed to write to file " + outputFile + ": " + e.getMessage() + ". Proceeding with partial execution.");
            throw e;
        }
    }
}