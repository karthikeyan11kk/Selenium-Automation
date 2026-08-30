package WebApp.Pages.IBP;


import java.util.Map;

import org.openqa.selenium.WebDriver;
import util.Common_Methods;
import util.Initialize;
import util.ExtentReports.ExtentTestManager;


public class LoginPage extends Initialize {
    private WebDriver driver;
    private Common_Methods commonMethods;

    // Constructor injection
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.commonMethods = new Common_Methods();
    }


    public void nigLogin(Map<String, String> testData) throws Exception {
        WebDriver driver = Initialize.getWebDriver();
        try {
        	String strRestrictedAccess = getTestDataPF("AccessRestricted");
            ExtentTestManager.getTest().info("Attempting login for user: " + getConfigValue("LoginUserName"));
            commonMethods.editAField(driver, getTestObjectPF("IBP_edtAccess"),strRestrictedAccess);
            commonMethods.click(driver, getTestObjectPF("IBP_btnSubmitAccess"));
//            commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_acceptCookies"), 5000);
//            boolean cookies = commonMethods.IsDisplayed(driver, getTestObjectPF("IBP_acceptCookies"));
//            if (cookies) {
//                commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_acceptCookies"));
//            }
            commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_edtUserName"), 5000);
            commonMethods.editAField(driver, getTestObjectPF("IBP_edtUserName"), getConfigValue("LoginUserName"));
            commonMethods.editAField(driver, getTestObjectPF("IBP_edtPassword"), getConfigValue("LoginPassword"));
            commonMethods.click(driver, getTestObjectPF("IBP_btnLogin"));
            commonMethods.takeScreenShotonSuccess(driver, "pass", "Login successful");
            ExtentTestManager.getTest().info("Login completed for user: " + getConfigValue("LoginUserName"));
        } catch (Exception e) {
            String screenshotPath = Common_Methods.takeScreenshot(driver);
            ExtentTestManager.getTest().fail("Login failed: " + e.getMessage()).addScreenCaptureFromPath(screenshotPath);
            throw e;
        }
    }
}