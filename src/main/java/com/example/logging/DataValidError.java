package com.example.logging;
import java.io.IOException;

public interface DataValidError {
    void logDataValid(String reason)throws IOException;
}
