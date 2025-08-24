package com.example.processor;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeProcessorTest {

    private EmployeeProcessor processor;
    @TempDir
    static Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        processor = new EmployeeProcessor(); // Или с передачей параметров, если требуется
        Path testDataDir = Paths.get("testData").toAbsolutePath();
        Path errorLog = EmployeeProcessor.OUTPUT_DIR.resolve("error.log");
        if (Files.exists(errorLog)) {
            Files.delete(errorLog);
        }
        Files.createDirectories(EmployeeProcessor.OUTPUT_DIR);
        if (Files.exists(testDataDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(testDataDir, "*.sb")) {
                for (Path file : stream) {
                    Files.deleteIfExists(file);
                }
            }
        } else {
            Files.createDirectories(testDataDir);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Очистка после тестов
        Files.deleteIfExists(tempDir.resolve("output"));
        Files.deleteIfExists(tempDir.resolve("error.log"));
    }

    @Test
    void testSortData_InvalidSalaryException() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Employee", "101", "John Doe", "invalid", "1"});
        data.add(new String[]{"Employee", "102", "Alice Smith", "3500", "1"});
        data.add(new String[]{"Manager", "1", "Jane Smith", "5000", "HR"});

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        processor.sortData(data, "salary", "desc");
        assertEquals("Manager", data.get(0)[0]); // Менеджер остаётся первым
        assertTrue(out.toString().contains("Invalid salary format for sorting :invalid"));
    }

    // Тест для ошибки создания OUTPUT_DIR
    @Test
    void testWriteDepartmentFile_OutputDirCreationFailure() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Manager", "1", "Jane Smith", "5000", "HR"});
        // Симуляция ошибки, делая директорию недоступной
        Path originalOutputDir = EmployeeProcessor.OUTPUT_DIR;
        System.out.println(originalOutputDir);
        System.out.println("Before try, OUTPUT_DIR: " + EmployeeProcessor.OUTPUT_DIR);
        EmployeeProcessor.OUTPUT_DIR = Path.of("C:/Windows/output");
        System.out.println("After change, OUTPUT_DIR: " + EmployeeProcessor.OUTPUT_DIR);
        try {
            processor.writeDepartmentFile("HR", data);
            fail("Expected IOException");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            assertTrue(true); // Ожидаемое поведение
        } finally {
            EmployeeProcessor.OUTPUT_DIR = originalOutputDir;
        }
    }

    // Тест для ошибки в generateStat с пустой зарплатой
    @Test
    void testGenerateStat_EmptySalaryField() throws IOException {
        Map<String, List<String[]>> departmentData = new HashMap<>();
        List<String[]> hrData = new ArrayList<>();
        hrData.add(new String[]{"Manager", "1", "Jane Smith", "5000", "HR"});
        hrData.add(new String[]{"Employee", "101", "John Doe", "", "1"});
        departmentData.put("HR", hrData);
        Path statPath = tempDir.resolve("stat.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        processor.generateStat(departmentData, "file", statPath.toString());
        assertTrue(Files.exists(statPath));
        List<String> stats = Files.readAllLines(statPath);
        assertEquals(2, stats.size());
        assertEquals("department,min,max,mid", stats.get(0));
        assertTrue(stats.get(1).startsWith("HR,0.0,0.0,0.0")); // Статистика пустая
        assertTrue(out.toString().contains("Empty salary field for department HR"));
    }

    // Тест для ошибки в logError
    @Test
    void testLogError_WithIOException() {
        Path originalOutputDir = EmployeeProcessor.OUTPUT_DIR;
        EmployeeProcessor.OUTPUT_DIR = Path.of("R:/root/output"); // Недоступный путь
        try {
            processor.logError("Test error");
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(true); // Ожидаемое поведение
        } finally {
            EmployeeProcessor.OUTPUT_DIR = originalOutputDir;
        }
    }


}