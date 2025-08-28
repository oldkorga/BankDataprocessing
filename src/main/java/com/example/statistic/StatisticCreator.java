package com.example.statistic;

import java.util.List;
import java.util.Map;

public interface StatisticCreator {
    void createStatistic(Map<String, List<String[]>> departmentData, String output, String outputPath)throws Exception;
}
