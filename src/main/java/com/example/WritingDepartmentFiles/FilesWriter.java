package com.example.WritingDepartmentFiles;

import java.io.IOException;
import java.util.List;

public interface FilesWriter {
    void writeDepartmentFile(String department, List<String[]> data) throws IOException;
}
