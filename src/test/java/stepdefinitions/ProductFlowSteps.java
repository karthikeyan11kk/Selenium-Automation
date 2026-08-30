package stepdefinitions;

import WebApp.Pages.IBP.Products.New1CPropertyOwners;
import WebApp.Pages.IBP.Products.New1CShop;
import framework.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import util.Common_Methods;
import util.Initialize;
import util.JiraIntegration;
import util.ExtentReports.ExtentTestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ports the body of MultiProductTest.executeTC(Map, Method) into Gherkin steps.
 *
 * Still reads rows from TestData.xls by TestCaseName across the sheets listed
 * in "activeSheets" (TestData.properties) - same source of truth as before,
 * just driven by the feature file's Scenario Outline instead of a TestNG
 * @DataProvider.
 */
public class ProductFlowSteps {

    private Map<String, String> testData;

    @Given("test case {string} data is loaded")
    public void loadTestData(String testCaseName) throws Exception {
        testData = findRowByTestCaseName(testCaseName);
        ScenarioContext.set(testData);

        String product = testData.get("ProductType");
        String jiraKey = testData.get("JiraKey");

        ExtentTestManager.startTest(testCaseName + " - " + product, "Jira Key: " + jiraKey);
    }

    @When("I execute the product flow for test case {string}")
    public void executeProductFlow(String testCaseName) throws Exception {
        String product = testData.get("ProductType");
        String executionFlag = testData.get("Execution");
        String jiraKey = testData.get("JiraKey");
        String testExecKey = testData.get("TestExecutionKey");

        System.out.println("\n==============================");
        System.out.println("Starting Test Case: " + testCaseName + " | Product: " + product + " | Jira: " + jiraKey);
        System.out.println("==============================");

        if (!"Yes".equalsIgnoreCase(executionFlag)) {
            System.out.println("Skipping test case: " + testCaseName + " (Execution flag is not Yes)");
            ExtentTestManager.getTest().info("Skipped: Execution flag is not Yes");
            return;
        }

        boolean quoteFlag = "Yes".equalsIgnoreCase(testData.get("Quote"));
        boolean policyFlag = "Yes".equalsIgnoreCase(testData.get("Policy"));
        boolean jiraEnabled = "Yes".equalsIgnoreCase(testData.get("JiraExecution"));

        List<Map<String, String>> stepResults = new ArrayList<>();
        List<String> steps = List.of(
                "Open Home", "Navigate to Product", "Fill Quote Details", "Calculate Premium", "Bind Policy");
        for (String stepName : steps) {
            Map<String, String> stepData = new HashMap<>();
            stepData.put("status", "PASS");
            stepData.put("actualResult", "As expected result");
            stepResults.add(stepData);
        }

        try {
            if ("Shop".equalsIgnoreCase(product)) {
                New1CShop flow = new New1CShop();
                if (policyFlag) {
                    Map<String, String> combinedFlags = new HashMap<>(testData);
                    combinedFlags.put("Quote", "Yes");
                    combinedFlags.put("Policy", "Yes");
                    flow.executeScenario(Initialize.getWebDriver(), combinedFlags);
                } else if (quoteFlag) {
                    flow.executeScenario(Initialize.getWebDriver(), setFlag(testData, "Policy", "No"));
                } else {
                    ExtentTestManager.getTest().warning("Neither Quote nor Policy is requested for: " + testCaseName);
                }
            } else if ("PO".equalsIgnoreCase(product)) {
                New1CPropertyOwners flow = new New1CPropertyOwners();
                if (policyFlag) {
                    Map<String, String> combinedFlags = new HashMap<>(testData);
                    combinedFlags.put("Quote", "Yes");
                    combinedFlags.put("Policy", "Yes");
                    flow.executeScenario(Initialize.getWebDriver(), combinedFlags);
                } else if (quoteFlag) {
                    flow.executeScenario(Initialize.getWebDriver(), setFlag(testData, "Policy", "No"));
                } else {
                    ExtentTestManager.getTest().warning("Neither Quote nor Policy is requested for: " + testCaseName);
                }
            } else {
                ExtentTestManager.getTest().warning("Unknown product type: " + product);
            }

            if (!jiraEnabled) {
                System.out.println("JiraExecution=No for " + testCaseName + " - skipping all Jira/Xray reporting.");
            } else if (testExecKey != null && !testExecKey.isBlank()) {
                try {
                    JiraIntegration.updateXrayStepResults(testExecKey, jiraKey, stepResults);
                    System.out.println("Xray step results updated for " + jiraKey + " in " + testExecKey);
                } catch (Exception xrWarn) {
                    System.err.println("Xray update failed: " + xrWarn.getMessage());
                    ExtentTestManager.getTest().warning("Xray update failed: " + xrWarn.getMessage());
                }
            } else {
                System.out.println("No TestExecutionKey provided; skipping Xray step update for " + jiraKey);
            }

            String screenshotPath = Common_Methods.takeScreenshot(Initialize.getWebDriver());
            if (jiraEnabled && screenshotPath != null && !screenshotPath.isBlank()) {
                JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
                System.out.println("Evidence collected for Jira: " + testCaseName + " -> " + screenshotPath);
            }

            // Final PASS/FAIL Extent marking + Jira bug/comment handling now
            // happens in hooks.Hooks.afterScenario (ported from
            // ITestListener.onTestFailure/onTestSuccess), which reads
            // ScenarioContext to know the current tcName/jiraKey/testExecKey.

        } catch (Exception e) {
            for (Map<String, String> step : stepResults) {
                step.put("status", "FAIL");
                step.put("actualResult", "Step failed");
            }
            if (jiraEnabled && testExecKey != null && !testExecKey.isBlank()) {
                try {
                    JiraIntegration.updateXrayStepResults(testExecKey, jiraKey, stepResults);
                    System.out.println("Xray step results updated as FAIL for " + jiraKey);
                } catch (Exception xrEx) {
                    System.err.println("Xray update failed: " + xrEx.getMessage());
                }
            }

            String screenshotPath = Common_Methods.takeScreenshot(Initialize.getWebDriver());
            if (jiraEnabled) {
                try {
                    JiraIntegration.attachScreenshot(jiraKey, screenshotPath);
                } catch (Exception jiraEx) {
                    ExtentTestManager.getTest().warning("Jira update failed on error: " + jiraEx.getMessage());
                    System.err.println("Jira update failed during failure handling: " + jiraEx);
                }
            }

            throw e; // Hooks.afterScenario picks this up via scenario.isFailed()
        }
    }

    @Then("test case {string} should have completed")
    public void verifyCompleted(String testCaseName) {
        // Pass/fail Extent + Jira reporting for both outcomes is handled in
        // Hooks.afterScenario, ported from the old ITestListener methods -
        // nothing else to assert here unless you want an explicit status check.
    }

    /**
     * Delegates to framework.TestDataLookup (shared with Hooks, which uses
     * it to decide whether to launch a browser at all for this scenario).
     */
    private Map<String, String> findRowByTestCaseName(String testCaseName) throws Exception {
        Map<String, String> row = framework.TestDataLookup.findRowByTestCaseName(testCaseName);
        if (row == null) {
            throw new RuntimeException("No row found for TestCaseName=" + testCaseName
                    + " in any of the activeSheets: " + Initialize.getConfigValue("activeSheets"));
        }
        return row;
    }

    private Map<String, String> setFlag(Map<String, String> original, String key, String value) {
        Map<String, String> copy = new HashMap<>(original);
        copy.put(key, value);
        return copy;
    }
}
