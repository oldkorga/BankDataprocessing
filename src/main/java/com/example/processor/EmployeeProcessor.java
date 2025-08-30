package com.example.processor;

import com.example.WritingDepartmentFiles.DepartmentFilesWriter;
import com.example.config.ApplicationConfig;
import com.example.enums.AppConstants;
import com.example.logging.CriticalErrorLogger;
import com.example.logging.DataValidError;
import com.example.logging.DataValidLogger;
import com.example.logging.FileErrorLogger;
import com.example.sorting.EmployeeDataSorter;
import com.example.statistic.DepartmentStatisticCreator;
import com.example.filereader.FileReader;
import com.example.directoriesManager.DirectoryManager;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class EmployeeProcessor implements DataProcessor {
    private static final Path OUTPUT = DirectoryManager.getOUTPUT_DIR().toAbsolutePath();
    private final EmployeeDataSorter sorter = new EmployeeDataSorter();
    private final DepartmentFilesWriter writer = new DepartmentFilesWriter();
    private final DepartmentStatisticCreator statisticCreator = new DepartmentStatisticCreator();
    private static final FileErrorLogger errorLogger = new FileErrorLogger();
    private static final CriticalErrorLogger criticalLogger = new CriticalErrorLogger();
    private static final DataValidLogger errorDataLogger = new DataValidLogger();

    public void processFiles(String[] args) throws IOException {
        ApplicationConfig config = ApplicationConfig.fromArgs(args);
        config.validate();

        String sortBy = config.getSortBy();
        String sortOrder = config.getSortOrder();
        boolean stat = config.isStat();
        String output = config.getOutput();
        String outputPath = config.getOutputPath();

        Map<String, List<String[]>> departmentData = new HashMap<>();
        Map<String, String> managerDepartments = new HashMap<>();
        List<String[]> pendingEmployees = new ArrayList<>();

        try {
            processData(managerDepartments, departmentData, pendingEmployees);

            for (Map.Entry<String, List<String[]>> entry : departmentData.entrySet()) {
                String department = entry.getKey();
                List<String[]> data = entry.getValue();
                sortData(data, sortBy, sortOrder);
                writeDepartmentFile(department, data);
            }

            if (stat) {
                generateStat(departmentData, output, outputPath);
            }
        } catch (IOException e) {
            criticalLogger.logCriticalError("Critical error processing files in directory " + OUTPUT + ": " + e.getMessage() + ". Execution terminated.");
            throw e;
        }
    }

    @Override
    public void processData(Map<String, String> managerDepartments, Map<String, List<String[]>> departmentData, List<String[]> pendingEmployees) throws IOException {
        Path currentDir = Paths.get(".").toAbsolutePath();
        List<String> allLines = FileReader.readSbFiles(currentDir);
        if (allLines == null || allLines.isEmpty()) {
            errorLogger.logError("No data found in directory: " + currentDir.toAbsolutePath(), "Empty input data");
            return;
        }

        allLines.sort(Comparator.comparing(line -> {
            String[] fields = FileReader.parseLine(line);
            return fields != null && fields.length == 5 && AppConstants.MANAGER_ROLE.getValue().equals(fields[0].trim()) ? 0 : 1;
        }));

        for (String line : allLines) {
            String[] fields = parseLine(line);
            if (!isValidFields(fields)) {
                errorLogger.logError(line, "Invalid field count or null fields");
                continue;
            }

            String role = fields[0].trim();
            String managerId = fields[1].trim();
            String employeeManagerId = fields[4].trim();

            if (AppConstants.MANAGER_ROLE.getValue().equals(role)) {
                processManager(fields, managerId, managerDepartments, departmentData, line);
            } else if (AppConstants.EMPLOYEE_ROLE.getValue().equals(role)) {
                processEmployee(fields, employeeManagerId, managerDepartments, departmentData, pendingEmployees, line);
            } else {
                errorLogger.logError(line, "Unknown role: " + role);
            }
        }
    }

    private String[] parseLine(String line) {
        return FileReader.parseLine(line);
    }

    private boolean isValidFields(String[] fields) {
        return fields != null && fields.length == 5;
    }

    private boolean validateRecord(String[] fields, String line) {
        if (fields == null || fields.length != 5) {
            return false;
        }

        String role = fields[0].trim();
        String id = fields[1].trim();
        String name = fields[2].trim();
        String salaryStr = fields[3].trim();
        String departmentOrManagerId = fields[4].trim();

        if (!AppConstants.MANAGER_ROLE.getValue().equals(role) && !AppConstants.EMPLOYEE_ROLE.getValue().equals(role)) {
            errorLogger.logError(line, "Unknown role: " + role);
            return false;
        }

        if (id.isEmpty()) {
            errorLogger.logError(line, "Empty ID field");
            return false;
        }

        if (name.isEmpty()) {
            errorLogger.logError(line, "Empty name field");
            return false;
        }

        if (salaryStr.isEmpty()) {
            errorLogger.logError(line, "Empty salary field");
            return false;
        }
        try {
            double salary = Double.parseDouble(salaryStr);
            if (salary <= 0) {
                errorLogger.logError(line, "Non-positive salary: " + salaryStr);
                return false;
            }
        } catch (NumberFormatException e) {
            errorLogger.logError(line, "Invalid salary format: " + salaryStr);
            return false;
        }

        if (AppConstants.MANAGER_ROLE.getValue().equals(role) && departmentOrManagerId.isEmpty()) {
            errorLogger.logError(line, "Empty department field for manager");
            return false;
        }

        return true;
    }

    private void processManager(String[] fields, String managerId, Map<String, String> managerDepartments, Map<String, List<String[]>> departmentData, String line) throws IOException {
        if (!validateRecord(fields, line)) {
            return;
        }

        if (managerDepartments.containsKey(managerId)) {
            errorLogger.logError(line, "Duplicate manager ID: " + managerId);
            return;
        }

        String department = fields[4].trim();
        managerDepartments.put(managerId, department);
        departmentData.computeIfAbsent(department, k -> new ArrayList<>()).add(fields);
    }

    private void processEmployee(String[] fields, String employeeManagerId, Map<String, String> managerDepartments, Map<String, List<String[]>> departmentData, List<String[]> pendingEmployees, String line) throws IOException {
        if (!validateRecord(fields, line)) {
            return;
        }

        String salaryStr = fields[3].trim();
        try {
            if (managerDepartments.containsKey(employeeManagerId)) {
                String department = managerDepartments.get(employeeManagerId);
                departmentData.computeIfAbsent(department, k -> new ArrayList<>()).add(fields);
            } else {
                pendingEmployees.add(fields);
            }
        } catch (NumberFormatException e) {
            errorLogger.logError(line, "Invalid salary format: " + salaryStr); // Дублирование исключено, так как уже проверено
        }
    }

    protected void sortData(List<String[]> data, String sortBy, String order) {
        sorter.sortData(data, sortBy, order);
    }

    protected void writeDepartmentFile(String department, List<String[]> data) throws IOException {
        try {
            writer.writeDepartmentFile(department, data);
        } catch (IOException e) {
            errorDataLogger.logDataValidation("Failed to write department file for " + department + ": " + e.getMessage() + ". Proceeding with partial execution.");
        }
    }

    protected void generateStat(Map<String, List<String[]>> departmentData, String output, String outputPath) throws IOException {
        try {
            statisticCreator.createStatistic(departmentData, output, outputPath);
        } catch (IOException e) {
            errorDataLogger.logDataValidation("Failed to generate statistics for output " + (outputPath != null ? outputPath : "default") + ": " + e.getMessage() + ". Proceeding with partial execution.");
        }
    }
}