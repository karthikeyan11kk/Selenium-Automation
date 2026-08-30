package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Replaces MultiProductTest as the TestNG entry point (see testng.xml).
 *
 * "tags" defaults to "@regression" so both feature files run together, same
 * as the old suite running MultiProductTest + ShopPdfValidationTest side by
 * side. Override at the command line for a narrower run, e.g.:
 *   mvn test -Dcucumber.filter.tags="@pdf"
 *   mvn test -Dcucumber.filter.tags="@JIRA-PVII-7860"
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"hooks", "stepdefinitions"},
        tags = "@regression",
        plugin = {
                "pretty",
                "html:target/cucumber-report/cucumber.html",
                "json:target/cucumber-report/cucumber.json"
        },
        monochrome = true,
        publish = false
)
public class TestRunner extends AbstractTestNGCucumberTests {

    @Override
    @org.testng.annotations.DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
