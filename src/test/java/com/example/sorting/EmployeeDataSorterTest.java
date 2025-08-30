package com.example.sorting;

import com.example.enums.AppConstants;
import com.example.logging.FileErrorLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeDataSorterTest {

    @Mock
    private FileErrorLogger fileErrorLogger;

    @InjectMocks
    private EmployeeDataSorter sorter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSortDataWithManagerAndEmployeesByNameAsc() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Manager", "001", "Manager Doe", "5000.0", "HR"});
        data.add(new String[]{"Employee", "002", "Jane Doe", "3000.0", "001"});
        data.add(new String[]{"Employee", "003", "John Doe", "4000.0", "001"});

        sorter.sortData(data, AppConstants.SORT_NAME.getValue(), AppConstants.ORDER_ASC.getValue());

        assertEquals(3, data.size(), "List size should remain 3");
        assertEquals("Manager", data.get(0)[0], "First entry should be Manager");
        assertEquals("Jane Doe", data.get(1)[2], "Second entry should be Jane Doe");
        assertEquals("John Doe", data.get(2)[2], "Third entry should be John Doe");
        verifyNoInteractions(fileErrorLogger);
    }

    @Test
    void testSortDataWithManagerAndEmployeesBySalaryDesc() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Manager", "001", "Manager Doe", "5000.0", "HR"});
        data.add(new String[]{"Employee", "002", "Jane Doe", "3000.0", "001"});
        data.add(new String[]{"Employee", "003", "John Doe", "4000.0", "001"});

        sorter.sortData(data, AppConstants.SORT_SALARY.getValue(), AppConstants.ORDER_DESC.getValue());

        assertEquals(3, data.size(), "List size should remain 3");
        assertEquals("Manager", data.get(0)[0], "First entry should be Manager");
        assertEquals("4000.0", data.get(1)[3], "Second entry should be Manager Doe with highest salary");
        assertEquals("3000.0", data.get(2)[3], "Third entry should be John Doe");
        verifyNoInteractions(fileErrorLogger);
    }

    @Test
    void testSortDataWithNullOrEmptyInput() {
        List<String[]> nullData = null;
        List<String[]> emptyData = new ArrayList<>();
        List<String[]> dataWithManager = new ArrayList<>();
        dataWithManager.add(new String[]{"Manager", "001", "Manager Doe", "5000.0", "HR"});

        sorter.sortData(nullData, AppConstants.SORT_NAME.getValue(), AppConstants.ORDER_ASC.getValue());
        sorter.sortData(emptyData, AppConstants.SORT_NAME.getValue(), AppConstants.ORDER_ASC.getValue());
        sorter.sortData(dataWithManager, null, AppConstants.ORDER_ASC.getValue());

        assertNull(nullData, "Null list should remain null");
        assertTrue(emptyData.isEmpty(), "Empty list should remain empty");
        assertEquals(1, dataWithManager.size(), "List with manager should retain manager");
        assertEquals("Manager", dataWithManager.get(0)[0], "Manager should remain in list");
        verifyNoInteractions(fileErrorLogger);
    }

    @Test
    void testSortDataWithValidSalaries() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Manager", "001", "Manager Doe", "5000.0", "HR"});
        data.add(new String[]{"Employee", "002", "Jane Doe", "3000.0", "001"});
        data.add(new String[]{"Employee", "003", "John Doe", "4000.0", "001"});

        sorter.sortData(data, AppConstants.SORT_SALARY.getValue(), AppConstants.ORDER_ASC.getValue());

        assertEquals(3, data.size(), "List size should remain 3");
        assertEquals("Manager", data.get(0)[0], "First entry should be Manager");
        assertEquals("3000.0", data.get(1)[3], "Second entry should be Jane Doe with lowest salary");
        assertEquals("4000.0", data.get(2)[3], "Third entry should be John Doe with highest salary");
        verifyNoInteractions(fileErrorLogger);
    }

    @Test
    void testExtractManagerWithMultipleEmployees() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Employee", "002", "Jane Doe", "3000.0", "001"});
        data.add(new String[]{"Manager", "001", "Manager Doe", "5000.0", "HR"});
        data.add(new String[]{"Employee", "003", "John Doe", "4000.0", "001"});

        String[] manager = sorter.extractManager(data);

        assertEquals("Manager", manager[0], "Extracted manager role should be 'Manager'");
        assertEquals(2, data.size(), "List should contain 2 employees after extraction");
        assertEquals("Employee", data.get(0)[0], "First remaining entry should be Employee");
    }

    @Test
    void testExtractManagerWithNoManager() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Employee", "002", "Jane Doe", "3000.0", "001"});
        data.add(new String[]{"Employee", "003", "John Doe", "4000.0", "001"});

        String[] manager = sorter.extractManager(data);

        assertNull(manager, "Should return null if no manager exists");
        assertEquals(2, data.size(), "List size should remain unchanged");
    }
}