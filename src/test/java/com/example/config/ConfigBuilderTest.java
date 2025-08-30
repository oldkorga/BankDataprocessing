package com.example.config;

import com.example.enums.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigBuilderTest {

    private ConfigBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ConfigBuilder();
    }

    @Test
    void testDefaultValues() {
        ApplicationConfig config = builder.build();

        assertNull(config.getSortBy(), "SortBy should be null by default");
        assertNull(config.getSortOrder(), "SortOrder should be null by default");
        assertFalse(config.isStat(), "Stat should be false by default");
        assertEquals(AppConstants.DEFAULT_OUTPUT.getValue(), config.getOutput(), "Output should be 'console' by default");
        assertNull(config.getOutputPath(), "OutputPath should be null by default");
    }

    @Test
    void testSetAllParameters() {
        ApplicationConfig config = builder
                .setSortBy(AppConstants.SORT_SALARY.getValue())
                .setSortOrder(AppConstants.ORDER_DESC.getValue())
                .setStat(true)
                .setOutput(AppConstants.OUTPUT_FILE.getValue())
                .setOutputPath("/path/to/output")
                .build();

        assertEquals(AppConstants.SORT_SALARY.getValue(), config.getSortBy(), "SortBy should be 'salary'");
        assertEquals(AppConstants.ORDER_DESC.getValue(), config.getSortOrder(), "SortOrder should be 'desc'");
        assertTrue(config.isStat(), "Stat should be true");
        assertEquals(AppConstants.OUTPUT_FILE.getValue(), config.getOutput(), "Output should be 'file'");
        assertEquals("/path/to/output", config.getOutputPath(), "OutputPath should be '/path/to/output'");
    }

    @Test
    void testSetNullValues() {
        ApplicationConfig config = builder
                .setSortBy(null)
                .setSortOrder(null)
                .setOutput(null)
                .setOutputPath(null)
                .build();

        assertNull(config.getSortBy(), "SortBy should accept null");
        assertNull(config.getSortOrder(), "SortOrder should accept null");
        assertEquals(AppConstants.DEFAULT_OUTPUT.getValue(), config.getOutput(), "Output should revert to default 'console' if null");
        assertNull(config.getOutputPath(), "OutputPath should accept null");
    }

    @Test
    void testChainedCalls() {
        ApplicationConfig config = builder
                .setSortBy(AppConstants.SORT_NAME.getValue())
                .setSortOrder(AppConstants.ORDER_ASC.getValue())
                .setStat(true)
                .setOutput(AppConstants.OUTPUT_FILE.getValue())
                .setOutputPath("/another/path")
                .build();

        assertSame(builder, builder.setSortBy(AppConstants.SORT_NAME.getValue()), "Chained call should return the same builder instance");

        assertEquals(AppConstants.SORT_NAME.getValue(), config.getSortBy(), "SortBy should be 'name'");
        assertEquals(AppConstants.ORDER_ASC.getValue(), config.getSortOrder(), "SortOrder should be 'asc'");
        assertTrue(config.isStat(), "Stat should be true");
        assertEquals(AppConstants.OUTPUT_FILE.getValue(), config.getOutput(), "Output should be 'file'");
        assertEquals("/another/path", config.getOutputPath(), "OutputPath should be '/another/path'");
    }

    @Test
    void testMultipleBuildCalls() {
        ApplicationConfig config1 = builder.setSortBy(AppConstants.SORT_SALARY.getValue()).build();
        ApplicationConfig config2 = builder.setSortOrder(AppConstants.ORDER_ASC.getValue()).build();

        assertEquals(AppConstants.SORT_SALARY.getValue(), config1.getSortBy(), "First build should reflect setSortBy");
        assertNull(config1.getSortOrder(), "First build should not reflect later setSortOrder");
        assertEquals(AppConstants.ORDER_ASC.getValue(), config2.getSortOrder(), "Second build should reflect setSortOrder");
        assertEquals(AppConstants.SORT_SALARY.getValue(), config2.getSortBy(), "Second build should retain previous sortBy");
    }
}