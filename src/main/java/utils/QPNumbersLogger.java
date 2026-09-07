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
                System.err.println("setDefectId: unrecognized product '" + product
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

            System.out.println("Wrote DefectID " + defectKey + " into " + sheetName + " sheet row for " + testCaseName);
        } catch (Exception e) {
            System.err.println("Failed to write DefectID into Q&P-Numbers.xls " + product + " sheet for "
                    + testCaseName + ": " + e.getMessage());
        }
    }
}
