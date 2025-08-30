package com.example.config;

import com.example.enums.AppConstants;
import com.example.logging.DataValidLogger;
import java.util.Objects;
import java.util.Optional;

public class ApplicationConfig {
    protected static DataValidLogger errorDataLogger = new DataValidLogger(); // Не финальное для тестов

    private final String sortBy;
    private final String sortOrder;
    private final boolean stat;
    private String output;
    private final String outputPath;

    protected ApplicationConfig(String sortBy, String sortOrder, boolean stat, String output, String outputPath) {
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.stat = stat;
        this.output = (output != null) ? output : AppConstants.DEFAULT_OUTPUT.getValue();
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
            if (option.isPresent()) {
                String value = option.get().extractValue(trimmedArg);
                option.get().apply(builder, value);
            } else {
                errorDataLogger.logDataValidation("Ignoring unknown parameter: '%s'", trimmedArg);
            }
        }
        return builder.build().logSortIssues();
    }

    private ApplicationConfig logSortIssues() {
        if (sortBy != null && sortOrder == null) {
            errorDataLogger.logDataValidation("Order parameter is missing for sortBy '%s', valid parameters: name, salary. Execution will proceed without sorting.", sortBy);
        } else if (sortOrder != null && sortBy == null) {
            errorDataLogger.logDataValidation("Sort parameter is missing for sortOrder '%s', valid parameters: desc, asc. Execution will proceed without sorting.", sortOrder);
        }
        return this;
    }

    public void validate() {
        if (!AppConstants.OUTPUT_FILE.getValue().equals(output) && !AppConstants.DEFAULT_OUTPUT.getValue().equals(output)) {
            errorDataLogger.logDataValidation("Invalid output value '%s'. Switching to console output for statistics.", output);
            this.output = AppConstants.DEFAULT_OUTPUT.getValue(); // Явное обновление поля
        }
        if (AppConstants.OUTPUT_FILE.getValue().equals(output)) {
            if (outputPath == null) {
                errorDataLogger.logDataValidation("Output path is missing for output=file. Switching to console output for statistics.");
                this.output = AppConstants.DEFAULT_OUTPUT.getValue(); // Явное обновление поля
            } else if (outputPath.trim().isEmpty()) {
                errorDataLogger.logDataValidation("Output path is empty for output=file. Switching to console output for statistics.");
                this.output = AppConstants.DEFAULT_OUTPUT.getValue(); // Явное обновление поля
            } else if (!outputPath.matches("[a-zA-Z0-9_.-/\\\\]+")) {
                errorDataLogger.logDataValidation("Invalid output path format: '%s'. Switching to console output for statistics.", outputPath);
                this.output = AppConstants.DEFAULT_OUTPUT.getValue(); // Явное обновление поля
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationConfig that = (ApplicationConfig) o;
        return stat == that.stat && Objects.equals(sortBy, that.sortBy) && Objects.equals(sortOrder, that.sortOrder) && Objects.equals(output, that.output) && Objects.equals(outputPath, that.outputPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sortBy, sortOrder, stat, output, outputPath);
    }

    @Override
    public String toString() {
        return "ApplicationConfig{" + "sortBy='" + sortBy + '\'' + ", sortOrder='" + sortOrder + '\'' + ", stat=" + stat + ", output='" + output + '\'' + ", outputPath='" + outputPath + '\'' + '}';
    }

    public static class ConfigBuilder {
        private String sortBy;
        private String sortOrder;
        private boolean stat = false;
        private String output = "console";
        private String outputPath;

        public ConfigBuilder setSortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public ConfigBuilder setSortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public ConfigBuilder setStat(boolean stat) {
            this.stat = stat;
            return this;
        }

        public ConfigBuilder setOutput(String output) {
            this.output = output;
            return this;
        }

        public ConfigBuilder setOutputPath(String outputPath) {
            this.outputPath = outputPath;
            return this;
        }

        public ApplicationConfig build() {
            return new ApplicationConfig(sortBy, sortOrder, stat, output, outputPath);
        }
    }
}