package com.example.processor;

import com.example.util.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


public class EmployeeProcessor {
    protected static Path OUTPUT_DIR = Paths.get("output").toAbsolutePath();;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeProcessor.class);
    private static boolean isFirstCall = true;

    protected void processFiles(String[] args) {
        // Инициализация параметров с использованием объекта
        ArgsConfig argsConfig = parseArgs(args);
        String sortBy = argsConfig.sortBy;
        String sortOrder = argsConfig.sortOrder;
        boolean stat = argsConfig.stat;
        String output = argsConfig.output;
        String outputPath = argsConfig.outputPath;

        Map<String, List<String[]>> departmentData = new HashMap<>();
        Map<String, String> managerDepartments = new HashMap<>();
        List<String[]> pendingEmployees = new ArrayList<>();

        try {
            Path currentDir = Paths.get(".").toAbsolutePath();
            List<String> allLines = FileReader.readSbFiles(currentDir);

            // Первый проход: накопление данных
            for (String line : allLines) {
                String[] fields = FileReader.parseLine(line);
                if (fields == null || fields.length != 5) {
                    logError(line);
                    continue;
                }

                String role = fields[0].trim();
                String managerId = fields[1].trim(); // Идентификатор менеджера или сотрудника
                String employeeManagerId = fields[4].trim(); // Идентификатор менеджера для сотрудника

                if ("Manager".equals(role)) {
                    if(managerDepartments.containsKey(managerId)) {
                        logError(line);
                        System.out.println(line);
                    }else {
                    String department = fields[4].trim(); // Департамент менеджера
                    managerDepartments.put(managerId, department);
                    departmentData.computeIfAbsent(department, k -> new ArrayList<>()).add(fields);
                    }
                } else if ("Employee".equals(role)) {
                    // Проверка корректности зарплаты
                    String salaryStr = fields[3].trim();
                    if (salaryStr.isEmpty()) {
                        logError(line); // Пустая или null зарплата
                        continue;
                    }
                    try {
                        double salary = Double.parseDouble(salaryStr);
                        if (salary <= 0) {
                            logError(line); // Негативная или нулевая зарплата
                            continue;
                        }
                        // Если менеджер известен, добавляем сразу
                        if (managerDepartments.containsKey(employeeManagerId)) {
                            String department = managerDepartments.get(employeeManagerId);
                            departmentData.computeIfAbsent(department, k -> new ArrayList<>()).add(fields);
                        } else {
                            pendingEmployees.add(fields); // Отложенное добавление
                        }
                    } catch (NumberFormatException e) {
                        logError(line); // Некорректный формат зарплаты
                    }
                } else {
                    logError(line); // Неизвестная роль
                }
            }

            // Второй проход: распределение отложенных сотрудников
            for (String[] fields : pendingEmployees) {
                String role = fields[0].trim();
                String employeeManagerId = fields[4].trim();
                if ("Employee".equals(role) && managerDepartments.containsKey(employeeManagerId)) {
                    String salaryStr = fields[3].trim();
                    try {
                        double salary = Double.parseDouble(salaryStr);
                        if (salary > 0) {
                            String department = managerDepartments.get(employeeManagerId);
                            departmentData.computeIfAbsent(department, k -> new ArrayList<>()).add(fields);
                        } else {
                            logError(String.join(",", fields)); // Негативная или нулевая зарплата
                        }
                    } catch (NumberFormatException e) {
                        logError(String.join(",", fields)); // Некорректный формат зарплаты
                    }
                } else {
                    logError(String.join(",", fields)); // Сотрудник без менеджера
                }
            }

            for (Map.Entry<String, List<String[]>> entry : departmentData.entrySet()) {
                String department = entry.getKey();
                List<String[]> data = entry.getValue();
                sortData(data, sortBy, sortOrder);
                writeDepartmentFile(department, data);
            }

            if (stat) {
                generateStat(departmentData, output, outputPath);
            }

        } catch (IOException e) {
            logger.error("Error processing files: {}", e.getMessage());
        }
    }

    protected static class ArgsConfig {
        String sortBy, sortOrder, output, outputPath;
        boolean stat;

        ArgsConfig(String sortBy, String sortOrder, boolean stat, String output, String outputPath) {
            this.sortBy = sortBy;
            this.sortOrder = sortOrder;
            this.stat = stat;
            this.output = output;
            this.outputPath = outputPath;
        }
    }

    protected ArgsConfig parseArgs(String[] args) {
        String sortBy = null;
        String sortOrder = null;
        boolean stat = false;
        String output = "console";
        String outputPath = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--sort") || arg.startsWith("-s=")) {
                String value = arg.split("=")[1].trim().toLowerCase();
                if ("name".equals(value) || "salary".equals(value)) {
                    sortBy = value;
                } else {
                    logger.error("Invalid sort option: {}. Use 'name' or 'salary'", value);
                }
            } else if (arg.startsWith("--order")) {
                String value = arg.split("=")[1].trim().toLowerCase();
                if ("asc".equals(value) || "desc".equals(value)) {
                    sortOrder = value;
                } else {
                    logger.error("Invalid order option: {}. Use 'asc' or 'desc'", value);
                }
            } else if (arg.startsWith("--stat")) {
                stat = true;
            } else if (arg.startsWith("--output") || arg.startsWith("-o=")) {
                String value = arg.split("=")[1].trim().toLowerCase();
                if ("console".equals(value)) {
                    output = "console";
                } else if ("file".equals(value)) {
                    output = "file";
                    if (i + 1 < args.length && args[i + 1].startsWith("--path=")) {
                        outputPath = args[i + 1].split("=")[1].trim();
                        i++;
                    } else {
                        logger.error("Path must be specified with --output=file using --path=<path>");
                    }
                } else {
                    logger.error("Invalid output value: {}. Use 'file' or 'console'", value);
                }
            } else {
                logger.warn("Unknown argument: {}", arg);
            }
        }

        if ((sortBy != null && sortOrder == null) || (sortOrder != null && sortBy == null)) {
            logger.error("Sort and order parameters must be used together");
        }
        if ("file".equals(output) && outputPath == null) {
            logger.error("Output path must be specified with --output=file");
        }

        return new ArgsConfig(sortBy, sortOrder, stat, output, outputPath);
    }


    protected void sortData(List<String[]> data, String sortBy, String order) {

        if( sortBy ==null){
            return;
        }

        String[] manager= null;
        Iterator<String[]> iterator = data.iterator();
        while (iterator.hasNext()) {
            String[] fields = iterator.next();
            if ("Manager".equals(fields[0].trim())) {
                manager = fields;
                iterator.remove();
                break;
            }
        }

        if(!data.isEmpty()){
            data.sort((a,b)->{
                if(!"Employee".equals(a[0].trim()) || !"Employee".equals(b[0].trim())){
                    return 0;
                }

                int comparison=0;
                if ("name".equals(sortBy)) {
                    comparison=a[2].trim().compareTo(b[2].trim());
                }else if ("salary".equals(sortBy)) {
                    try{
                        double salaryA = Double.parseDouble(a[3].trim());
                        double salaryB = Double.parseDouble(b[3].trim());
                            comparison= Double.compare(salaryA,salaryB);

                    }catch (NumberFormatException e){
                        logger.warn("Invalid salary format for sorting :{}", b[3].trim() );
                        return 0;
                    }
                }
                return "desc".equals(order) ? -comparison : comparison;

            });
        }
        if(manager!=null){
            data.add(0,manager);
        }
    }

    protected void writeDepartmentFile(String department, List<String[]> data) throws IOException {
        if (data == null || data.isEmpty()) {
            logger.warn("No data to write for department {}", department);
            return;
        }
        if (!data.get(0)[0].trim().equals("Manager")) {
            logger.warn("No manager found for department: {}. File not created", department);
            return;
        }

        try {
            // Использование фиксированного пути OUTPUT_DIR
            if (!Files.exists(OUTPUT_DIR)) {
                Files.createDirectories(OUTPUT_DIR);
            }
            String clearDepartment = department.replaceAll("[^a-zA-Z0-9-_.]", "").trim();
            if (clearDepartment.isEmpty()) {
                // Перенаправление данных в error.log при некорректном департаменте
                for (String[] fields : data) {
                    String line = String.join(",", fields).trim();
                    logError(line);
                }
                logger.error("Invalid department name after cleaning, data logged to error.log: {}", department);
                return;
            }

            Path outputFile = OUTPUT_DIR.resolve(clearDepartment + ".sb");

            List<String> lines = new ArrayList<>();
            for (String[] fields : data) {
                String line = String.join(",", fields).trim();
                lines.add(line);
            }

            Files.write(outputFile, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Data written to file: {}", outputFile.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Error writing to file. {}", e.getMessage());
            throw e;
        }
    }

    protected void generateStat(Map<String, List<String[]>> departmentData, String output, String outputPath) throws IOException {

        if(departmentData.isEmpty()){
            logger.warn("No data to generate statistic . ");
            return;
        }

        List<String> stats = new ArrayList<>();
        stats.add("department,min,max,mid");

        List<Map.Entry<String, List<String[]>>> sortedEntries = new ArrayList<>(departmentData.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());

        for (Map.Entry<String, List<String[]>> entry : sortedEntries) {
            List<String[]> values = entry.getValue();
            List<Double> salaries = new ArrayList<>(); // Локальный список для текущего департамента
            for (String[] fields : values) {
                if (!"Manager".equals(fields[0].trim())) {
                    if(fields[3]==null || fields[3].trim().isEmpty()){
                        logger.warn("Empty salary field for department {}", entry.getKey());
                        continue;
                    }
                    try {
                        double salaryValue = Double.parseDouble(fields[3].trim());
                        if (salaryValue > 0) {
                            salaries.add(salaryValue);
                        } else {
                            logger.warn("Invalid salary (non-positive): {} for department {}", fields[3], entry.getKey());
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid salary format: {} for department {}", fields[3], entry.getKey());
                    }
                }
            }

            double min = salaries.isEmpty() ? 0.0 : Collections.min(salaries);
            double max = salaries.isEmpty() ? 0.0 : Collections.max(salaries);
            double sum=0;
            for (Double salary : salaries) {
                sum += salary;
            }
            double mid = salaries.isEmpty() ? 0.0 : sum / salaries.size();
            min = Math.ceil(min * 100) / 100;
            max = Math.ceil(max * 100) / 100;
            mid = Math.ceil(mid * 100) / 100;
            stats.add(entry.getKey()+","+min+","+max+","+mid);
        }

        if("console".equals(output) ){
            stats.forEach(System.out::println);
        }else if("file".equals(output) && outputPath != null){
            Files.write(Paths.get(outputPath), stats,StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Statistics written to file: {}", outputPath);
        }


    }

    protected void logError(String line) throws IOException {
        Path path = OUTPUT_DIR.resolve("error.log");
        if (isFirstCall) {
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.writeString(path, (line + "\n"), StandardOpenOption.CREATE_NEW);
            isFirstCall = false;
        } else {
            Files.writeString(path, (line + "\n"), StandardOpenOption.APPEND);
        }
    }

    public static void main(String[] args) {
        EmployeeProcessor processor = new EmployeeProcessor();
        try {
            processor.processFiles(args);
        } catch (Exception e) {
            logger.error("Application failed: {}", e.getMessage());
        }
    }


}
