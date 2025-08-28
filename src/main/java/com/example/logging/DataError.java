package com.example.logging;

import java.io.IOException;

public interface DataError {
    void logError(String line, String reason) throws IOException;
}