package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

/**
 * Report path is deliberately kept stable (Testresults/ExtentReport.html)
 * rather than the demo's timestamped-per-run naming, since
 * MailReportSender attaches this exact file to the post-run email.
 */
public class ExtentManager {
    private static ExtentReports extent;

    public synchronized static ExtentReports getReporter() {
        if (extent == null) {
            String reportPath = System.getProperty("user.dir") + "/Testresults/ExtentReport.html";
            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Framework", "Intact/New1C Automation");
            extent.setSystemInfo("Project", "IBP");
        }
        return extent;
    }
}
