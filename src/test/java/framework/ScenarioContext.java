package framework;

import java.util.Map;

/**
 * Cucumber instantiates step-definition classes and the Hooks class separately
 * per scenario. This ThreadLocal is how ProductFlowSteps (which loads the
 * Excel row) hands the current test case's data - TestCaseName, JiraKey,
 * TestExecutionKey, etc - to Hooks (which needs it in @After to replicate the
 * old ITestListener.onTestFailure/onTestSuccess Jira reporting).
 *
 * Thread-safe by construction, so parallel scenario execution is still safe.
 */
public class ScenarioContext {

    private static final ThreadLocal<Map<String, String>> currentTestData = new ThreadLocal<>();

    public static void set(Map<String, String> testData) {
        currentTestData.set(testData);
    }

    public static Map<String, String> get() {
        return currentTestData.get();
    }

    public static void clear() {
        currentTestData.remove();
    }
}
