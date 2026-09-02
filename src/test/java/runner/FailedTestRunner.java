package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

import org.testng.annotations.DataProvider;

/**
 * Re-runs only the scenarios that failed in the main TestRunner pass, read
 * from target/failed_scenarios.txt (written by TestRunner's "rerun:" plugin
 * entry). Wired into testng.xml as a second <test> block after the main
 * run, matching the demo framework's TestRunner/FailedTestRunner pairing.
 *
 * If nothing failed, this file is empty and the rerun executes zero
 * scenarios - harmless, not an error.
 */
@CucumberOptions(
        features = {"@target/failed_scenarios.txt"},
        glue = {"stepdefinitions"},
        plugin = {
                "pretty",
                "html:target/cucumber-report/rerun-report.html",
                "json:target/cucumber-report/rerun-report.json"
        },
        monochrome = true,
        publish = false
)
public class FailedTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
