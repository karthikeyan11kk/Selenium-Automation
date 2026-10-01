package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(QPNumbersLogger.class);


    private static final String SHEET_NAME = "Defects";
    private static final String[] HEADERS = {"Timestamp", "Product", "TestCaseName", "DefectKey", "DefectURL"};

    public static synchronized void logDefect(String product, String testCaseName, String defectKey, String defectUrl) throws Exception {
        String path = Initialize.getQPNumbersFilePath();
        File file = new File(path);
        if (!file.exists()) {
            log.error("Q&P-Numbers.xls not found at " + path + " - skipping defect log entry.");
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

        log.info("Logged defect " + defectKey + " for " + testCaseName + " into Q&P-Numbers.xls (Defects sheet)");
    }

    /**
     * Writes the DefectID (and a Timestamp) directly into the SAME row as
     * this test case's Quote/PolicyNumber in the Shop/PO sheet - not just
     * the separate "Defects" sheet above. This is what makes the defect
     * visible right next to the quote/policy it belongs to, at a glance,
     * without cross-referencing a different sheet. "product" must be
     * exactly "Shop" or "PO" to match the sheet name New1CShop/
     * New1CPropertyOwners already write QuoteNumber/PolicyNumber into.
     */
    public static void setDefectId(String product, String testCaseName, String defectKey) {
        try {
            if (product == null || (!"Shop".equalsIgnoreCase(product) && !"PO".equalsIgnoreCase(product))) {
                log.error("setDefectId: unrecognized product '" + product
                        + "' - expected 'Shop' or 'PO', skipping row-level DefectID write.");
                return;
            }
            String sheetName = "Shop".equalsIgnoreCase(product) ? "Shop" : "PO";
            String path = Initialize.getQPNumbersFilePath();

            Common_Methods commonMethods = new Common_Methods();
            int rowIndex = commonMethods.findOrCreateRowIndexByTestCaseName(path, sheetName, testCaseName);

            commonMethods.excelWriteMethod(path, sheetName, rowIndex, "TestCaseName", testCaseName);
            commonMethods.excelWriteMethod(path, sheetName, rowIndex, "DefectID", defectKey == null ? "" : defectKey);
            commonMethods.excelWriteMethod(path, sheetName, rowIndex, "Timestamp",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));

            log.info("Wrote DefectID " + defectKey + " into " + sheetName + " sheet row for " + testCaseName);
        } catch (Exception e) {
            log.error("Failed to write DefectID into Q&P-Numbers.xls " + product + " sheet for "
                    + testCaseName + ": " + e.getMessage());
        }
    }

    /**
     * Reads back a value (QuoteNumber, PolicyNumber, etc.) from this test
     * case's row in the Shop/PO sheet. Used by ProductFlowSteps' Then step
     * to actually verify the business outcome (a quote/policy reference was
     * genuinely captured) rather than just checking "no exception was
     * thrown" - a flow that silently fails to capture a reference number,
     * without Selenium itself throwing, would otherwise pass unnoticed.
     * Returns null if the row or column doesn't exist / is blank.
     */
    public static String getRecordedValue(String product, String testCaseName, String columnHeader) {
        try {
            if (product == null || (!"Shop".equalsIgnoreCase(product) && !"PO".equalsIgnoreCase(product))) {
                return null;
            }
            String sheetName = "Shop".equalsIgnoreCase(product) ? "Shop" : "PO";
            String path = Initialize.getQPNumbersFilePath();
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }

            Workbook workbook;
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = WorkbookFactory.create(fis);
            }
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                workbook.close();
                return null;
            }

            org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
            int colIndex = -1;
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(c);
                if (cell != null && columnHeader.equalsIgnoreCase(cell.toString().trim())) {
                    colIndex = c;
                    break;
                }
            }
            if (colIndex < 0) {
                workbook.close();
                return null;
            }

            String result = null;
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                org.apache.poi.ss.usermodel.Cell nameCell = row.getCell(0);
                if (nameCell != null && testCaseName.equalsIgnoreCase(nameCell.toString().trim())) {
                    org.apache.poi.ss.usermodel.Cell valueCell = row.getCell(colIndex);
                    result = valueCell == null ? null : valueCell.toString().trim();
                    break;
                }
            }
            workbook.close();
            return (result == null || result.isEmpty()) ? null : result;
        } catch (Exception e) {
            log.error("Failed to read '" + columnHeader + "' for " + testCaseName + " from Q&P-Numbers.xls: " , e);
            return null;
        }
    }
}
