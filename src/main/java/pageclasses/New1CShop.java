package pageclasses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.Common_Methods;
import utils.Initialize;
import utils.JiraIntegration;
import utils.ExtentReporter;

public class New1CShop extends Initialize {
    private static final Logger log = LoggerFactory.getLogger(New1CShop.class);

    private Common_Methods commonMethods;

    public New1CShop() {
        WebDriver driver = Initialize.getWebDriver(); // Thread-safe driver
        commonMethods = new Common_Methods();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    public void executeScenario(WebDriver driver, Map<String, String> testData) throws Exception {

        String TestCaseName = testData.get("TestCaseName");

        if ("Yes".equalsIgnoreCase(testData.get("Quote"))) {
            createQuote(driver, testData, TestCaseName);
        }

        if ("Yes".equalsIgnoreCase(testData.get("Policy"))) {
            createPolicy(driver, testData, TestCaseName);
        }
        if ("Yes".equalsIgnoreCase(testData.get("Renewal"))) {
            // Renewal=Yes always goes straight to the dedicated ShopRenewal
            // page class now - no more old single-path createRenewal() here.
            // ShopRenewal reads "Renewal Execution" (1-5) and runs ONLY the
            // matching renewalScriptN() method via its switch-case (e.g.
            // Renewal Execution=1 runs renewalScript1() only - nothing else).
            new ShopRenewal().executeRenewal(driver, testData, TestCaseName);
        }
        log.info("QuoteFlag: " + testData.get("Quote") + ", PolicyFlag: " + testData.get("Policy"));
    }

    private void createQuote(WebDriver driver, Map<String, String> testData, String TestCaseName) throws Exception {
        try {
            String screenshotPath;
            String strLookupcompany = testData.get("Look up Company");
            String strEmailAddress = testData.get("EmailAddress");
            String strAgencyNumber = testData.get("AgencyNumber");
            String strLegalTradingStatus = testData.get("LegalTradingStatus");
            String strCompanyName = testData.get("CompanyName");
            String strCorrespondanceAddress = testData.get("CorrespondanceAddress");
            String strYearBusinessEstablished = testData.get("YearBusinessEstablished");
            String strAnnualTurnover = testData.get("AnnualTurnover");
            String strEstimatedAnnualWageRoll = testData.get("EstimatedAnnualWageRoll");
            String strCoverStartDate = testData.get("CoverStartDate");
            String strTrade1 = testData.get("Trade1");
            String strNumberofEmployees = testData.get("NumberofEmployees");
            String strGeneralContents = testData.get("GeneralContents");
            String strAllOtherStock = testData.get("AllOtherStock");
            String strPercentageTurnover = testData.get("PercentageTurnover");
            String strBusinessYearEstablishedatProperty = testData.get("BusinessYearEstablishedatProperty");
            String strStockOfCigarette = testData.get("StockOfCigarette");
            String strStockOfWineAndSpirit = testData.get("StockOfWineAndSpirit");
            String strStockOfDVD_CD_VideoTapes = testData.get("StockOfDVD_CD_VideoTapes");
            String strStockOfAllOtherStock = testData.get("StockOfAllOtherStock");
            String strBusinessContents = testData.get("BusinessContents");
            String strBuildingsCoverAmount = testData.get("BuildingsCoverAmount");
            String strPropertyBuilt = testData.get("PropertyBuilt");
            String strPropertyLocation = testData.get("PropertyLocation");
            String strShopOccupied = testData.get("ShopOccupied");
            String strAdditionalSecurityAtPremises = testData.get("AdditionalSecurityAtPremises");
            String strxpathAdditionalSecurityAtPremises = strAdditionalSecurityAtPremises + "']";
            String strPhoneNumber = testData.get("PhoneNumber");
            String strEmail = testData.get("Email");
            String strCurrentOrPreviousInsurer = testData.get("Current/Previous Insurer");
            String strELTOYorN = testData.get("ELTOYorN");
            String strPaymentType = testData.get("PaymentType");
            String strExecution = testData.get("Execution");
            String NBPolicyRequired = testData.get("NBPolicyRequired");
            String PaymentType = testData.get("PaymentType");
            String strBusinessTelephone = testData.get("BusinessTelephone");
            String strCompRegNumber = testData.get("CompRegNumber");
            String strSortCode = testData.get("SortCode");
            String strAccountNumber = testData.get("AccountNumber");
            String strShopPropertybuild = testData.get("PropertyBuild");
            String strShopPropertyLocation = testData.get("PropertyLocation");

            ExtentReporter.getTest().info("Starting New1CShop flow for: " + "testCaseName");
            int rowIndex = commonMethods.getNextAvailableRowIndex(Initialize.getQPNumbersFilePath(), "Shop");
            commonMethods.scrollThePage(driver, "DOWN");
            commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_mouseoverToShop"));
            commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_ShopgetQuote"), 20);
            commonMethods.click(driver, getTestObjectPF("IBP_ShopgetQuote"));

            commonMethods.click(driver, getTestObjectPF("IBP_AddClientManually"));
            commonMethods.editAField(driver, getTestObjectPF("IBP_CompanyName"), strCompanyName);
            commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_CorrespondAdd"), 15);
            commonMethods.editAField(driver, getTestObjectPF("IBP_CorrespondAdd"), strCorrespondanceAddress);
            commonMethods.click(driver, getTestObjectPF("IBP_ClklookupAdd"));
            commonMethods.click(driver, getTestObjectPF("IBP_SelAddress"));
            commonMethods.editAField(driver, getTestObjectPF("IBP_InputEmailadd"), strEmailAddress);
            commonMethods.editAField(driver, getTestObjectPF("IBP_InputPhonenum"), strPhoneNumber);
            commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessYear"), strBusinessYearEstablishedatProperty);
            commonMethods.click(driver, getTestObjectPF("IBP_ClkdrpdwnAgencyNumber"));
            commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_sltAgencyNumber"), 15);
            commonMethods.click(driver, getTestObjectPF("IBP_sltAgencyNumber"));
            commonMethods.click(driver, getTestObjectPF("IBP_drpdwnTrade1"));
            commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_FillTrade1"), 15);
            commonMethods.editAField(driver, getTestObjectPF("IBP_FillTrade1"), strTrade1);
            commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelTrade1"), 15);
            commonMethods.click(driver, getTestObjectPF("IBP_SelTrade1"));

            screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop First Page");
            JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);

            commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
            commonMethods.scrollThePage(driver, "DOWN");
            commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_ConfirnStatement"));
            screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Second Page");
            JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);

            commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
            commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
            commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelectLegalTrade"), 15);
            commonMethods.click(driver, getTestObjectPF("IBP_SelectLegalTrade"));
            commonMethods.editAField(driver, getTestObjectPF("IBP_NumberofEmployee"), strNumberofEmployees);
            commonMethods.editAField(driver, getTestObjectPF("IBP_Annualturnover"), strAnnualTurnover);
            commonMethods.editAField(driver, getTestObjectPF("IBP_AnnualWageRoll"), strEstimatedAnnualWageRoll);
            screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Third Page");
            JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);

            commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
            commonMethods.click(driver, getTestObjectPF("IBP_InsuredERNYes"));
            screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Fourth Page");
            JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);

            commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
            commonMethods.editAField(driver, getTestObjectPF("IBP_PercentageTurnover"), strPercentageTurnover);
            commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_PropListedBuilding"));
            commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelPropListedBuilding"), 15);
            commonMethods.click(driver, getTestObjectPF("IBP_SelPropListedBuilding"));
            commonMethods.editAField(driver, getTestObjectPF("IBP_GeneralContents"), strGeneralContents);
            commonMethods.editAField(driver, getTestObjectPF("IBP_AllOtherStock"), strAllOtherStock);
            screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Fifth Page");
            JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);

            commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
            screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Sixth Page");
            JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
            commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));

            commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_QuoteRef"), 50);
            String QuoteRef = driver.findElement(By.xpath("//p[text()='Quote Ref']/span")).getText();
            log.info("Obtained Quote Ref ==>" + QuoteRef);
            ExtentReporter.getTest().info("Quote Ref captured: " + QuoteRef);
            commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "TestCaseName", TestCaseName);
            commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "QuoteNumber", QuoteRef);
            commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "Timestamp",
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
            screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Quote Page");
            JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);

        } catch (Exception e) {
            String screenshotPath = Common_Methods.takeScreenshot(driver);
            ExtentReporter.getTest().fail("Shop flow failed: " + e.getMessage())
                    .addScreenCaptureFromPath(screenshotPath);
            throw e;
        }
    }

    private void createPolicy(WebDriver driver, Map<String, String> testData, String TestCaseName) throws Exception {
        try {
            log.info(">>> Inside createPolicy()");
            int rowIndex = commonMethods.findOrCreateRowIndexByTestCaseName(Initialize.getQPNumbersFilePath(), "Shop", TestCaseName);
            String screenshotPath;
            String strCompanyName = testData.get("CompanyName");
            String strPaymentType = testData.get("PaymentType");
            String strExecution = testData.get("Execution");
            String NBPolicyRequired = testData.get("NBPolicyRequired");
            String PaymentType = testData.get("PaymentType");
            String strBusinessTelephone = testData.get("BusinessTelephone");
            String strCompRegNumber = testData.get("CompRegNumber");
            String strSortCode = testData.get("SortCode");
            String strAccountNumber = testData.get("AccountNumber");
            String strShopPropertybuild = testData.get("PropertyBuild");
            String strShopPropertyLocation = testData.get("PropertyLocation");
            String strEmailAddress = testData.get("EmailAddress");

            ExtentReporter.getTest().info("Starting New1CShop flow for: " + "testCaseName");

            commonMethods.click(driver, getTestObjectPF("IBP_ClkBuy"));
            commonMethods.click(driver, getTestObjectPF("IBP_Clkproceed"));

            if (PaymentType.equals("Monthly")) {
                commonMethods.click(driver, getTestObjectPF("IBP_DDPaymentYes"));
                commonMethods.click(driver, getTestObjectPF("IBP_RiskaccountNo"));
                commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessTelephone"), strBusinessTelephone);
                commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
                commonMethods.scrollThePage(driver, "DOWN");
                commonMethods.click(driver, getTestObjectPF("IBP_SellingofInsurance"));
                driver.findElement(By.xpath("//label[@data-testingonly-subidentifier='Yes']")).click();
                commonMethods.editAField(driver, getTestObjectPF("IBP_CompRegNo"), strCompRegNumber);
                commonMethods.editAField(driver, getTestObjectPF("IBP_AccountName"), strCompanyName);
                commonMethods.editAField(driver, getTestObjectPF("IBP_SortCode"), strSortCode);
                commonMethods.editAField(driver, getTestObjectPF("IBP_AccountNumber"), strAccountNumber);
                commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
            }

            if (PaymentType.equals("Annual")) {
                commonMethods.click(driver, getTestObjectPF("IBP_DDPaymentNo"));
                commonMethods.click(driver, getTestObjectPF("IBP_RiskaccountNo"));
                commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessTelephone"), strBusinessTelephone);
                commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessEmail"), strEmailAddress);
                commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
                commonMethods.click(driver, getTestObjectPF("IBP_Clkconfirm"));
            }

            String PolicyNumber = driver.findElement(By.xpath("(//h1[@class='startTitle topTitle'])[2]")).getText();
            log.info(PolicyNumber);
            String PolicyNo = PolicyNumber.substring(14);
            log.info("Policy Number ==>" + PolicyNo);
            ExtentReporter.getTest().info("Policy Number captured: " + PolicyNo);
            screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Policy_Number Page");
            JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);

            commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "PolicyNumber", PolicyNo);
            commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "Timestamp",
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
            log.info("-----Property Owner NB created Successfully-----");
        } catch (Exception e) {
            String screenshotPath = Common_Methods.takeScreenshot(driver);
            ExtentReporter.getTest().fail("Shop flow failed: " + e.getMessage())
                    .addScreenCaptureFromPath(screenshotPath);
            throw e;
        }
    }

    // createRenewal() removed - Renewal=Yes now always goes through
    // ShopRenewal.executeRenewal() above, which reads "Renewal Execution"
    // and dispatches to exactly one renewalScriptN() method. See ShopRenewal.java.
}