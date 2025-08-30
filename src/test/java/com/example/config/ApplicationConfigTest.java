package com.example.config;

import com.example.enums.AppConstants;
import com.example.logging.DataValidLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ApplicationConfigTest {

    private DataValidLogger originalLogger;
    private DataValidLogger mockLogger;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field field = ApplicationConfig.class.getDeclaredField("errorDataLogger");
        field.setAccessible(true);
        originalLogger = (DataValidLogger) field.get(null);

        mockLogger = Mockito.mock(DataValidLogger.class);
        field.set(null, mockLogger);
    }

    @AfterEach
    void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Field field = ApplicationConfig.class.getDeclaredField("errorDataLogger");
        field.setAccessible(true);
        field.set(null, originalLogger);
    }

    @Test
    void testFromArgsWithMissingSortOrder() {
        String[] args = {"--sort=salary", "--stat"};
        ApplicationConfig config = ApplicationConfig.fromArgs(args);

        assertNotNull(config);
        assertEquals(AppConstants.SORT_SALARY.getValue(), config.getSortBy());
        assertNull(config.getSortOrder());
        assertTrue(config.isStat());
        verify(mockLogger).logDataValidation("Order parameter is missing for sortBy '%s', valid parameters: name, salary. Execution will proceed without sorting.", "salary");
    }

    @Test
    void testFromArgsWithUnknownParameter() {
        String[] args = {"--unknown=param", "--sort=name"};
        ApplicationConfig config = ApplicationConfig.fromArgs(args);

        assertNotNull(config);
        assertEquals(AppConstants.SORT_NAME.getValue(), config.getSortBy());
        assertNull(config.getSortOrder());
        verify(mockLogger).logDataValidation("Ignoring unknown parameter: '%s'", "--unknown=param");
    }

    @Test
    void testFromArgsWithInvalidSortValue() {
        String[] args = {"--sort=invalid", "--stat"};
        ApplicationConfig config = ApplicationConfig.fromArgs(args);

        assertNotNull(config);
        assertNull(config.getSortBy(), "SortBy should be null for invalid sort value");
        assertTrue(config.isStat());
        verify(mockLogger).logDataValidation("Ignoring invalid sort value '{}'. Valid values are: {}", "invalid", Arrays.toString(AppConstants.getValidSortValues()));
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    void testValidateWithMissingOutputPath() {
        String[] args = {"--output=file"};
        ApplicationConfig config = ApplicationConfig.fromArgs(args);

        assertDoesNotThrow(config::validate, "Validation should not throw an exception for missing output path, should switch to console");
        assertEquals(AppConstants.DEFAULT_OUTPUT.getValue(), config.getOutput(), "Output should switch to console due to missing output path");
        verify(mockLogger).logDataValidation("Output path is missing for output=file. Switching to console output for statistics.");
    }

    @Test
    void testValidateWithInvalidOutput() {
        String[] args = {"--output=invalid", "--outputPath=/path"};
        ApplicationConfig config = ApplicationConfig.fromArgs(args);

        assertDoesNotThrow(config::validate, "Validation should not throw an exception for invalid output, should switch to console");
        assertEquals(AppConstants.DEFAULT_OUTPUT.getValue(), config.getOutput(), "Output should switch to console due to invalid output value");
        verify(mockLogger).logDataValidation("Invalid output value '%s'. Switching to console output for statistics.", "invalid");
    }

    @Test
    void testValidateWithInvalidOutputPathFormat() {
        String[] args = {"--output=file", "--path=invalid@path"};
        ApplicationConfig config = ApplicationConfig.fromArgs(args);

        assertDoesNotThrow(config::validate, "Validation should not throw an exception for invalid output path format, should switch to console");
        assertEquals(AppConstants.DEFAULT_OUTPUT.getValue(), config.getOutput(), "Output should switch to console due to invalid output path format");
        verify(mockLogger).logDataValidation("Invalid output path format: '%s'. Switching to console output for statistics.", "invalid@path");
    }

    @Test
    void testEqualsAndHashCode() {
        ApplicationConfig config1 = new ApplicationConfig(AppConstants.SORT_SALARY.getValue(), AppConstants.ORDER_ASC.getValue(), true, AppConstants.OUTPUT_FILE.getValue(), "/path");
        ApplicationConfig config2 = new ApplicationConfig(AppConstants.SORT_SALARY.getValue(), AppConstants.ORDER_ASC.getValue(), true, AppConstants.OUTPUT_FILE.getValue(), "/path");
        ApplicationConfig config3 = new ApplicationConfig(AppConstants.SORT_NAME.getValue(), AppConstants.ORDER_ASC.getValue(), false, AppConstants.DEFAULT_OUTPUT.getValue(), null);

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1, config3);
        assertNotEquals(config1.hashCode(), config3.hashCode());
    }

    @Test
    void testToString() {
        ApplicationConfig config = new ApplicationConfig("salary", "asc", true, "file", "/path");
        String expected = "ApplicationConfig{sortBy='salary', sortOrder='asc', stat=true, output='file', outputPath='/path'}";
        assertEquals(expected, config.toString());
    }

    @Test
    void testFromArgsWithAllValidParameters() {
        String[] args = {"--sort=salary", "--order=desc", "--stat", "--output=file", "--path=/path/to/output"};
        ApplicationConfig config = ApplicationConfig.fromArgs(args);

        assertNotNull(config, "Configuration object should not be null");
        assertEquals(AppConstants.SORT_SALARY.getValue(), config.getSortBy(), "Sort by should be 'salary'");
        assertEquals(AppConstants.ORDER_DESC.getValue(), config.getSortOrder(), "Sort order should be 'desc'");
        assertTrue(config.isStat(), "Stat flag should be true");
        assertEquals(AppConstants.OUTPUT_FILE.getValue(), config.getOutput(), "Output should be 'file'");
        assertEquals("/path/to/output", config.getOutputPath(), "Output path should be '/path/to/output'");
        assertDoesNotThrow(config::validate, "Validation should not throw an exception for valid configuration");
    }
}