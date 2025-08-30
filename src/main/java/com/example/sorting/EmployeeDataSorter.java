package com.example.sorting;

import com.example.enums.AppConstants;
import com.example.logging.FileErrorLogger;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class EmployeeDataSorter implements DataSorter {
    private static final FileErrorLogger errorLogger = new FileErrorLogger();

    @Override
    public void sortData(List<String[]> data, String sortBy, String order) {
        if (data == null || data.isEmpty() || sortBy == null) {
            return;
        }

        String[] manager = extractManager(data);
        if (data.isEmpty()) {
            if (manager != null) {
                data.add(manager);
            }
            return;
        }

        data.sort(createComparator(sortBy, order));

        if (manager != null) {
            data.add(0, manager);
        }
    }

    protected String[] extractManager(List<String[]> data) {
        Iterator<String[]> iterator = data.iterator();
        while (iterator.hasNext()) {
            String[] fields = iterator.next();
            if (AppConstants.MANAGER_ROLE.getValue().equals(fields[0].trim())) {
                iterator.remove();
                return fields;
            }
        }
        return null;
    }

    private Comparator<String[]> createComparator(String sortBy, String order) {
        return (a, b) -> {
            if (!isEmployee(a) || !isEmployee(b)) {
                return 0;
            }

            int comparison = compareByCriteria(a, b, sortBy);
            return isDescendingOrder(order) ? -comparison : comparison;
        };
    }

    private boolean isEmployee(String[] fields) {
        return AppConstants.EMPLOYEE_ROLE.getValue().equals(fields[0].trim());
    }

    private boolean isDescendingOrder(String order) {
        return AppConstants.ORDER_DESC.getValue().equals(order);
    }

    private int compareByCriteria(String[] a, String[] b, String sortBy) {
        if (AppConstants.SORT_NAME.getValue().equals(sortBy)) {
            return compareByName(a, b);
        } else if (AppConstants.SORT_SALARY.getValue().equals(sortBy)) {
            return compareBySalary(a, b);
        }
        return 0;
    }

    private int compareByName(String[] a, String[] b) {
        return a[2].trim().compareTo(b[2].trim());
    }

    private int compareBySalary(String[] a, String[] b) {
        try {
            double salaryA = Double.parseDouble(a[3].trim());
            double salaryB = Double.parseDouble(b[3].trim());
            return Double.compare(salaryA, salaryB);
        } catch (NumberFormatException e) {
            errorLogger.logError(a[3] + "," + b[3], "Invalid salary format during sorting: " + e.getMessage());
            return 0;
        }
    }
}