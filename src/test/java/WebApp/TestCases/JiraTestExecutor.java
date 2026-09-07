package WebApp.TestCases;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import com.aventstack.extentreports.ExtentReports;

import utils.Initialize;
import utils.JiraIntegration;
import utils.ExtentReporter;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class JiraTestExecutor extends Initialize {

    // ---- Config helpers ----
    static String jiraBaseUrl()           { return Initialize.getConfigValue("JiraURL"); }
    private static String jiraEmail()     { return Initialize.getConfigValue("JiraEmail"); }
    private static String jiraApiToken()  { return Initialize.getConfigValue("JiraAPItoken"); }
    private static String jiraJQL()       { return Initialize.getConfigValue("JiraJQL"); }
    private static String passTransitionId() { return Initialize.getConfigValue("JiraPassTransitionId"); }
    private static String failTransitionId() { return Initialize.getConfigValue("JiraFailTransitionId"); }

    /** Fetch Jira test cases using configurable JQL */
    public static List<String> fetchJiraTests() throws Exception {
        String searchUrl = jiraBaseUrl() + "rest/api/2/search?jql=" + jiraJQL().replace(" ", "%20");

        HttpGet get = new HttpGet(searchUrl);
        String basic = Base64.getEncoder().encodeToString((jiraEmail() + ":" + jiraApiToken()).getBytes());
        get.setHeader("Authorization", "Basic " + basic);
        get.setHeader("Content-Type", "application/json");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpResponse response = client.executeOpen(null, get, null);
            String body = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(body);

            JSONArray issues = json.getJSONArray("issues");
            List<String> keys = new ArrayList<>();
            for (int i = 0; i < issues.length(); i++) {
                keys.add(issues.getJSONObject(i).getString("key"));
            }
            System.out.println("✅ Jira Test Cases fetched: " + keys);
            return keys;
        }
    }

    /**
     * Execute the Cucumber scenarios tagged for this Jira key.
     *
     * Replaces the old TestNG-XmlSuite-around-MultiProductTest approach: each
     * Scenario Outline in product_flow.feature that maps to a Jira test issue
     * is tagged @JIRA-<key> (see the feature file), so instead of filtering
     * an Excel DataProvider via a "JiraKeyFilter" system property, we filter
     * Cucumber scenarios by tag and run them via the Cucumber CLI in-process.
     *
     * Returns true if every scenario for that tag passed (exit code 0).
     */
    public static boolean executeTest(String jiraKey) {
        System.out.println("Executing Cucumber scenarios tagged @JIRA-" + jiraKey);

        String[] args = new String[]{
                "--glue", "hooks",
                "--glue", "stepdefinitions",
                "--tags", "@JIRA-" + jiraKey,
                "--plugin", "pretty",
                "--plugin", "html:target/cucumber-report/jira-" + jiraKey + ".html",
                "--plugin", "json:target/cucumber-report/jira-" + jiraKey + ".json",
                "src/test/resources/features"
        };

        byte exitCode = io.cucumber.core.cli.Main.run(args, Thread.currentThread().getContextClassLoader());
        return exitCode == 0;
    }

    /** Update Jira with execution status and optional evidence */

public static void updateJiraStatus(String jiraKey, String status) throws Exception {
    // 1) Final status comment
    JiraIntegration.addComment(jiraKey, "Execution Status: " + status);

    // 2) Drain & attach all evidence collected during the test run

List<String> evidencePaths = JiraIntegration.drainEvidence(jiraKey);
System.out.println("🔍 Evidence list for " + jiraKey + ": " + evidencePaths);

if (evidencePaths != null && !evidencePaths.isEmpty()) {
    for (String path : evidencePaths) {
        File f = new File(path);
        System.out.println("📂 Checking evidence: " + path + " exists? " + f.exists() +
                           " | size=" + (f.exists() ? f.length() : 0));
        if (f.exists()) {
            System.out.println("➡ Attaching evidence to " + jiraKey + ": " + path);
            JiraIntegration.attachScreenshot(jiraKey, path);
            System.out.println("✅ Attached: " + path);
        } else {
            System.err.println("❌ File not found: " + path);
        }
    }
} else {
    System.out.println("ℹ No evidence collected for " + jiraKey + " to attach.");
}


    // 3) Transition by name (use config names; fallback to IDs if name not set)
    String passName = Initialize.getConfigValue("JiraPassTransitionName"); // e.g., "Done"
    String failName = Initialize.getConfigValue("JiraFailTransitionName"); // e.g., "Failed"
    String passId   = Initialize.getConfigValue("JiraPassTransitionId");   // fallback
    String failId   = Initialize.getConfigValue("JiraFailTransitionId");   // fallback

    try {
        if ("PASS".equalsIgnoreCase(status) && passName != null && !passName.isBlank()) {
            JiraIntegration.transitionIssueByName(jiraKey, passName);
        } else if ("FAIL".equalsIgnoreCase(status) && failName != null && !failName.isBlank()) {
            JiraIntegration.transitionIssueByName(jiraKey, failName);
        } else {
            // fallback to IDs if names not configured
            String transitionId = "PASS".equalsIgnoreCase(status) ? passId : failId;
            JiraIntegration.transitionIssue(jiraKey, transitionId);
        }
    } catch (Exception tEx) {
        System.err.println("Transition failed for " + jiraKey + ": " + tEx.getMessage());
    }

    // 4) Extent link (unchanged)
    String jiraLink = Initialize.getConfigValue("JiraURL") + "browse/" + jiraKey;
    if (ExtentReporter.getTest() != null) {
        ExtentReporter.getTest().info("Jira Issue: <a href='" + jiraLink + "'>" + jiraKey + "</a>");
    }
    System.out.println("✅ Jira updated for " + jiraKey + " with status: " + status);
}


    public static void logJiraLink(String jiraKey) {
        String jiraLink = jiraBaseUrl() + "browse/" + jiraKey;
        if (ExtentReporter.getTest() != null) {
            ExtentReporter.getTest().info("Jira Issue: <a href='" + jiraLink + "'>" + jiraKey + "</a>");
        }
    }

    /** Main workflow */
    public static void main(String[] args) throws Exception {
        ExtentReports extent = null;
        try {
            // Load config only (driver/login is now handled in stepdefinitions.Hooks per Scenario)
            Initialize.loadConfig();

            // Initialize ExtentReports
            extent = new ExtentReports();
            String reportPath = System.getProperty("user.dir") + "/reports/JiraExecutionReport.html";
            ExtentReporter.setExtent(extent, reportPath);
            ExtentReporter.startTest("Jira Execution", "Automated Jira Test Run");

            List<String> jiraTests = fetchJiraTests();
            for (String jiraKey : jiraTests) {
                try {
                    // Run the tagged Cucumber scenarios for this Jira key
                    boolean passed = executeTest(jiraKey);
                    String status = passed ? "PASS" : "FAIL";

                    updateJiraStatus(jiraKey, status);
                    logJiraLink(jiraKey);

                    if (passed) {
                        ExtentReporter.getTest().pass("Test Passed for Jira Key: " + jiraKey);
                    } else {
                        ExtentReporter.getTest().fail("Test Failed for Jira Key: " + jiraKey);
                    }
                } catch (Exception e) {
                    updateJiraStatus(jiraKey, "FAIL");
                    logJiraLink(jiraKey);
                    ExtentReporter.getTest().fail("Test Failed for Jira Key: " + jiraKey + " | Error: " + e.getMessage());
                    System.err.println("❌ Execution failed for " + jiraKey + ": " + e.getMessage());
                }
            }
        } finally {
            // Flush report
            ExtentReporter.endTest();
            if (extent != null) {
                extent.flush();
            }
        }
    }
}