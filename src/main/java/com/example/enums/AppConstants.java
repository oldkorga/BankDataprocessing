package com.example.enums;

public enum AppConstants {
    MANAGER_ROLE("Manager"),
    EMPLOYEE_ROLE("Employee"),
    DEFAULT_OUTPUT("console"),
    OUTPUT_FILE("file"),
    SORT_NAME("name"),
    SORT_SALARY("salary"),
    ORDER_ASC("asc"),
    ORDER_DESC("desc"),
    ErrorDetail_Path("error_details.log"),
    Error_Path("errors.log");

    private final String value;

    AppConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean equals(String other) {
        return this.value.equals(other);
    }
}