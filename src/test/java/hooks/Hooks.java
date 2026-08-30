package hooks;

import WebApp.Pages.IBP.LoginPage;
import framework.ScenarioContext;
import framework.TestDataLookup;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import util.Common_Methods;
import util.Initialize;
import util.JiraIntegration;
import util.ExtentReports.ExtentTestManager;

import java.util.List;
import java.util.Map;

/**
 * Ports MultiProductTest's TestNG lifecycle onto Cucumber:
 *
 *   @BeforeSuite setUpSuite()                 -> @BeforeAll (config load only - once per JVM)
 *   @BeforeMethod navigateToHome() + login     -> @Before (driver init + login, once per Scenario)
 *   @AfterMethod recoverToHome()               -> folded into @After below
 *   ITestListener.onTestFailure(result)        -> afterScenario(...) when scenario.isFailed()
 *   ITestListener.onTestSuccess(result)        -> afterScenario(...) when scenario passed
 *
 * Driver + login now happen per Scenario instead of once per suite. The
 * original @BeforeSuite login assumed one shared thread; Initialize.driver is
 * a ThreadLocal, so if scenarios ever run in parallel each thread needs its
 * own driver and its own logged-in session. testng.xml is left at
 * parallel="none" for now - flip that only once you've confirmed the app
 * tolerates concurrent logins.
 */
public class Hooks {

    @BeforeAll
    public static void setUpSuite() throws Exception {
        Initialize.loadConfig();
        // Xray bearer token bootstrap was commented out in the original
        // MultiProductTest - left disabled here to match:
        // String token = JiraIntegration.obtainXrayBearerToken();
        // System.setProperty("XrayBearerToken", token);
    }

    @Before
    public void beforeScenario(Scenario scenario) throws Exception {
        // Check the Excel "Execution" flag BEFORE launching a browser at all.
        // Previously every scenario opened Edge and hit the login/SSO page
        // here regardless of Execution, and only the actual product-flow
        // logic downstream respected the flag - so "skipped" test cases
        // still visibly launched a browser and went through SSO for nothing.
        String testCaseName = TestDataLookup.extractTestCaseName(scenario.getName());
        if (testCaseName != null) {
            try {
                Map<String, String> row = TestDataLookup.findRowByTestCaseName(testCaseName);
                if (row != null && !"Yes".equalsIgnoreCase(row.get("Execution"))) {
                    System.out.println("Execution=No for " + testCaseName + " - skipping browser launch and login.");
                    return;
                }
            } catch (Exception lookupEx) {
                System.err.println("Could not check Execution flag ahead of browser launch for "
                        + testCaseName + " - launching anyway: " + lookupEx.getMessage());
            }
        }

        Initialize.initializeDriver(null);
        System.out.println("Browser session started (SSO handled inside initializeDriver). Current URL: "
                + safeCurrentUrl(Initialize.getWebDriver()));

        String landingUrl = Initialize.getTestDataPF("LoginUrl");
        System.out.println("Navigating to LoginUrl: " + landingUrl);
        new Common_Methods().openBrowser(Initialize.getWebDriver(), landingUrl);
        System.out.println("After openBrowser(), current URL is: " + safeCurrentUrl(Initialize.getWebDriver()));
        if (!urlLooksLikeItNavigated(landingUrl, safeCurrentUrl(Initialize.getWebDriver()))) {
            System.err.println("WARNING: current URL doesn't look like it changed to LoginUrl. If the next "
                    + "steps fail, this is almost certainly why - check TestData.properties' LoginUrl value.");
        }

        new LoginPage(Initialize.getWebDriver()).nigLogin(null);
        System.out.println("Logged in successfully. Current URL after login: "
                + safeCurrentUrl(Initialize.getWebDriver()));

        String homeUrl = Initialize.getTestDataPF("HomePageUrl");
        System.out.println("Navigating to HomePageUrl: " + homeUrl);
        Initialize.getWebDriver().navigate().to(homeUrl);
        System.out.println("After navigating to HomePageUrl, current URL is: "
                + safeCurrentUrl(Initialize.getWebDriver()));
    }

    private String safeCurrentUrl(org.openqa.selenium.WebDriver driver) {
        try {
            return driver == null ? "(no driver)" : driver.getCurrentUrl();
        } catch (Exception e) {
            return "(could not read current URL: " + e.getMessage() + ")";
        }
    }

    private boolean urlLooksLikeItNavigated(String targetUrl, String currentUrl) {
        if (targetUrl == null || currentUrl == null) {
            return true; // can't judge - don't false-alarm
        }
        try {
            String targetHost = new java.net.URI(targetUrl).getHost();
            return targetHost == null || currentUrl.contains(targetHost);
        } catch (Exception e) {
            return true;
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                // Recovery navigation, same as the old recoverToHome(ITestResult).
                // Guarded on a non-null driver now: if @Before itself threw
                // (e.g. the browser session never started), there's no
                // driver to navigate and this used to log a confusing
                // NullPointerException on top of the real failure.
                if (Initialize.getWebDriver() != null) {
                    try {
                        String homeUrl = Initialize.getTestDataPF("HomePageUrl");
                        Initialize.getWebDriver().navigate().to(homeUrl);
                        System.out.println("Recovery: Navigated to Home Page after failure.");
                    } catch (Exception navEx) {
                        System.err.println("Recovery navigation failed: " + navEx.getMessage());
                    }
                }
                onTestFailure(scenario);
            } else {
                onTestSuccess(scenario);
            }
        } finally {
            ScenarioContext.clear();
            if (Initialize.getWebDriver() != null) {
                Initialize.getWebDriver().quit();
                // Initialize.initializeDriver() only creates a new driver when
                // getWebDriver() == null, so this thread's slot must be reset
                // after quit(), otherwise the next Scenario on this thread
                // would reuse a dead driver reference.
                Initialize.setWebDriver(null);
            }
        }
    }

    // ---- ported from MultiProductTest.onTestFailure(ITestResult) ----
    private void onTestFailure(Scenario scenario) {
        try {
            Map<String, String> testData = ScenarioContext.get();
            if (testData == null) {
                System.err.println("No test data in ScenarioContext; skipping Jira failure reporting for: " + scenario.getName());
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
                System.out.println("JiraExecution=No for " + tcName + " - skipping Jira bug creation/reporting.");
                if (ExtentTestManager.getTest() != null) {
                    ExtentTestManager.getTest().fail(errorMessage);
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
                System.err.println("JiraKey (target issue) is empty; skipped posting the bug link comment.");
            }

            List<String> screenshotLinks = evidencePaths.stream()
                    .map(path -> jiraBase + "browse/" + bugKey)
                    .collect(java.util.stream.Collectors.toList());
            JiraIntegration.updateDescriptionWithLinks(bugKey, tcName, product, errorMessage, screenshotLinks);

            for (String path : evidencePaths) {
                try {
                    JiraIntegration.attachScreenshot(bugKey, path);
                } catch (Exception attachEx) {
                    System.err.println("Failed to attach evidence to bug " + bugKey + ": " + attachEx.getMessage());
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
                System.err.println("Issue linking failed (Bug " + bugKey + " <-> TE " + teKey + "): " + linkEx.getMessage());
            }

            if (ExtentTestManager.getTest() != null) {
                ExtentTestManager.getTest().fail("Test Failed. Jira Issue: <a href='" + jiraBase
                        + "browse/" + targetIssueKey + "'>" + targetIssueKey + "</a> | Bug: " + bugKey);
            }

            System.out.println("Jira Bug Created: " + bugKey
                    + " | Test issue (comment target): " + (targetIssueKey.isBlank() ? "-" : targetIssueKey)
                    + " | TE (plan): " + (teKey.isBlank() ? "-" : teKey)
                    + " | Evidence attached to bug: " + evidencePaths.size());

        } catch (Exception e) {
            System.err.println("Jira Integration Failed: " + e.getMessage());
        }
    }

    // ---- ported from MultiProductTest.onTestSuccess(ITestResult) ----
    private void onTestSuccess(Scenario scenario) {
        try {
            Map<String, String> testData = ScenarioContext.get();
            if (testData == null) {
                System.err.println("No test data in ScenarioContext; skipping Jira success reporting for: " + scenario.getName());
                return;
            }

            String tcName = testData.getOrDefault("TestCaseName", scenario.getName());
            String jiraKey = testData.getOrDefault("JiraKey", "");

            if (!"Yes".equalsIgnoreCase(testData.get("JiraExecution"))) {
                System.out.println("JiraExecution=No for " + tcName + " - skipping Jira comment/attachment.");
                if (ExtentTestManager.getTest() != null) {
                    ExtentTestManager.getTest().pass("Test Passed.");
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
                        System.err.println("Failed to attach screenshot to Jira: " + e.getMessage());
                    }
                }
                try {
                    JiraIntegration.addComment(jiraKey, "Test Case Executed for : " + tcName + " With Attached "
                            + screenshotPaths.size() + " screenshot(s).");
                } catch (Exception e) {
                    System.err.println("Failed to add comment to Jira: " + e.getMessage());
                }
                System.out.println("Attached " + screenshotPaths.size() + " screenshot(s) to Jira Issue: " + jiraKey);
            } else {
                System.err.println("JiraKey not provided for passed test: " + tcName);
            }

            if (ExtentTestManager.getTest() != null) {
                String jiraBase = Initialize.getConfigValue("JiraURL");
                ExtentTestManager.getTest().pass("Test Passed. Jira Issue: <a href='" + jiraBase
                        + "browse/" + jiraKey + "'>" + jiraKey + "</a>");
            }
        } catch (Exception e) {
            System.err.println("Jira Integration Failed on success: " + e.getMessage());
        }
    }
}
