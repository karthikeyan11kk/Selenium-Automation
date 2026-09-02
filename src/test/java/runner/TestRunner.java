package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

import org.testng.annotations.DataProvider;

/**
 * Main suite entry point. "rerun:target/failed_scenarios.txt" in plugin
 * writes out any failed scenarios to a file that FailedTestRunner then
 * reads and re-executes on its own, in testng.xml's second <test> block -
 * matches the demo framework's TestRunner/FailedTestRunner pairing.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepdefinitions"},
        tags = "@regression",
        plugin = {
                "pretty",
                "html:target/cucumber-report/cucumber.html",
                "json:target/cucumber-report/cucumber.json",
                "rerun:target/failed_scenarios.txt"
        },
        monochrome = true,
        publish = false
)
public class TestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
