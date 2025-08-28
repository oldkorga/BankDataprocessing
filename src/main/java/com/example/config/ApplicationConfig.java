package com.example.config;

import com.example.enums.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.logging.DataValidLogger;

import java.util.Objects;
import java.util.Optional;

public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final DataValidLogger errorDataLogger = new DataValidLogger();

    private final String sortBy;
    private final String sortOrder;
    private final boolean stat;
    private final String output;
    private final String outputPath;

    ApplicationConfig(String sortBy, String sortOrder, boolean stat, String output, String outputPath) {
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.stat = stat;
        this.output = output != null ? output : AppConstants.DEFAULT_OUTPUT.getValue();
        this.outputPath = outputPath;
    }

    public String getSortBy() { return sortBy; }
    public String getSortOrder() { return sortOrder; }
    public boolean isStat() { return stat; }
    public String getOutput() { return output; }
    public String getOutputPath() { return outputPath; }

    public static ApplicationConfig fromArgs(String[] args) {
        ConfigBuilder builder = new ConfigBuilder();
        for (String arg : args) {
            String trimmedArg = arg.trim();
            Optional<CliOption> option = CliOption.fromString(trimmedArg);
            if (option.isEmpty()) {
                errorDataLogger.logDataValid("Ignoring unknown parameter: {}", trimmedArg);
                continue;
            }
            String value = option.get().extractValue(trimmedArg);
            option.get().apply(builder, value);
        }
        ApplicationConfig config = builder.build();
        config.logSortIssues();
        return config;
    }

    private void logSortIssues() {
        if (sortBy != null && sortOrder == null) {
            System.out.println("Logging missing order for sortBy: " + sortBy);
            errorDataLogger.logDataValid("Order parameter is missing for sortBy '%s'. Execution will proceed without sorting.", sortBy);
        } else if (sortOrder != null && sortBy == null) {
            System.out.println("Logging missing sort for sortOrder: " + sortOrder);
            errorDataLogger.logDataValid("Sort parameter is missing for sortOrder '%s'. Execution will proceed without sorting.", sortOrder);
        }
    }

    public void validate() {
        // Проверка совместимости output и path
        if (AppConstants.OUTPUT_FILE.getValue().equals(output) && (outputPath == null || outputPath.trim().isEmpty())) {
            throw new IllegalArgumentException("Output path must be specified with --output=file");
        }
        // Проверка допустимого значения output
        if (!AppConstants.OUTPUT_FILE.getValue().equals(output) && !AppConstants.DEFAULT_OUTPUT.getValue().equals(output)) {
            throw new IllegalArgumentException("Output must be 'file' or 'console', got: " + output);
        }
        // Проверка формата outputPath
        if (outputPath != null && !outputPath.matches("[a-zA-Z0-9_.-/]+")) {
            throw new IllegalArgumentException("Invalid output path format: " + outputPath);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationConfig that = (ApplicationConfig) o;
        return stat == that.stat &&
                Objects.equals(sortBy, that.sortBy) &&
                Objects.equals(sortOrder, that.sortOrder) &&
                Objects.equals(output, that.output) &&
                Objects.equals(outputPath, that.outputPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sortBy, sortOrder, stat, output, outputPath);
    }

    @Override
    public String toString() {
        return "ApplicationConfig{" +
                "sortBy='" + sortBy + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                ", stat=" + stat +
                ", output='" + output + '\'' +
                ", outputPath='" + outputPath + '\'' +
                '}';
    }
}