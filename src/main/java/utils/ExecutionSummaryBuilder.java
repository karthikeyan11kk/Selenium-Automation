package utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature 2 - "step-wise pass, policy number and defect Id those details
 * must be present in the Excel (each Sheet must contain single single test
 * case)".
 *
 * Collects, per test case, the step-wise results already produced in
 * ProductFlowSteps.executeProductFlow() (the same list handed to
 * JiraIntegration.updateXrayManualSteps()), plus the Quote/Policy/Defect
 * references already tracked in Q&P-Numbers.xls via QPNumbersLogger - then,
 * once the whole suite finishes, writes ONE WORKBOOK where every test case
 * gets its OWN sheet (not a shared sheet with one row per test case, as
 * Q&P-Numbers.xls still does for backward compatibility).
 *
 * This does not replace Q&P-Numbers.xls or the Extent HTML report - it is
 * the new, additional per-test-case workbook Feature 2 asked for, built from
 * the same data those already capture.
 */
public class ExecutionSummaryBuilder {
    private static final Logger log = LoggerFactory.getLogger(ExecutionSummaryBuilder.class);

    private static final Object LOCK = new Object();

    private static final Map<String, TestCaseRecord> RECORDS = new LinkedHashMap<>();

    public static class TestCaseRecord {
        String testCaseName;
        String product;
        String overallStatus = "NOT RUN";
        String jiraKey;
        List<Map<String, String>> steps = new ArrayList<>();
    }

    /** Called once per scenario (pass or fail) from ProductFlowSteps. */
    public static void recordOutcome(String testCaseName, String product, String jiraKey,
                                      List<Map<String, String>> stepResults, String overallStatus) {
        synchronized (LOCK) {
            TestCaseRecord rec = new TestCaseRecord();
            rec.testCaseName = testCaseName;
            rec.product = product;
            rec.jiraKey = jiraKey;
            rec.overallStatus = overallStatus;
            rec.steps = stepResults == null ? new ArrayList<>() : new ArrayList<>(stepResults);
            RECORDS.put(testCaseName, rec);
        }
        log.info("ExecutionSummaryBuilder: recorded " + stepResultsSize(stepResults)
                + " step(s) for '" + testCaseName + "' - overall " + overallStatus);
    }

    private static int stepResultsSize(List<Map<String, String>> l) {
        return l == null ? 0 : l.size();
    }

    public static boolean hasRecords() {
        synchronized (LOCK) {
            return !RECORDS.isEmpty();
        }
    }

    /**
     * Writes the per-test-case workbook. Each sheet = one test case (name
     * sanitized/truncated to Excel's 31-char sheet-name limit, de-duplicated
     * if two test case names collide after truncation). Every sheet
     * contains:
     *   - the step-wise rows (Step / Status / Actual Result)
     *   - a trailing summary block: Quote Number, Policy Number, Defect ID,
     *     Timestamp, Overall Result - pulled live from Q&P-Numbers.xls via
     *     QPNumbersLogger, which Hooks.onTestFailure/onTestSuccess already
     *     keep up to date for every test case.
     */
    public static String writeWorkbook(String outputPath) {
        synchronized (LOCK) {
            if (RECORDS.isEmpty()) {
                log.info("ExecutionSummaryBuilder: no recorded test cases - skipping per-test-case workbook.");
                return null;
            }
            try (Workbook wb = new HSSFWorkbook()) {
                Font boldFont = wb.createFont();
                boldFont.setBold(true);
                CellStyle headerStyle = wb.createCellStyle();
                headerStyle.setFont(boldFont);

                java.util.Set<String> usedSheetNames = new java.util.HashSet<>();

                for (TestCaseRecord rec : RECORDS.values()) {
                    String sheetName = uniqueSheetName(rec.testCaseName, usedSheetNames);
                    Sheet sheet = wb.createSheet(sheetName);

                    int r = 0;
                    Row titleRow = sheet.createRow(r++);
                    setCell(titleRow, 0, "Test Case:", headerStyle);
                    setCell(titleRow, 1, rec.testCaseName, null);
                    Row metaRow = sheet.createRow(r++);
                    setCell(metaRow, 0, "Product:", headerStyle);
                    setCell(metaRow, 1, rec.product == null ? "" : rec.product, null);
                    setCell(metaRow, 2, "Jira Key:", headerStyle);
                    setCell(metaRow, 3, rec.jiraKey == null ? "" : rec.jiraKey, null);
                    r++; // blank spacer row

                    Row header = sheet.createRow(r++);
                    setCell(header, 0, "Step", headerStyle);
                    setCell(header, 1, "Status", headerStyle);
                    setCell(header, 2, "Actual Result", headerStyle);

                    for (Map<String, String> step : rec.steps) {
                        Row row = sheet.createRow(r++);
                        setCell(row, 0, step.getOrDefault("stepName", "Step"), null);
                        setCell(row, 1, step.getOrDefault("status", ""), null);
                        setCell(row, 2, step.getOrDefault("actualResult", ""), null);
                    }

                    r++; // blank spacer row
                    Row summaryHeader = sheet.createRow(r++);
                    setCell(summaryHeader, 0, "Summary", headerStyle);

                    String quoteNumber = QPNumbersLogger.getRecordedValue(rec.product, rec.testCaseName, "QuoteNumber");
                    String policyNumber = QPNumbersLogger.getRecordedValue(rec.product, rec.testCaseName, "PolicyNumber");
                    String defectId = QPNumbersLogger.getRecordedValue(rec.product, rec.testCaseName, "DefectID");
                    String timestamp = QPNumbersLogger.getRecordedValue(rec.product, rec.testCaseName, "Timestamp");

                    writeSummaryRow(sheet, r++, "Quote Number", quoteNumber);
                    writeSummaryRow(sheet, r++, "Policy Number", policyNumber);
                    writeSummaryRow(sheet, r++, "Defect ID", defectId);
                    writeSummaryRow(sheet, r++, "Timestamp", timestamp);
                    writeSummaryRow(sheet, r++, "Overall Result", rec.overallStatus);

                    for (int c = 0; c <= 3; c++) {
                        sheet.autoSizeColumn(c);
                    }
                }

                File outFile = new File(outputPath);
                File parent = outFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    wb.write(fos);
                }
                log.info("Execution summary workbook written: " + outFile.getAbsolutePath()
                        + " (" + RECORDS.size() + " test case sheet(s))");
                return outFile.getAbsolutePath();
            } catch (Exception e) {
                log.error("Failed to write execution summary workbook: ", e);
                return null;
            }
        }
    }

    private static void writeSummaryRow(Sheet sheet, int rowIndex, String label, String value) {
        Row row = sheet.createRow(rowIndex);
        setCell(row, 0, label, null);
        setCell(row, 1, value == null ? "-" : value, null);
    }

    private static void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value == null ? "" : value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    /** Excel sheet names: max 31 chars, no \ / ? * [ ] : characters, must be unique in the workbook. */
    private static String uniqueSheetName(String testCaseName, java.util.Set<String> used) {
        String base = (testCaseName == null ? "TestCase" : testCaseName)
                .replaceAll("[\\\\/\\?\\*\\[\\]:]", "_");
        if (base.length() > 31) {
            base = base.substring(0, 31);
        }
        String candidate = base;
        int suffix = 1;
        while (used.contains(candidate.toLowerCase())) {
            String suffixStr = "_" + suffix;
            int cut = Math.min(base.length(), 31 - suffixStr.length());
            candidate = base.substring(0, cut) + suffixStr;
            suffix++;
        }
        used.add(candidate.toLowerCase());
        return candidate;
    }

    /** Clears all recorded data - call at the very start of a fresh suite run if re-using the same JVM. */
    public static void reset() {
        synchronized (LOCK) {
            RECORDS.clear();
        }
    }
}
