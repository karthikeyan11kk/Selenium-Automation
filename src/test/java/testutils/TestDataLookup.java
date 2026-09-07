package testutils;

import utils.Excel_Class;
import utils.Initialize;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared by Hooks and ProductFlowSteps so both look up TestData.xls rows the
 * same way. Added so Hooks can check the "Execution" flag for a scenario
 * BEFORE launching a browser at all - previously every scenario (including
 * ones marked Execution=No in the spreadsheet) still opened Edge and hit the
 * login/SSO page in @Before, only to have the actual product-flow logic
 * skipped afterward in the step definition. That's why "all the shops" were
 * visibly launching browsers even though only one PO test case was supposed
 * to run.
 */
public class TestDataLookup {

    private static final Pattern QUOTED_NAME = Pattern.compile("\"([^\"]+)\"");

    /**
     * Cucumber scenario names for these features are always of the form
     * `... test case "Shop_TC01_Quote"` (Scenario Outline title with the
     * Examples value substituted in) - pull the TestCaseName back out of it.
     */
    public static String extractTestCaseName(String scenarioName) {
        if (scenarioName == null) {
            return null;
        }
        Matcher m = QUOTED_NAME.matcher(scenarioName);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Same lookup MultiProductTest's old @DataProvider effectively did:
     * scan every "activeSheets" sheet in TestData.xls for a matching
     * TestCaseName. Returns null (not an exception) if not found, since
     * Hooks needs to fail open (launch the browser anyway) rather than
     * crash the whole run if a lookup goes wrong.
     */
    public static Map<String, String> findRowByTestCaseName(String testCaseName) throws Exception {
        Excel_Class xl = new Excel_Class();
        String[] sheets = Initialize.getConfigValue("activeSheets").split(",");
        for (String sheet : sheets) {
            for (Map<String, String> row : xl.getAllData(Initialize.getDataTablePath("TestData"), sheet.trim())) {
                if (testCaseName.equalsIgnoreCase(row.get("TestCaseName"))) {
                    return row;
                }
            }
        }
        return null;
    }
}
