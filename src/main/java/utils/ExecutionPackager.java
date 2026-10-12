package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Feature 2 - "Once the execution is done I need a zip to download
 * automatically. In that I need each test case screenshot and report as a
 * folder and an overall execution report."
 *
 * Builds, at the end of every suite run (Hooks.tearDownSuite):
 *
 *   Testresults/ExecutionPackage_<timestamp>.zip
 *     |- OverallReport/ExtentReport.html        (the full-suite Extent report)
 *     |- OverallReport/ExecutionSummary.xls      (Feature 2's per-test-case workbook)
 *     |- <TestCaseName_Timestamp>/report.html    (one small per-test summary page)
 *     |- <TestCaseName_Timestamp>/*.png          (that test case's own screenshots)
 *     |- <TestCaseName_Timestamp>/...             (one such folder per test case)
 *
 * "Automatically" here means: no manual step is needed to produce it - the
 * zip is built unconditionally as part of @AfterAll, ready to be picked up
 * as a pipeline build artifact, attached to the completion email (see
 * MailReportSender), and - when a display is actually available (a local/dev
 * run, not a headless CI agent) - automatically opened via the OS so it
 * behaves like a normal browser "download" would.
 */
public class ExecutionPackager {
    private static final Logger log = LoggerFactory.getLogger(ExecutionPackager.class);

    public static String buildPackage() {
        try {
            String timestamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            String zipPath = System.getProperty("user.dir") + File.separator + "Testresults"
                    + File.separator + "ExecutionPackage_" + timestamp + ".zip";

            File zipFile = new File(zipPath);
            File parent = zipFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // 1) Overall execution report (Extent HTML)
                String extentReportPath = System.getProperty("user.dir") + File.separator + "Testresults"
                        + File.separator + "ExtentReport.html";
                addFileToZip(zos, new File(extentReportPath), "OverallReport/ExtentReport.html");

                // 2) Overall per-test-case Excel workbook (Feature 2's step-wise/
                //    quote/policy/defect sheet-per-test-case workbook)
                String summaryPath = System.getProperty("user.dir") + File.separator + "Testresults"
                        + File.separator + "ExecutionSummary.xls";
                String writtenSummaryPath = ExecutionSummaryBuilder.writeWorkbook(summaryPath);
                if (writtenSummaryPath != null) {
                    addFileToZip(zos, new File(writtenSummaryPath), "OverallReport/ExecutionSummary.xls");
                }

                // 3) One folder per test case: its screenshots + a small
                //    per-test-case HTML report, copied straight from the
                //    centralized Screenshots/<TestCaseName_Timestamp>/ folder
                //    Initialize.getScreenshotDir() already organizes evidence into.
                String screenshotsRoot;
                try {
                    String override = Initialize.getTestDataPF("screenshotsRootDir");
                    screenshotsRoot = (override != null && !override.trim().isEmpty())
                            ? override.trim()
                            : System.getProperty("user.dir") + File.separator + "Screenshots";
                } catch (Exception e) {
                    screenshotsRoot = System.getProperty("user.dir") + File.separator + "Screenshots";
                }

                File screenshotsRootDir = new File(screenshotsRoot);
                File[] testCaseFolders = screenshotsRootDir.listFiles(File::isDirectory);
                if (testCaseFolders != null) {
                    for (File tcFolder : testCaseFolders) {
                        addTestCaseFolder(zos, tcFolder);
                    }
                }

                zos.finish();
            }

            log.info("Execution package built: " + zipFile.getAbsolutePath());

            maybeAutoOpen(zipFile);

            return zipFile.getAbsolutePath();
        } catch (Exception e) {
            log.error("Failed to build execution package zip: ", e);
            return null;
        }
    }

    private static void addTestCaseFolder(ZipOutputStream zos, File tcFolder) throws IOException {
        String tcName = tcFolder.getName();
        String reportHtml = buildPerTestCaseReportHtml(tcName, tcFolder);
        addBytesToZip(zos, reportHtml.getBytes(StandardCharsets.UTF_8), tcName + "/report.html");

        File[] files = tcFolder.listFiles(File::isFile);
        if (files != null) {
            for (File f : files) {
                addFileToZip(zos, f, tcName + "/" + f.getName());
            }
        }
    }

    private static String buildPerTestCaseReportHtml(String tcName, File tcFolder) {
        File[] screenshots = tcFolder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
        StringBuilder gallery = new StringBuilder();
        if (screenshots != null) {
            java.util.Arrays.sort(screenshots, java.util.Comparator.comparing(File::getName));
            for (File shot : screenshots) {
                gallery.append("<div class='shot'><img src='").append(shot.getName())
                        .append("' alt='").append(shot.getName()).append("'><p>")
                        .append(shot.getName()).append("</p></div>\n");
            }
        }
        return "<!DOCTYPE html><html><head><title>" + tcName + "</title>"
                + "<style>body{font-family:Segoe UI,Arial,sans-serif;margin:24px;color:#222}"
                + "h2{color:#1a3c6e}.shot{margin-bottom:24px}.shot img{max-width:900px;border:1px solid #ccc}"
                + "</style></head><body>"
                + "<h2>" + tcName + "</h2>"
                + "<p>Evidence captured during this test case's execution. See the overall "
                + "<code>OverallReport/ExtentReport.html</code> and <code>OverallReport/ExecutionSummary.xls</code> "
                + "for pass/fail, step-wise results, policy/quote numbers and any defect ID.</p>"
                + gallery
                + "</body></html>";
    }

    private static void addFileToZip(ZipOutputStream zos, File file, String entryName) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        zos.putNextEntry(new ZipEntry(entryName));
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.transferTo(zos);
        }
        zos.closeEntry();
    }

    private static void addBytesToZip(ZipOutputStream zos, byte[] bytes, String entryName) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        zos.write(bytes);
        zos.closeEntry();
    }

    /**
     * Best-effort: on a machine with an actual display (a local/dev run,
     * never a headless CI agent), hand the finished zip to the OS the way a
     * browser "Open downloads folder" button would - Windows Explorer will
     * select/offer to open it. On a headless pipeline agent this silently
     * does nothing; the zip is still produced and sits in Testresults/,
     * which is what the pipeline should publish as a build artifact. Gate
     * this behind "autoOpenReportZip" (default No) so CI runs never hang or
     * fail on a missing Desktop API.
     */
    private static void maybeAutoOpen(File zipFile) {
        try {
            boolean autoOpen = "Yes".equalsIgnoreCase(Initialize.getTestDataPF("autoOpenReportZip"));
            if (!autoOpen) {
                return;
            }
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                return;
            }
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(zipFile.getParentFile());
                }
            }
        } catch (Exception e) {
            log.info("Could not auto-open the Testresults folder (likely a headless agent) - "
                    + "the zip is still at " + zipFile.getAbsolutePath() + ". Detail: " + e.getMessage());
        }
    }
}
