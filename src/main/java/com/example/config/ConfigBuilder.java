package com.example.config;

public class ConfigBuilder {
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