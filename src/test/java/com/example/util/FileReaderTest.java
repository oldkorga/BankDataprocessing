package com.example.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class FileReaderTest {
    private static final Logger logger = LoggerFactory.getLogger(FileReaderTest.class);
    private static final Path TEST_RESOURCES = Paths.get("src/test/resources");

    @BeforeEach
    void setUp() {
        // Очистка или подготовка тестового окружения, если требуется
        logger.info("Setting up test environment");
    }

    @Test
    void testReadSbFiles_SuccessfulRead() throws IOException {
        // Подготовка
        Path sampleFile = TEST_RESOURCES.resolve("sample.sb");
        Path invalidFile = TEST_RESOURCES.resolve("invalid.sb");
        Files.writeString(sampleFile, "Manager,100,Alice,1000.00,HR\nEmployee,1,John,500.50,100");
        Files.writeString(invalidFile, "Manager,101,Bob,1000.00,IT\nEmployee,2,Alice,600.75,101");

        // Выполнение
        List<String> lines = FileReader.readSbFiles(TEST_RESOURCES);

        // Проверка
        assertNotNull(lines, "Lines should not be null");
        assertFalse(lines.isEmpty(), "Lines should contain at least one entry");
        assertEquals(4, lines.size(), "Should read 4 lines from both sample.sb and invalid.sb");
        assertTrue(lines.contains("Manager,100,Alice,1000.00,HR"), "Should contain manager line from sample.sb");
        assertTrue(lines.contains("Employee,1,John,500.50,100"), "Should contain employee line from sample.sb");
        assertTrue(lines.contains("Manager,101,Bob,1000.00,IT"), "Should contain manager line from invalid.sb");
        assertTrue(lines.contains("Employee,2,Alice,600.75,101"), "Should contain employee line from invalid.sb");
        logger.info("Successfully tested reading {} lines", lines.size());
    }

    @Test
    void testReadSbFiles_NonExistentDirectory() {
        // Подготовка
        Path nonExistentPath = Paths.get("nonexistent/dir");

        // Выполнение и проверка
        IOException exception = assertThrows(IOException.class, () -> FileReader.readSbFiles(nonExistentPath),
                "Should throw IOException for non-existent directory");
        assertTrue(exception.getMessage().contains("Directory does not exist"),
                "Exception message should indicate non-existent directory");
        logger.info("Successfully tested exception for non-existent directory");
    }

    @Test
    void testReadSbFiles_NotADirectory() throws IOException {
        // Подготовка
        Path filePath = TEST_RESOURCES.resolve("sample.sb");
        Files.writeString(filePath, "Test content");

        // Выполнение и проверка
        IOException exception = assertThrows(IOException.class, () -> FileReader.readSbFiles(filePath),
                "Should throw IOException for non-directory path");
        assertTrue(exception.getMessage().contains("Path is not a directory"),
                "Exception message should indicate non-directory path");
        logger.info("Successfully tested exception for non-directory path");
    }

    @Test
    void testParseLine_SuccessfulParse() {
        // Подготовка
        String line = "Employee,1,John,500.50,100";

        // Выполнение
        String[] fields = FileReader.parseLine(line);

        // Проверка
        assertNotNull(fields, "Fields should not be null");
        assertEquals(5, fields.length, "Should parse into 5 fields");
        assertEquals("Employee", fields[0], "First field should be Employee");
        assertEquals("1", fields[1], "Second field should be ID");
        assertEquals("John", fields[2], "Third field should be Name");
        assertEquals("500.50", fields[3], "Fourth field should be Salary");
        assertEquals("100", fields[4], "Fifth field should be ManagerID");
        logger.info("Successfully tested parsing of valid line");
    }

    @Test
    void testParseLine_EmptyLine() {
        // Подготовка
        String line = "";

        // Выполнение
        String[] fields = FileReader.parseLine(line);

        // Проверка
        assertNull(fields, "Should return null for empty line");
        logger.info("Successfully tested parsing of empty line");
    }

    @Test
    void testParseLine_InsufficientFields() {
        // Подготовка
        String line = "Employee,1";

        // Выполнение
        String[] fields = FileReader.parseLine(line);

        // Проверка
        assertNull(fields, "Should return null for line with insufficient fields");
        logger.info("Successfully tested parsing of line with insufficient fields");
    }
}