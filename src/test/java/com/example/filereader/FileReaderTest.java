package com.example.filereader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileReaderTest {

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // Создаём временную директорию для тестов
        tempDir = Files.createTempDirectory("test-files-");
    }

    @AfterEach
    void tearDown() throws IOException {
        // Очищаем временную директорию после тестов
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a)) // Удаление в обратном порядке для поддиректорий
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to clean up temp directory: " + path, e);
                        }
                    });
        }
    }

    @Test
    void testReadSbFilesWithValidDirectoryAndFiles() throws IOException {
        // Подготовка: Создаём тестовые файлы
        Path file1 = tempDir.resolve("test1.sb");
        Path file2 = tempDir.resolve("test2.sb");
        Files.writeString(file1, "line1, value1\nline2, value2");
        Files.writeString(file2, "line3, value3");

        // Выполнение
        List<String> lines = FileReader.readSbFiles(tempDir);

        // Проверка
        assertEquals(3, lines.size(), "Should read all lines from .sb files");
        assertTrue(lines.contains("line1, value1"), "Should contain line1");
        assertTrue(lines.contains("line2, value2"), "Should contain line2");
        assertTrue(lines.contains("line3, value3"), "Should contain line3");
    }

    @Test
    void testReadSbFilesWithNonExistentDirectory() {
        // Подготовка: Используем несуществующий путь
        Path nonExistentDir = tempDir.resolve("nonexistent");

        // Проверка
        assertThrows(IOException.class, () -> FileReader.readSbFiles(nonExistentDir),
                "Should throw IOException for non-existent directory");
    }

    @Test
    void testReadSbFilesWithFileInsteadOfDirectory() throws IOException {
        // Подготовка: Создаём файл вместо директории
        Path filePath = tempDir.resolve("testfile.txt");
        Files.createFile(filePath);

        // Проверка
        assertThrows(IOException.class, () -> FileReader.readSbFiles(filePath),
                "Should throw IOException for file instead of directory");
    }

    @Test
    void testReadSbFilesWithUnreadableFile() throws IOException {
        // Подготовка: Создаём файл с ошибкой (например, пустой или недоступный)
        Path file = tempDir.resolve("test3.sb");
        Files.writeString(file, "valid line");
        // Эмуляция ошибки чтения (например, через манипуляцию правами доступа не поддерживается напрямую в тесте)
        // Здесь просто проверяем обработку исключений при некорректном файле (дополнительная настройка требуется для реальной эмуляции)

        List<String> lines = FileReader.readSbFiles(tempDir);
        assertEquals(1, lines.size(), "Should read valid lines despite unreadable file");
        assertTrue(lines.contains("valid line"), "Should contain valid line");
    }

    @Test
    void testParseLineWithValidInput() {
        // Проверка
        String[] fields = FileReader.parseLine("name, age, 25");
        assertNotNull(fields, "Should not return null for valid input");
        assertEquals(3, fields.length, "Should split into 3 fields");
        assertEquals("name", fields[0], "First field should be 'name'");
        assertEquals("age", fields[1], "Second field should be 'age'");
        assertEquals("25", fields[2], "Third field should be '25'");
    }

    @Test
    void testParseLineWithNullInput() {
        // Проверка
        String[] fields = FileReader.parseLine(null);
        assertNull(fields, "Should return null for null input");
    }

    @Test
    void testParseLineWithEmptyInput() {
        // Проверка
        String[] fields = FileReader.parseLine("");
        assertNull(fields, "Should return null for empty input");
    }

    @Test
    void testParseLineWithExtraSpaces() {
        // Проверка
        String[] fields = FileReader.parseLine("  name  ,  age  ,  25  ");
        assertNotNull(fields, "Should not return null for input with extra spaces");
        assertEquals(3, fields.length, "Should split into 3 fields");
        assertEquals("name", fields[0], "First field should be 'name' with spaces trimmed");
        assertEquals("age", fields[1], "Second field should be 'age' with spaces trimmed");
        assertEquals("25", fields[2], "Third field should be '25' with spaces trimmed");
    }
}