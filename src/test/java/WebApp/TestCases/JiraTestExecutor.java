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

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.xml.XmlClass;

import com.aventstack.extentreports.ExtentReports;

import util.Initialize;
import util.JiraIntegration;
import util.ExtentReports.ExtentTestManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
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

    /** Execute Selenium test dynamically using TestNG with JiraKeyFilter */
    public static TestListenerAdapter executeTest(String jiraKey) {
        System.out.println("▶ Executing TestNG for Jira Key: " + jiraKey);
        System.setProperty("JiraKeyFilter", jiraKey);

        XmlSuite suite = new XmlSuite();
        suite.setName("DynamicSuite");

        XmlTest test = new XmlTest(suite);
        test.setName("DynamicTest");
        test.setXmlClasses(Collections.singletonList(new XmlClass(MultiProductTest.class))); // ✅ Class reference

        TestNG testNG = new TestNG();
        testNG.setXmlSuites(Collections.singletonList(suite));

        TestListenerAdapter tla = new TestListenerAdapter();
        testNG.addListener(tla);
        testNG.run();

        return tla;
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
    if (ExtentTestManager.getTest() != null) {
        ExtentTestManager.getTest().info("Jira Issue: <a href='" + jiraLink + "'>" + jiraKey + "</a>");
    }
    System.out.println("✅ Jira updated for " + jiraKey + " with status: " + status);
}


    public static void logJiraLink(String jiraKey) {
        String jiraLink = jiraBaseUrl() + "browse/" + jiraKey;
        if (ExtentTestManager.getTest() != null) {
            ExtentTestManager.getTest().info("Jira Issue: <a href='" + jiraLink + "'>" + jiraKey + "</a>");
        }
    }

    /** Main workflow */
    public static void main(String[] args) throws Exception {
        ExtentReports extent = null;
        try {
        	System.out.println("Class exists? " + MultiProductTest.class.getName());
            // Load config only (driver/login is handled in MultiProductTest @BeforeSuite)
            Initialize.loadConfig();

            // Initialize ExtentReports
            extent = new ExtentReports();
            String reportPath = System.getProperty("user.dir") + "/reports/JiraExecutionReport.html";
            ExtentTestManager.setExtent(extent, reportPath);
            ExtentTestManager.startTest("Jira Execution", "Automated Jira Test Run");

            List<String> jiraTests = fetchJiraTests();
            for (String jiraKey : jiraTests) {
                try {
                    // Run the suite for this Jira key
                    TestListenerAdapter tla = executeTest(jiraKey);

                    // Decide PASS/FAIL based on listener counters
                    boolean passed = tla.getPassedTests().size() > 0 && tla.getFailedTests().isEmpty();
                    String status = passed ? "PASS" : "FAIL";

                    updateJiraStatus(jiraKey, status);
                    logJiraLink(jiraKey);

                    if (passed) {
                        ExtentTestManager.getTest().pass("Test Passed for Jira Key: " + jiraKey);
                    } else {
                        ExtentTestManager.getTest().fail("Test Failed for Jira Key: " + jiraKey);
                    }
                } catch (Exception e) {
                    updateJiraStatus(jiraKey, "FAIL");
                    logJiraLink(jiraKey);
                    ExtentTestManager.getTest().fail("Test Failed for Jira Key: " + jiraKey + " | Error: " + e.getMessage());
                    System.err.println("❌ Execution failed for " + jiraKey + ": " + e.getMessage());
                }
            }
        } finally {
            // Flush report
            ExtentTestManager.endTest();
            if (extent != null) {
                extent.flush();
            }
        }
    }
}