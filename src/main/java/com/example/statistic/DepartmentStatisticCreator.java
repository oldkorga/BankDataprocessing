package com.example.statistic;

import com.example.enums.AppConstants;
import com.example.logging.DataValidLogger;
import com.example.logging.FileErrorLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DepartmentStatisticCreator implements StatisticCreator {

    private static final FileErrorLogger errorLogger = new FileErrorLogger();
    private static final DataValidLogger errorDataLogger = new DataValidLogger();

    @Override
    public void createStatistic(Map<String, List<String[]>> departmentData, String output, String outputPath) throws IOException {
        if (departmentData == null || departmentData.isEmpty()) {
            errorLogger.logError(departmentData.toString(), "No department data available for statistic creation");
            return;
        }

        List<Map.Entry<String, List<String[]>>> sortedEntries = new ArrayList<>(departmentData.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());

        List<String> stats = new ArrayList<>();
        stats.add("department,min,max,mid");

        for (Map.Entry<String, List<String[]>> entry : sortedEntries) {
            List<Double> salaries = collectSalaries(entry);
            StatisticResult result = calculateStatistics(salaries, entry.getKey());
            stats.add(String.format("%s,%.2f,%.2f,%.2f", result.department(), result.min(), result.max(), result.mid()));
        }

        writeStatistics(stats, output, outputPath != null ? Path.of(outputPath) : null);
    }

    private List<Double> collectSalaries(Map.Entry<String, List<String[]>> entry) {
        List<Double> salaries = new ArrayList<>();
        String department = entry.getKey();
        List<String[]> values = entry.getValue();

        if (values == null || values.isEmpty()) {
            errorDataLogger.logDataValidation("No data available for department: " + department);
            return salaries;
        }

        for (String[] fields : values) {
            if (isManager(fields)) {
                continue;
            }
            Double salary = parseAndValidateSalary(fields, department);
            if (salary != null) {
                salaries.add(salary);
            }
        }
        return salaries;
    }

    private Double parseAndValidateSalary(String[] fields, String department) {
        String salaryStr = fields[3];
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            errorDataLogger.logDataValidation("Empty salary field for department: " + department);
            return null;
        }

        try {
            double salaryValue = Double.parseDouble(salaryStr.trim());
            return salaryValue;
        } catch (NumberFormatException e) {
            errorLogger.logError(salaryStr, "Invalid salary format for department: " + department + " - " + e.getMessage());
            return null;
        }
    }

    private boolean isManager(String[] fields) {
        return AppConstants.MANAGER_ROLE.getValue().equals(fields[0].trim());
    }

    private StatisticResult calculateStatistics(List<Double> salaries, String department) {
        double min = salaries.isEmpty() ? 0.0 : Collections.min(salaries);
        double max = salaries.isEmpty() ? 0.0 : Collections.max(salaries);
        double sum = salaries.stream().mapToDouble(Double::doubleValue).sum();
        double mid = salaries.isEmpty() ? 0.0 : sum / salaries.size();

        return new StatisticResult(
                department,
                roundSalary(min),
                roundSalary(max),
                roundSalary(mid)
        );
    }

    private double roundSalary(double value) {
        return Math.ceil(value * 100) / 100;
    }

    private void writeStatistics(List<String> stats, String output, Path outputPath) throws IOException {
        if (AppConstants.DEFAULT_OUTPUT.getValue().equals(output)) {
            stats.forEach(System.out::println); // Вывод в консоль при дефолтном значении
            return;
        }
        if (AppConstants.OUTPUT_FILE.getValue().equals(output) && outputPath != null) {
            ensureOutputDirectoryExists(outputPath);
            try {
                Files.write(outputPath, stats, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                errorDataLogger.logDataValidation("Failed to write statistics to file %s: %s. Switching to console output.", outputPath, e.getMessage());
                stats.forEach(System.out::println);
                throw e;
            }
        } else {
            stats.forEach(System.out::println);
        }
    }

    private void ensureOutputDirectoryExists(Path outputPath) throws IOException {
        if (!Files.exists(outputPath.getParent())) {
            Files.createDirectories(outputPath.getParent());
        }
    }

    private static class StatisticResult {
        private final String department;
        private final double min;
        private final double max;
        private final double mid;

        public StatisticResult(String department, double min, double max, double mid) {
            this.department = department;
            this.min = min;
            this.max = max;
            this.mid = mid;
        }

        public String department() { return department; }
        public double min() { return min; }
        public double max() { return max; }
        public double mid() { return mid; }
    }
}