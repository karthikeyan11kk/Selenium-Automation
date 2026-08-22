package WebApp.TestCases;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import org.testng.annotations.*;
import org.testng.ITestListener;
import org.testng.ITestResult;
import WebApp.Pages.IBP.LoginPage;
import WebApp.Pages.IBP.Products.New1CPropertyOwners;
import WebApp.Pages.IBP.Products.New1CShop;
import util.Common_Methods;
import util.Excel_Class;
import util.Initialize;
import util.JiraIntegration;
import util.ExtentReports.ExtentTestManager;

public class MultiProductTest extends Initialize implements ITestListener {

	@BeforeSuite
	public void setUpSuite() {
		try {
			Initialize.loadConfig(); // Load properties
			initializeDriver(null);
			String landingUrl = getTestDataPF("LoginUrl");
			Common_Methods commonMethods = new Common_Methods();
			commonMethods.openBrowser(getWebDriver(), landingUrl);
			new LoginPage(getWebDriver()).nigLogin(null);
			System.out.println("✅ Logged in successfully.");

//String token = JiraIntegration.getXrayAuthToken();
//System.setProperty("XrayBearerToken", token);
//JiraIntegration.obtainXrayBearerToken();
		} catch (Exception e) {
			System.err.println("❌ Setup failed: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Setup failed", e);
		}
	}

	@DataProvider(name = "allData")
	public Object[][] getAllData() throws Exception {
		Excel_Class XL = new Excel_Class();
		String[] sheets = getConfigValue("activeSheets").split(",");
		List<Map<String, String>> allData = new ArrayList<>();

		for (String sheet : sheets) {
			allData.addAll(XL.getAllData(getDataTablePath("TestData"), sheet.trim()));
		}

		// Filter rows where Execution = Yes
		allData = allData.stream().filter(row -> "Yes".equalsIgnoreCase(row.getOrDefault("Execution", "")))
				.collect(Collectors.toList());

		// 🔎 Optional: filter by JiraKey passed by JiraTestExecutor
		String jiraKeyFilter = System.getProperty("JiraKeyFilter");
		if (jiraKeyFilter != null && !jiraKeyFilter.isEmpty()) {
			allData = allData.stream().filter(row -> jiraKeyFilter.equalsIgnoreCase(row.getOrDefault("JiraKey", "")))
					.collect(Collectors.toList());
			System.out.println("▶ DataProvider filtered by JiraKey: " + jiraKeyFilter + " | Rows: " + allData.size());
		}
		Object[][] data = new Object[allData.size()][1];
		for (int i = 0; i < allData.size(); i++) {
			data[i][0] = allData.get(i);
		}
		return data;

	}

	@BeforeMethod
	public void navigateToHome() {
		try {
			String homeUrl = getTestDataPF("HomePageUrl");
			getWebDriver().navigate().to(homeUrl);
			System.out.println("✅ Navigated to Home Page before test.");
		} catch (Exception e) {
			System.err.println("❌ Failed to navigate to Home Page: " + e.getMessage());
		}
	}

	@AfterMethod
	public void recoverToHome(ITestResult result) throws Exception {
		if (!result.isSuccess()) {
			String homeUrl = getTestDataPF("HomePageUrl");
			getWebDriver().navigate().to(homeUrl);
			System.out.println("🔄 Recovery: Navigated to Home Page after failure.");
		}
	}





@Test(dataProvider = "allData")
public void executeTC(Map<String, String> testData, Method method) throws Exception {

    String product = testData.get("ProductType");
    String tcName = testData.get("TestCaseName");
    String executionFlag = testData.get("Execution");
    String jiraKey = testData.get("JiraKey");                // Xray Test issue key
    String testExecKey = testData.get("TestExecutionKey");   // Xray Test Execution key (add this column in Excel)

    System.out.println("\n==============================");
    System.out.println("▶ Starting Test Case: " + tcName + " | Product: " + product + " | Jira: " + jiraKey);
    System.out.println("==============================");

    ExtentTestManager.startTest(tcName + " - " + product, "Jira Key: " + jiraKey);

    // ✅ Skip if Execution flag is not Yes
    if (!"Yes".equalsIgnoreCase(executionFlag)) {
        System.out.println("⏭ Skipping test case: " + tcName + " (Execution flag is not Yes)");
        ExtentTestManager.getTest().info("Skipped: Execution flag is not Yes");
        return;
    }

    boolean quoteFlag = "Yes".equalsIgnoreCase(testData.get("Quote"));
    boolean policyFlag = "Yes".equalsIgnoreCase(testData.get("Policy"));

    // ✅ Prepare step results list for Xray
    List<Map<String, String>> stepResults = new ArrayList<>();
    List<String> steps = List.of(
        "Open Home",
        "Navigate to Product",
        "Fill Quote Details",
        "Calculate Premium",
        "Bind Policy"
    );

    // Default all steps as PASS initially
    for (String stepName : steps) {
        Map<String, String> stepData = new HashMap<>();
        stepData.put("status", "PASS");
        stepData.put("actualResult", "As expected result");
        stepResults.add(stepData);
    }

    try {
        // ✅ Execute product flow
        if ("Shop".equalsIgnoreCase(product)) {
            New1CShop flow = new New1CShop();
            if (policyFlag) {
                Map<String, String> combinedFlags = new HashMap<>(testData);
                combinedFlags.put("Quote", "Yes");
                combinedFlags.put("Policy", "Yes");
                flow.executeScenario(getWebDriver(), combinedFlags);
            } else if (quoteFlag) {
                flow.executeScenario(getWebDriver(), setFlag(testData, "Policy", "No"));
            } else {
                ExtentTestManager.getTest().warning("Neither Quote nor Policy is requested for: " + tcName);
            }
        } else if ("PO".equalsIgnoreCase(product)) {
            New1CPropertyOwners flow = new New1CPropertyOwners();
            // Apply same logic if needed
        } else {
            ExtentTestManager.getTest().warning("Unknown product type: " + product);
        }

        // ✅ Push step results to Xray (warn-only)
        if (testExecKey != null && !testExecKey.isBlank()) {
            try {
                JiraIntegration.updateXrayStepResults(testExecKey, jiraKey, stepResults);
                System.out.println("✅ Xray step results updated for " + jiraKey + " in " + testExecKey);
            } catch (Exception xrWarn) {
                System.err.println("⚠ Xray update failed: " + xrWarn.getMessage());
                ExtentTestManager.getTest().warning("Xray update failed: " + xrWarn.getMessage());
            }
        } else {
            System.out.println("ℹ️ No TestExecutionKey provided; skipping Xray step update for " + jiraKey);
        }

        // ✅ Capture final screenshot and evidence
        String screenshotPath = Common_Methods.takeScreenshot(getWebDriver());
        if (screenshotPath != null && !screenshotPath.isBlank()) {
            JiraIntegration.addEvidencePath(tcName, screenshotPath);
            System.out.println("✅ Evidence collected for Jira: " + tcName + " -> " + screenshotPath);
        }

        // ✅ Mark test as PASS in Extent
        ExtentTestManager.getTest().pass("Test Passed. Jira Issue: <a href='" +
                Initialize.getConfigValue("JiraURL") + "browse/" + jiraKey + "'>" + jiraKey + "</a>");

    } catch (Exception e) {
        // ❌ On Fail: Update all steps to FAIL
        for (Map<String, String> step : stepResults) {
            step.put("status", "FAIL");
            step.put("actualResult", "Step failed");
        }

        // Push FAIL step results to Xray
        if (testExecKey != null && !testExecKey.isBlank()) {
            try {
                JiraIntegration.updateXrayStepResults(testExecKey, jiraKey, stepResults);
                System.out.println("❌ Xray step results updated as FAIL for " + jiraKey);
            } catch (Exception xrEx) {
                System.err.println("❌ Xray update failed: " + xrEx.getMessage());
            }
        }

        // Capture screenshot and update Jira
        String screenshotPath = Common_Methods.takeScreenshot(getWebDriver());
        try {
        	JiraIntegration.attachScreenshot(jiraKey, screenshotPath);
            //JiraIntegration.addComment(jiraKey, "Test Failed: " + tcName + " | Error: " + e.getMessage());
            
        } catch (Exception jiraEx) {
            ExtentTestManager.getTest().warning("Jira update failed on error: " + jiraEx.getMessage());
            System.err.println("Jira update failed during failure handling: " + jiraEx);
        }

        ExtentTestManager.getTest().fail("Test Failed. Jira Issue: <a href='" +
                Initialize.getConfigValue("JiraURL") + "browse/" + jiraKey + "'>" + jiraKey + "</a>");
        throw e; // Keep functional failure semantics
    }
}
	
	/**
	 * Small helper to override a single flag in testData without mutating original
	 * map.
	 */
	private Map<String, String> setFlag(Map<String, String> original, String key, String value) {
		Map<String, String> copy = new java.util.HashMap<>(original);
		copy.put(key, value);
		return copy;
	}




@Override
public void onTestFailure(ITestResult result) {
    try {
        @SuppressWarnings("unchecked")
        Map<String, String> testData = (Map<String, String>) result.getParameters()[0];

        final String tcName  = testData.getOrDefault("TestCaseName", result.getName());
        final String product = testData.getOrDefault("ProductType", "Unknown Product");

        // Keys from data row
        final String teKeyRaw = testData.getOrDefault("TestExecutionKey", ""); // plan TE, e.g., PVII-7969
        final String teKey    = teKeyRaw == null ? "" : teKeyRaw.trim();
        final String targetIssueKey = testData.getOrDefault("JiraKey", "").trim(); // the Test issue, e.g., PVII-7860

        // Base URL once (avoid re-declaration later)
        final String jiraBase = Initialize.getConfigValue("JiraURL");

        final String errorMessage = (result.getThrowable() != null && result.getThrowable().getMessage() != null)
                ? result.getThrowable().getMessage()
                : "Unknown error";

        // ---- Collect ALL execution evidence (stored by tcName) ----
        java.util.List<String> evidencePaths = JiraIntegration.drainEvidence(tcName);

        // Ensure we have at least one "failed" screenshot at the moment of failure
        String finalScreenshot = Common_Methods.takeScreenshot(getWebDriver());
        if (finalScreenshot != null && !finalScreenshot.isBlank()) {
            evidencePaths.add(finalScreenshot);
        }

        // ---- Create the BUG ----
        String summary = "Automation Failure: " + tcName + " | Product: " + product;
        String bugKey  = JiraIntegration.createIssue(summary, errorMessage, java.util.List.of(), tcName, product);

        // ✅ Post a plain-text comment with full bug URL on the Test issue (PVII-7860)
        // Jira auto-links full URLs in v2 comments.
        if (!targetIssueKey.isBlank()) {
            String bugUrl = jiraBase + "browse/" + bugKey;
            JiraIntegration.addComment(
                targetIssueKey,
                "Failure captured in bug " + bugKey + ": " + summary + "\n" +
                "All failure evidence has been attached to the bug.\n" +
                "Bug Link: " + bugUrl
            );
        } else {
            System.err.println("⚠ JiraKey (target issue) is empty; skipped posting the bug link comment.");
        }

        // ---- (Optional) enrich description — links to bug page itself ----
        java.util.List<String> screenshotLinks = evidencePaths.stream()
                .map(path -> jiraBase + "browse/" + bugKey)
                .collect(java.util.stream.Collectors.toList());
        JiraIntegration.updateDescriptionWithLinks(bugKey, tcName, product, errorMessage, screenshotLinks);

        // === Attach ALL evidence to the BUG ===
        for (String path : evidencePaths) {
            try {
                JiraIntegration.attachScreenshot(bugKey, path);
            } catch (Exception attachEx) {
                System.err.println("⚠ Failed to attach evidence to bug " + bugKey + ": " + attachEx.getMessage());
            }
        }
        // --- (Optional) create a formal Jira issue link BUG ↔ TE ---
        try {
            if (!teKey.isBlank()) {
                String linkTypeName = Initialize.getConfigValue("JiraLinkTypeRelates"); // e.g., "Relates"
                if (linkTypeName != null && !linkTypeName.isBlank()) {
                    JiraIntegration.linkIssues(bugKey, teKey, linkTypeName);
                }
            }
        } catch (Exception linkEx) {
            System.err.println("⚠ Issue linking failed (Bug " + bugKey + " ↔ TE " + teKey + "): " + linkEx.getMessage());
        }

        System.out.println("✅ Jira Bug Created: " + bugKey
                + " | Test issue (comment target): " + (targetIssueKey.isBlank() ? "-" : targetIssueKey)
                + " | TE (plan): " + (teKey.isBlank() ? "-" : teKey)
                + " | Evidence attached to bug: " + evidencePaths.size());

    } catch (Exception e) {
        System.err.println("❌ Jira Integration Failed: " + e.getMessage());
    }
}

@Override
public void onTestSuccess(ITestResult result) {
    try {
        @SuppressWarnings("unchecked")
        Map<String, String> testData = (Map<String, String>) result.getParameters()[0];
        String tcName = testData.getOrDefault("TestCaseName", result.getName());
        String product = testData.getOrDefault("ProductType", "Unknown Product");
        String jiraKey = testData.getOrDefault("JiraKey", ""); // Existing Jira issue key

        // Collect screenshots
        List<String> screenshotPaths = JiraIntegration.drainEvidence(tcName);
        if (screenshotPaths.isEmpty()) {
            String finalScreenshot = Common_Methods.takeScreenshot(getWebDriver());
            if (finalScreenshot != null) screenshotPaths.add(finalScreenshot);
        }

        if (jiraKey != null && !jiraKey.isBlank()) {
            // ✅ Attach screenshots to existing Jira issue
            for (String path : screenshotPaths) {
                try {
                    JiraIntegration.attachScreenshot(jiraKey, path);
                } catch (Exception e) {
                    System.err.println("⚠ Failed to attach screenshot to Jira: " + e.getMessage());
                }
            }

            // ✅ Add comment to existing Jira issue
            try {
                JiraIntegration.addComment(jiraKey, "Test Passed: " + tcName + " | Attached " + screenshotPaths.size() + " screenshot(s).");
            } catch (Exception e) {
                System.err.println("⚠ Failed to add comment to Jira: " + e.getMessage());
            }

            System.out.println("✅ Attached " + screenshotPaths.size() + " screenshot(s) to Jira Issue: " + jiraKey);
        } else {
            System.err.println("⚠ JiraKey not provided for passed test: " + tcName);
        }
    } catch (Exception e) {
        System.err.println("⚠ Jira Integration Failed on success: " + e.getMessage());
    }
}





}
