package WebApp.TestCases;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import WebApp.Pages.IBP.LoginPage;
import WebApp.Pages.IBP.Products.ShopPdfValidation;
import util.Common_Methods;
import util.Excel_Class;
import util.Initialize;
import util.ReportManager;
import util.ExtentReports.ExtentManager;
import util.ExtentReports.ExtentTestManager;

public class ShopPdfValidationTest extends Initialize implements ITestListener {
	

	private static Common_Methods commonMethods = null;
	public long vScriptStartTime = 0;
	@SuppressWarnings("unused")
	private static Excel_Class XL = null;

	public LoginPage lp;
	public ShopPdfValidation Spv;

	public static String dateFormat = ReportManager.getDateFormat(ReportManager.vDatetype8);
	//Local bsLocal = new Local();

	public ShopPdfValidationTest() {
		XL = new Excel_Class();
	}
	// BrowserStack updates
	public static final String USERNAME = "annudalmia1";
	public static final String AUTOMATE_KEY = "skvzWjxMDnnaqr5yX1ho";
	public static final String URL = "https://" + USERNAME + ":" + AUTOMATE_KEY + "@hub-cloud.browserstack.com/wd/hub";

	@AfterClass
	public void tearDownClass() {
		try {
			//bsLocal.stop();
			 ExtentManager.getReporter().flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
	public void ShopPdfValidation(Method method) throws Exception {
		HashMap<String, String> testData = (HashMap<String, String>) setUp(method, null);
		ReportManager RM = new ReportManager();
		if (testData.get("Execution").equalsIgnoreCase("Yes")) {
			WebDriver driver = initializeDriver(testData);
			appLogin(method, driver, testData);
			Spv = new ShopPdfValidation(driver);
			Spv.shoppdfvalidation(driver, testData);
			//driver.close();
		}	
	}

	public void appLogin(Method method, WebDriver driver, HashMap<String, String> testData) {
		try {
			
			String currentUrl = driver.getCurrentUrl();
			if (currentUrl.contains("https://intact.uat.uk.acturis.com/AWE/Container.aspx?CurrentWorkflow=Intact&CurrentStep=Home")) {	
            System.out.println("Already on Home Page. Skipping login.");
            return;
			}
			
			else {
			commonMethods = new Common_Methods();
			lp = new LoginPage(driver);
			XL = new Excel_Class();
			String landingUrl = getTestDataPF("LoginUrl");
			String strRestrictedAccess = getTestDataPF("AccessRestricted");
			commonMethods.openBrowser(driver, landingUrl);
			System.out.println("Landing URL : " + landingUrl);
			commonMethods.fullScreenPrint(driver, testData, "NIG_URL_Launched", "NIG_URL_Launched");
			commonMethods.editAField(driver, getTestObjectPF("IBP_edtAccess"),strRestrictedAccess);
			commonMethods.click(driver, getTestObjectPF("IBP_btnSubmitAccess"));
			lp.nigLogin(testData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test(description = "PDF", priority = 1)
	public void IBP_TC01_E2E_Shop(Method method) throws Exception {
		dateName = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(new Date());
		ExtentTestManager.startTest(method.getName(), "PDF");
		ShopPdfValidation(method);
		ExtentTestManager.endTest();
	
	}
}


