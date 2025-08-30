package com.example.logging;
import java.io.IOException;

public interface DataValidError {
    void logDataValidation(String reason)throws IOException;
}
