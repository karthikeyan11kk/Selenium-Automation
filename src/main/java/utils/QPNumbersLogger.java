package utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logs every defect Hooks.onTestFailure() raises in Jira into a "Defects"
 * sheet inside Q&P-Numbers.xls, with a timestamp - a local, spreadsheet-
 * visible record of every defect raised, alongside the quote/policy numbers
 * already tracked in the Shop/PO sheets there. Doesn't touch those existing
 * sheets' structure at all - adds a new sheet only.
 */
public class QPNumbersLogger {

    private static final String SHEET_NAME = "Defects";
    private static final String[] HEADERS = {"Timestamp", "Product", "TestCaseName", "DefectKey", "DefectURL"};

    public static synchronized void logDefect(String product, String testCaseName, String defectKey, String defectUrl) throws Exception {
        String path = Initialize.getQPNumbersFilePath();
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("Q&P-Numbers.xls not found at " + path + " - skipping defect log entry.");
            return;
        }

        Workbook workbook;
        try (FileInputStream fis = new FileInputStream(file)) {
            workbook = WorkbookFactory.create(fis);
        }

        Sheet sheet = workbook.getSheet(SHEET_NAME);
        if (sheet == null) {
            sheet = workbook.createSheet(SHEET_NAME);
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }
        }

        int nextRow = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(nextRow);
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        row.createCell(0).setCellValue(timestamp);
        row.createCell(1).setCellValue(product == null ? "" : product);
        row.createCell(2).setCellValue(testCaseName == null ? "" : testCaseName);
        row.createCell(3).setCellValue(defectKey == null ? "" : defectKey);
        row.createCell(4).setCellValue(defectUrl == null ? "" : defectUrl);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        workbook.close();

        System.out.println("Logged defect " + defectKey + " for " + testCaseName + " into Q&P-Numbers.xls (Defects sheet)");
    }
}
