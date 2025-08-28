package com.example.config;

import com.example.enums.AppConstants;
import com.example.logging.DataValidLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public enum CliOption {

    SORT("--sort", "-s", "sort", true) {
        @Override
        public void apply(ConfigBuilder builder, String value) {
            if (value != null && (AppConstants.SORT_NAME.getValue().equals(value) || AppConstants.SORT_SALARY.getValue().equals(value))) {
                builder.setSortBy(value);
            } else if (value != null) {
                System.out.println("Logging invalid sortBy: " + value);
                errorDataLogger.logDataValid("Ignoring invalid sortBy value '%s'. Execution will proceed without sorting.", value);
            }
        }
    },
    ORDER("--order", null, "order", true) {
        @Override
        public void apply(ConfigBuilder builder, String value) {
            if (value != null && (AppConstants.ORDER_ASC.getValue().equals(value) || AppConstants.ORDER_DESC.getValue().equals(value))) {
                builder.setSortOrder(value);
            } else if (value != null) {
                System.out.println("Logging invalid sortOrder: " + value);
                errorDataLogger.logDataValid("Ignoring invalid sortOrder value '%s'. Execution will proceed without sorting.", value);
            }
        }
    },

    STAT("--stat", null, "stat", false) {
        @Override
        public void apply(ConfigBuilder builder, String value) {
            builder.setStat(true); // Не требует значения
        }
    },

    OUTPUT("--output", "-o", "output", true) {
        @Override
        public void apply(ConfigBuilder builder, String value) {
            if (value != null && (AppConstants.DEFAULT_OUTPUT.getValue().equals(value) || AppConstants.OUTPUT_FILE.getValue().equals(value))) {
                builder.setOutput(value);
            } else {
                throw new IllegalArgumentException("Output must be 'console' or 'file', got: " + value);
            }
        }
    },

    PATH("--path", null, "path", true) {
        @Override
        public void apply(ConfigBuilder builder, String value) {
            if (value != null && !value.trim().isEmpty()) {
                builder.setOutputPath(value);
            } else {
                throw new IllegalArgumentException("Path must not be empty");
            }
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(CliOption.class);
    private static final DataValidLogger errorDataLogger = new DataValidLogger();

    private final String longName;
    private final String shortName;
    private final String key;
    private final boolean hasValue;

    CliOption(String longName, String shortName, String key, boolean hasValue) {
        this.longName = longName;
        this.shortName = shortName;
        this.key = key;
        this.hasValue = hasValue;
    }

    public abstract void apply(ConfigBuilder builder, String value);

    public static Optional<CliOption> fromString(String arg) {
        return Arrays.stream(values())
                .filter(option -> arg.equals(option.longName) ||
                        (option.shortName != null && arg.startsWith(option.shortName + "=")) ||
                        arg.startsWith(option.longName + "="))
                .findFirst();
    }

    public String extractValue(String arg) {
        if (!hasValue) return null;

        if (arg.contains("=")) {
            return arg.split("=", 2)[1].trim();
        }
        return null;
    }
}