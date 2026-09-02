
package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;



import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportManager {

    private static ExtentReports extent;
    private static ExtentTest test;
    private static String reportPath;
    private static String dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(new Date());


public static final String vDatetype8 = "dd-MMM-yyyy HH:mm:ss"; // Example format

    public static String getDateFormat(String format) {
        return new java.text.SimpleDateFormat(format).format(new java.util.Date());
    }

    
    
    // Initialize ExtentReports
    public static void initReports() {
        try {
            String reportsDir = System.getProperty("user.dir") + File.separator + "Reports";
            Files.createDirectories(Paths.get(reportsDir));
            reportPath = reportsDir + File.separator + "ExtentReport_" + dateFormat + ".html";

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle("Automation Test Report");
            sparkReporter.config().setReportName("Test Execution Report");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("Environment", "QA");
            extent.setSystemInfo("User", System.getProperty("user.name"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ExtentReports getExtent() {
        return extent;
    }

    public static ExtentTest createTest(String testName, String description) {
        test = extent.createTest(testName, description);
        return test;
    }

    public static void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }
    
    public static void logToExtent(String status, String details) {
        if (test != null) {
            switch (status.toUpperCase()) {
                case "PASS":
                    test.pass(details);
                    break;
                case "FAIL":
                    test.fail(details);
                    break;
                case "INFO":
                    test.info(details);
                    break;
                case "SKIP":
                    test.skip(details);
                    break;
                default:
                    test.info(details);
            }
        }
    }

    // Write to Notepad
    public static void writeToNotepad(String status, String stepName, String details) {
        try {
            String filePath = System.getProperty("user.dir") + File.separator + "Reports" + File.separator + "ExecutionLog.txt";
            Files.createDirectories(Paths.get(System.getProperty("user.dir") + File.separator + "Reports"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
            writer.write(status + " | " + stepName + " | " + details);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Write to Excel
    public static void writeToExcel(String status, String stepName, String details) {
        String excelPath = System.getProperty("user.dir") + File.separator + "Reports" + File.separator + "ExecutionLog.xlsx";
        try {
            Workbook workbook;
            Sheet sheet;
            File file = new File(excelPath);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheet("Results");
                if (sheet == null) {
                    sheet = workbook.createSheet("Results");
                }
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Results");
            }

            int rowCount = sheet.getLastRowNum();
            Row row = sheet.createRow(rowCount + 1);
            row.createCell(0).setCellValue(status);
            row.createCell(1).setCellValue(stepName);
            row.createCell(2).setCellValue(details);
            row.createCell(3).setCellValue(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));

            FileOutputStream fos = new FileOutputStream(excelPath);
            workbook.write(fos);
            fos.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Write to HTML (simple log)
    public static void writeToHTML(String status, String stepName, String details) {
        try {
            String htmlPath = System.getProperty("user.dir") + File.separator + "Reports" + File.separator + "ExecutionLog.html";
            Files.createDirectories(Paths.get(System.getProperty("user.dir") + File.separator + "Reports"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(htmlPath, true));
            writer.write("<p><b>" + status + "</b> | " + stepName + " | " + details + "</p>");
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

public static String textExecutionTime(long diffMillis) {
    long diffSeconds = diffMillis / 1000;
    long hours = diffSeconds / 3600;
    long remainder = diffSeconds % 3600;
    long minutes = remainder / 60;
    long seconds = remainder % 60;
    return ((hours < 10 ? "0" : "") + hours + ":" +
            (minutes < 10 ? "0" : "") + minutes + ":" +
            (seconds < 10 ? "0" : "") + seconds);
}

public static long currentTimeInMillis() {
    return System.currentTimeMillis();
}
public static void ReportEvent(Initialize.TestStatus status, String stepName, String details) {
    String statusStr = status.toString();
    logToExtent(statusStr, stepName + " - " + details);
    writeToNotepad(statusStr, stepName, details);
    writeToExcel(statusStr, stepName, details);
    writeToHTML(statusStr, stepName, details);
}

}
