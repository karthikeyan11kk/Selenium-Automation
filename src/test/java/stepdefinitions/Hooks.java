package stepdefinitions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pageclasses.LoginPage;
import testutils.ScenarioContext;
import testutils.TestDataLookup;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import utils.Common_Methods;
import utils.Initialize;
import utils.JiraIntegration;
import utils.MailReportSender;
import utils.QPNumbersLogger;
import utils.ExtentReporter;

import java.util.List;
import java.util.Map;

/**
 * Ports MultiProductTest's TestNG lifecycle onto Cucumber:
 *
 *   @BeforeSuite setUpSuite()                 -> @BeforeAll (config load + ONE-TIME driver launch + login)
 *   @BeforeMethod navigateToHome()             -> @Before (per Scenario: just navigate back to HomePageUrl)
 *   @AfterMethod recoverToHome()               -> folded into @After below
 *   ITestListener.onTestFailure(result)        -> afterScenario(...) when scenario.isFailed()
 *   ITestListener.onTestSuccess(result)        -> afterScenario(...) when scenario passed
 *   (new) @AfterAll                            -> quits the ONE shared driver, emails the report
 *
 * CHANGED: driver + login now happen ONCE in @BeforeAll instead of once per
 * Scenario - every test case in the run shares the same browser session and
 * the same login, only navigating back to HomePageUrl between test cases
 * rather than logging in again. This is a deliberate architecture change:
 * previously each Scenario got its own fresh driver+login (safer for
 * parallel execution, since Initialize's driver is a ThreadLocal), but this
 * suite runs sequentially (testng.xml has no parallel setting), so a shared
 * session is safe and avoids re-authenticating for every single test case.
 * If you ever turn on parallel execution, this needs to go back to a
 * per-thread driver+login instead.
 */
public class Hooks {
    private static final Logger log = LoggerFactory.getLogger(Hooks.class);


    @BeforeAll
    public static void setUpSuite() throws Exception {
        Initialize.loadConfig();
        // Xray bearer token bootstrap was commented out in the original
        // MultiProductTest - left disabled here to match:
        // String token = JiraIntegration.obtainXrayBearerToken();
        // System.setProperty("XrayBearerToken", token);

        configureCustomTrustStoreIfSet();

        log.info("Launching browser and logging in ONCE for the whole suite...");
        Initialize.initializeDriver(null);

        String landingUrl = Initialize.getTestDataPF("LoginUrl");
        new Common_Methods().openBrowser(Initialize.getWebDriver(), landingUrl);

        new LoginPage(Initialize.getWebDriver()).nigLogin(null);
        log.info("Logged in successfully - this session will be reused for every test case.");

        String homeUrl = Initialize.getTestDataPF("HomePageUrl");
        Initialize.getWebDriver().navigate().to(homeUrl);
        waitForHomePageReady();
    }

    /**
     * Fixes: the same xpath that used to work reliably started failing after
     * moving to a single shared login session. Root cause: beforeScenario()
     * used to unconditionally re-navigate to HomePageUrl for every scenario,
     * including the very FIRST one - but setUpSuite() had just navigated
     * there moments earlier. Two back-to-back navigations to the same SPA
     * URL can interrupt the page's own JS/dropdown-menu initialization
     * before it actually finishes, leaving menu items like "Get a quote"
     * not yet in the DOM when the test's real steps started. This waits for
     * a known, reliably-present home page element before returning control,
     * so callers can trust the page is genuinely ready, not just navigated.
     */
    private static void waitForHomePageReady() {
        try {
            new Common_Methods().waitTillElementVisible(Initialize.getWebDriver(),
                    Initialize.getTestObjectPF("IBP_mouseoverToShop"), 20);
        } catch (Exception e) {
            log.error("Home page readiness check timed out/failed (continuing anyway - this is a "
                    + "best-effort check, not a hard requirement): " + e.getMessage());
        }
    }

    /**
     * Fixes: "Xray update failed... certificate_unknown: PKIX path building
     * failed" and "Jira update failed... SSLHandshakeException: PKIX path
     * building failed" - both hitting DIFFERENT hosts (xray.cloud.getxray.app
     * AND rsainsurance-sandbox-242.atlassian.net), which confirms this is a
     * JVM-wide trust store gap, not a problem with either service. The
     * near-certain cause: a corporate proxy/firewall doing TLS inspection
     * (common on managed Windows machines) presents its OWN certificate for
     * HTTPS traffic, signed by an internal corporate root CA - one the
     * JDK's default cacerts has never heard of, so every outbound HTTPS call
     * this JVM makes fails the same way.
     *
     * This is opt-in and does nothing unless you set customTrustStorePath in
     * TestData.properties - it does NOT disable certificate checking (that
     * would be genuinely unsafe), it just lets the JVM trust an ADDITIONAL
     * certificate store you provide, on top of its own defaults.
     *
     * To actually fix this, you (or your IT team) need to:
     *   1. Export the corporate proxy's root CA certificate (ask IT, or in
     *      Chrome/Edge: click the padlock on any HTTPS site -> Connection is
     *      secure -> Certificate -> Certification Path -> select the TOP
     *      (root) certificate -> Details -> Copy to File -> Base-64 encoded X.509 (.CER)).
     *   2. Import it into a trust store (creates one if it doesn't exist):
     *        keytool -import -alias corporate-proxy-ca -file path\to\exported.cer -keystore path\to\customtruststore.jks -storepass changeit
     *   3. Set in TestData.properties:
     *        customTrustStorePath=C:\\path\\to\\customtruststore.jks
     *        customTrustStorePassword=changeit
     */
    private static void configureCustomTrustStoreIfSet() {
        try {
            String trustStorePath = Initialize.getTestDataPF("customTrustStorePath");
            if (trustStorePath == null || trustStorePath.trim().isEmpty()) {
                return; // not configured - use the JVM's own default trust store, do nothing
            }
            if (!new java.io.File(trustStorePath).exists()) {
                log.error("customTrustStorePath is set to '" + trustStorePath
                        + "' but that file doesn't exist - ignoring, Jira/Xray HTTPS calls will likely keep failing "
                        + "with a PKIX/certificate error until this is fixed.");
                return;
            }
            String trustStorePassword = Initialize.getTestDataPF("customTrustStorePassword");
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword",
                    trustStorePassword == null ? "" : trustStorePassword);
            log.info("Custom trust store configured: " + trustStorePath);
        } catch (Exception e) {
            log.error("Could not configure custom trust store (continuing with JVM defaults): " , e);
        }
    }

    @Before
    public void beforeScenario(Scenario scenario) throws Exception {
        // Check the Excel "Execution" flag before doing anything for this
        // scenario - if it's No, there's nothing to navigate/reset either.
        String testCaseName = TestDataLookup.extractTestCaseName(scenario.getName());
        if (testCaseName != null) {
            // Fresh screenshot subfolder for this test case, regardless of
            // whether it actually runs - keeps evidence organized the same
            // way every time. See Initialize.getScreenshotDir().
            Initialize.setCurrentScreenshotFolder(testCaseName);

            try {
                Map<String, String> row = TestDataLookup.findRowByTestCaseName(testCaseName);
                if (row != null && !"Yes".equalsIgnoreCase(row.get("Execution"))) {
                    log.info("Execution=No for " + testCaseName + " - skipping (browser stays open, no re-login).");
                    return;
                }
            } catch (Exception lookupEx) {
                log.error("Could not check Execution flag for "
                        + testCaseName + " - proceeding anyway: " + lookupEx.getMessage());
            }
        }

        // No driver/login here anymore - the @BeforeAll session is reused.
        // Only navigate if we're not ALREADY on the home page - this is
        // what avoids the redundant double-navigation for the very first
        // scenario (setUpSuite() just navigated there). For later
        // scenarios, the previous scenario's flow will have moved away
        // from home into the quote/policy screens, so this correctly
        // triggers a real reset navigation.
        if (Initialize.getWebDriver() != null) {
            String homeUrl = Initialize.getTestDataPF("HomePageUrl");
            String currentUrl = safeCurrentUrl(Initialize.getWebDriver());
            // NOTE: exact string equality doesn't work here - the app
            // rewrites the URL after arriving (HomePageUrl in config ends
            // "...CurrentStep=Login", but the browser's actual URL once
            // loaded becomes "...CurrentStep=Home&CurrentWorkflow=Intact...",
            // confirmed from console output). Checking for "CurrentStep=Home"
            // specifically is what actually detects "already on the home
            // page" reliably.
            boolean alreadyOnHome = currentUrl != null && currentUrl.contains("CurrentStep=Home");
            if (!alreadyOnHome) {
                Initialize.getWebDriver().navigate().to(homeUrl);
                waitForHomePageReady();
                log.info("Reset to HomePageUrl for test case: " + testCaseName
                        + " | Current URL: " + safeCurrentUrl(Initialize.getWebDriver()));
            } else {
                log.info("Already on HomePageUrl for test case: " + testCaseName
                        + " - skipping redundant navigation.");
            }
        }
    }


    private String safeCurrentUrl(org.openqa.selenium.WebDriver driver) {
        try {
            return driver == null ? "(no driver)" : driver.getCurrentUrl();
        } catch (Exception e) {
            return "(could not read current URL: " + e.getMessage() + ")";
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                // Recovery navigation, same as the old recoverToHome(ITestResult).
                if (Initialize.getWebDriver() != null) {
                    try {
                        String homeUrl = Initialize.getTestDataPF("HomePageUrl");
                        Initialize.getWebDriver().navigate().to(homeUrl);
                        log.info("Recovery: Navigated to Home Page after failure.");
                    } catch (Exception navEx) {
                        log.error("Recovery navigation failed: " , navEx);
                    }
                }
                onTestFailure(scenario);
            } else {
                onTestSuccess(scenario);
            }
        } finally {
            ScenarioContext.clear();
            // NOTE: driver is deliberately NOT quit here anymore - the same
            // browser session is reused across every scenario in the suite
            // now (see class-level comment). It's quit once in @AfterAll.
        }
    }

    /**
     * Runs once after every scenario in the suite has finished: quits the
     * one shared browser session, flushes the Extent report, and emails it
     * out to the configured recipients.
     */
    @AfterAll
    public static void tearDownSuite() {
        try {
            if (Initialize.getWebDriver() != null) {
                log.info("Suite finished - closing the shared browser session.");
                Initialize.getWebDriver().quit();
                Initialize.setWebDriver(null);
            }
        } catch (Exception e) {
            log.error("Error closing WebDriver at end of suite: " , e);
        }

        try {
            utils.ExtentManager.getReporter().flush();
        } catch (Exception e) {
            log.error("Error flushing Extent report: " , e);
        }

        // Feature 2: build the per-test-case screenshot+report folders,
        // the per-test-case Excel workbook (step-wise status, quote/policy
        // number, defect ID) and the overall execution report into a single
        // zip - produced automatically, every run, no manual step needed.
        String executionZipPath = null;
        try {
            executionZipPath = utils.ExecutionPackager.buildPackage();
        } catch (Exception e) {
            log.error("Error building execution package zip: " , e);
        }

        try {
            MailReportSender.sendReportEmail(executionZipPath);
        } catch (Exception e) {
            log.error("Error emailing report: " , e);
        }
    }

    // ---- ported from MultiProductTest.onTestFailure(ITestResult) ----
    private void onTestFailure(Scenario scenario) {
        try {
            Map<String, String> testData = ScenarioContext.get();
            if (testData == null) {
                log.error("No test data in ScenarioContext; skipping Jira failure reporting for: " + scenario.getName());
                return;
            }

            final String tcName = testData.getOrDefault("TestCaseName", scenario.getName());
            final String product = testData.getOrDefault("ProductType", "Unknown Product");
            final String teKeyRaw = testData.getOrDefault("TestExecutionKey", "");
            final String teKey = teKeyRaw == null ? "" : teKeyRaw.trim();
            final String targetIssueKey = testData.getOrDefault("JiraKey", "").trim();
            final String jiraBase = Initialize.getConfigValue("JiraURL");
            final String errorMessage = "Scenario failed: " + tcName;

            if (!"Yes".equalsIgnoreCase(testData.get("JiraExecution"))) {
                log.info("JiraExecution=No for " + tcName + " - skipping Jira bug creation/reporting.");
                if (ExtentReporter.getTest() != null) {
                    ExtentReporter.getTest().fail(errorMessage);
                }
                return;
            }

            List<String> evidencePaths = JiraIntegration.drainEvidence(tcName);
            String finalScreenshot = Common_Methods.takeScreenshot(Initialize.getWebDriver());
            if (finalScreenshot != null && !finalScreenshot.isBlank()) {
                evidencePaths.add(finalScreenshot);
            }

            String summary = "Automation Failure: " + tcName + " | Product: " + product;
            String bugKey = JiraIntegration.createIssue(summary, errorMessage, java.util.List.of(), tcName, product);

            if (!targetIssueKey.isBlank()) {
                String bugUrl = jiraBase + "browse/" + bugKey;
                JiraIntegration.addComment(targetIssueKey, "Failure captured in bug " + bugKey + ": " + summary + "\n"
                        + "All failure evidence has been attached to the bug.\n" + "Bug Link: " + bugUrl);
            } else {
                log.error("JiraKey (target issue) is empty; skipped posting the bug link comment.");
            }

            List<String> screenshotLinks = evidencePaths.stream()
                    .map(path -> jiraBase + "browse/" + bugKey)
                    .collect(java.util.stream.Collectors.toList());
            JiraIntegration.updateDescriptionWithLinks(bugKey, tcName, product, errorMessage, screenshotLinks);

            for (String path : evidencePaths) {
                try {
                    JiraIntegration.attachScreenshot(bugKey, path);
                } catch (Exception attachEx) {
                    log.error("Failed to attach evidence to bug " + bugKey + ": " , attachEx);
                }
            }

            try {
                if (!teKey.isBlank()) {
                    String linkTypeName = Initialize.getConfigValue("JiraLinkTypeRelates");
                    if (linkTypeName != null && !linkTypeName.isBlank()) {
                        JiraIntegration.linkIssues(bugKey, teKey, linkTypeName);
                    }
                }
            } catch (Exception linkEx) {
                log.error("Issue linking failed (Bug " + bugKey + " <-> TE " + teKey + "): " , linkEx);
            }

            // Update the TEST issue's own workflow STATUS to Fail. This was
            // missing before - the old code only ever added a comment to
            // targetIssueKey and created a separate bug, but never actually
            // transitioned the test issue itself, which is why Jira never
            // showed Pass/Fail. JiraFailTransitionName in TestData.properties
            // must match a real transition name in your Jira workflow.
            if (!targetIssueKey.isBlank()) {
                try {
                    String failTransitionName = Initialize.getConfigValue("JiraFailTransitionName");
                    if (failTransitionName != null && !failTransitionName.isBlank()) {
                        JiraIntegration.transitionIssueByName(targetIssueKey, failTransitionName);
                        log.info("Transitioned " + targetIssueKey + " to '" + failTransitionName + "'");
                    } else {
                        log.error("JiraFailTransitionName not set in TestData.properties - "
                                + "skipping status transition for " + targetIssueKey);
                    }
                } catch (Exception transitionEx) {
                    log.error("Failed to transition " + targetIssueKey + " to Fail: " , transitionEx);
                }
            }

            // Log the defect in Q&P-Numbers.xls with a timestamp, per product sheet.
            try {
                QPNumbersLogger.logDefect(product, tcName, bugKey, jiraBase + "browse/" + bugKey);
            } catch (Exception qEx) {
                log.error("Failed to log defect in Q&P-Numbers.xls: " , qEx);
            }

            // ALSO write the DefectID (and a Timestamp) directly into the
            // Shop/PO sheet's own row for this test case, right next to its
            // QuoteNumber/PolicyNumber - visible at a glance without cross-
            // referencing the separate Defects sheet above.
            try {
                QPNumbersLogger.setDefectId(product, tcName, bugKey);
            } catch (Exception qEx) {
                log.error("Failed to write DefectID into " + product + " sheet: " , qEx);
            }

            if (ExtentReporter.getTest() != null) {
                ExtentReporter.getTest().fail("Test Failed. Jira Issue: <a href='" + jiraBase
                        + "browse/" + targetIssueKey + "'>" + targetIssueKey + "</a> | Bug: " + bugKey);
            }

            log.info("Jira Bug Created: " + bugKey
                    + " | Test issue (comment target): " + (targetIssueKey.isBlank() ? "-" : targetIssueKey)
                    + " | TE (plan): " + (teKey.isBlank() ? "-" : teKey)
                    + " | Evidence attached to bug: " + evidencePaths.size());

        } catch (Exception e) {
            log.error("Jira Integration Failed: " , e);
        }
    }

    // ---- ported from MultiProductTest.onTestSuccess(ITestResult) ----
    private void onTestSuccess(Scenario scenario) {
        try {
            Map<String, String> testData = ScenarioContext.get();
            if (testData == null) {
                log.error("No test data in ScenarioContext; skipping Jira success reporting for: " + scenario.getName());
                return;
            }

            String tcName = testData.getOrDefault("TestCaseName", scenario.getName());
            String jiraKey = testData.getOrDefault("JiraKey", "");

            if (!"Yes".equalsIgnoreCase(testData.get("JiraExecution"))) {
                log.info("JiraExecution=No for " + tcName + " - skipping Jira comment/attachment.");
                if (ExtentReporter.getTest() != null) {
                    ExtentReporter.getTest().pass("Test Passed.");
                }
                return;
            }

            List<String> screenshotPaths = JiraIntegration.drainEvidence(tcName);
            if (screenshotPaths.isEmpty()) {
                String finalScreenshot = Common_Methods.takeScreenshot(Initialize.getWebDriver());
                if (finalScreenshot != null) screenshotPaths.add(finalScreenshot);
            }

            if (jiraKey != null && !jiraKey.isBlank()) {
                for (String path : screenshotPaths) {
                    try {
                        JiraIntegration.attachScreenshot(jiraKey, path);
                    } catch (Exception e) {
                        log.error("Failed to attach screenshot to Jira: " , e);
                    }
                }
                try {
                    JiraIntegration.addComment(jiraKey, "Test Case Executed for : " + tcName + " With Attached "
                            + screenshotPaths.size() + " screenshot(s).");
                } catch (Exception e) {
                    log.error("Failed to add comment to Jira: " , e);
                }
                log.info("Attached " + screenshotPaths.size() + " screenshot(s) to Jira Issue: " + jiraKey);

                // Update the TEST issue's own workflow STATUS to Pass - same
                // gap as the failure path had (see onTestFailure comment).
                try {
                    String passTransitionName = Initialize.getConfigValue("JiraPassTransitionName");
                    if (passTransitionName != null && !passTransitionName.isBlank()) {
                        JiraIntegration.transitionIssueByName(jiraKey, passTransitionName);
                        log.info("Transitioned " + jiraKey + " to '" + passTransitionName + "'");
                    } else {
                        log.error("JiraPassTransitionName not set in TestData.properties - "
                                + "skipping status transition for " + jiraKey);
                    }
                } catch (Exception transitionEx) {
                    log.error("Failed to transition " + jiraKey + " to Pass: " , transitionEx);
                }
            } else {
                log.error("JiraKey not provided for passed test: " + tcName);
            }

            if (ExtentReporter.getTest() != null) {
                String jiraBase = Initialize.getConfigValue("JiraURL");
                ExtentReporter.getTest().pass("Test Passed. Jira Issue: <a href='" + jiraBase
                        + "browse/" + jiraKey + "'>" + jiraKey + "</a>");
            }
        } catch (Exception e) {
            log.error("Jira Integration Failed on success: " , e);
        }
    }
}
