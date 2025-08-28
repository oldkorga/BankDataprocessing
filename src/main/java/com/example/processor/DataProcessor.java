package com.example.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface DataProcessor {
    void processData( Map<String, String> managerDepartments, Map<String, List<String[]>> departmentData, List<String[]> pendingEmployees) throws IOException;
}