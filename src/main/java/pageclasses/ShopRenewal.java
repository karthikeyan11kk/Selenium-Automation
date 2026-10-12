package pageclasses;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import utils.Common_Methods;
import utils.Initialize;
import utils.JiraIntegration;
import utils.ExtentReporter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ShopRenewal {
    private static final Logger log = LoggerFactory.getLogger(ShopRenewal.class);

    private final Common_Methods commonMethods;

    public ShopRenewal() {
        this.commonMethods = new Common_Methods();
    }

    public void executeRenewal(WebDriver driver, Map<String, String> testData, String testCaseName) throws Exception {
        String renewalExecutionRaw = testData.get("Renewal Execution");

        if (renewalExecutionRaw == null || renewalExecutionRaw.trim().isEmpty()) {
            log.info("'Renewal Execution' not set for " + testCaseName
                    + " - nothing to run in ShopRenewal for this test case.");
            return;
        }

        int renewalExecution;
        try {
            renewalExecution = Integer.parseInt(renewalExecutionRaw.trim());
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("'Renewal Execution' value '" + renewalExecutionRaw
                    + "' for " + testCaseName + " is not a valid integer (expected 1-5).");
        }

        switch (renewalExecution) {
            case 4:
                renewalScript4(driver, testData, testCaseName);
                break;
            case 8:
                renewalScript8(driver, testData, testCaseName);
                break;
            case 9:
                renewalScript9(driver, testData, testCaseName);
                break;
            case 13:
                renewalScript13(driver, testData, testCaseName);
                break;
            case 16:
                renewalScript16(driver, testData, testCaseName);
                break;
            case 17:
                renewalScript17(driver, testData, testCaseName);
                break;
            default:
                throw new IllegalArgumentException("No renewal script defined for 'Renewal Execution'="
                        + renewalExecution + " (test case " + testCaseName + "). Valid values are 1-5.");
        }

        stampCompletionTimestamp(testCaseName);
    }


    private void openRenewalAndSearchPolicy(WebDriver driver, Map<String, String> testData, String testCaseName) throws Exception {
        String strRenewalPolicy = testData.get("Policy Number");
        String screenshotPath;

        ExtentReporter.getTest().info("ShopRenewal: starting renewal for " + testCaseName);

        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_RenewalTab"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Renewal Tab");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);

        commonMethods.editAField(driver, Initialize.getTestObjectPF("IBP_rnwl_RenewalPolicysearch"), strRenewalPolicy);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_FilterRenewal"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Renewal Invite List");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);

        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_ViewRenewalQuote"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Renewal Summary");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
    }

    /** Renewal Execution = 1: straight renewal, no amendment, no Direct Debit. */
    private void renewalScript4(WebDriver driver, Map<String, String> testData, String testCaseName) throws Exception {
        log.info("ShopRenewal: running renewalScript1 (straight renewal) for " + testCaseName);
        String screenshotPath;
        String strBusinessTelephone = testData.get("BusinessTelephone");
        String strCompRegNumber = testData.get("CompRegNumber");
        String strBusinessName = testData.get("BusinessName"); 
        String strSortCode = testData.get("SortCode");
        String strInvalidSortCode = testData.get("Invalid SortCode");
        String strAccountNumber = testData.get("AccountNumber");
        String strShopPropertybuild = testData.get("PropertyBuild");
        String strShopPropertyLocation = testData.get("PropertyLocation");
        String strEmailAddress = testData.get("EmailAddress");
        
        openRenewalAndSearchPolicy(driver,testData,testCaseName);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_BuyRenewalQuote"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_DDPaymentYes"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_RiskaccountNo"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Renewal Summary");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        commonMethods.scrollThePage(driver, "DOWN");
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_SellingofInsurance"));

        commonMethods.editAField(driver, Initialize.getTestObjectPF("IBP_rnwl_AccountName"), strBusinessName);
        commonMethods.editAField(driver, Initialize.getTestObjectPF("IBP_rnwl_SortCode"), strInvalidSortCode);
        commonMethods.editAField(driver, Initialize.getTestObjectPF("IBP_rnwl_AccountNumber"), strAccountNumber);
       
        
        
        commonMethods.editAField(driver, Initialize.getTestObjectPF("IBP_rnwl_CompRegNo"), strCompRegNumber);
//      commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        
        
        System.out.println("-------------------------------------------------");
        System.out.println("Script1");
        System.out.println("-------------------------------------------------");
    
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script1 - Policy Summary");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
    }

    private void renewalScript8(WebDriver driver, Map<String, String> testData, String testCaseName) throws Exception {
        log.info("ShopRenewal: running renewalScript5 (full renewal cycle) for " + testCaseName);
        String screenshotPath;
        
        openRenewalAndSearchPolicy(driver,testData,testCaseName);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_AmendRenewalQuote"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        commonMethods.mouseHoverOnObjectAndClick(driver,Initialize.getTestObjectPF("IBP_ConfirnStatement"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script8 - Child subsidance not present");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script8 - Loss History claim present");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script8 - Renewal quote documents present");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
    }

    private void renewalScript9(WebDriver driver, Map<String, String> testData, String testCaseName) throws Exception {
        log.info("ShopRenewal: running renewalScript5 (full renewal cycle) for " + testCaseName);
        String screenshotPath;
        
        openRenewalAndSearchPolicy(driver,testData,testCaseName);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_AmendRenewalQuote"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        commonMethods.mouseHoverOnObjectAndClick(driver,Initialize.getTestObjectPF("IBP_ConfirnStatement"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script9 - Loss History claim present");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_clkNext"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script9 - Renewal quote documents present");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_BuyRenewalQuote"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_DDPaymentNo"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_RiskaccountNo"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script9 - DD payment not visible with error");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
    }
    private void renewalScript13(WebDriver driver, Map<String, String> testData, String testCaseName) throws Exception {
        log.info("ShopRenewal: running renewalScript5 (full renewal cycle) for " + testCaseName);
        String screenshotPath;
        String premiumOverrideval="10";
        String CommissionOverrideval="20";
        openRenewalAndSearchPolicy(driver,testData,testCaseName);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_editCommission"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_rateCommission"));
        commonMethods.editAField(driver, Initialize.getTestObjectPF("IBP_rnwl_rateOverrideCommission"),CommissionOverrideval);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_calCommission"));
        	
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script13 - DD payment not visible with error");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_editPremium"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_discountPremium"));
        commonMethods.editAField(driver, Initialize.getTestObjectPF("IBP_rnwl_Overrideload"), premiumOverrideval);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_calPremium"));
        
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script13 - DD payment not visible with error");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
    }
    private void renewalScript16(WebDriver driver, Map<String, String> testData, String testCaseName) throws Exception {
        log.info("ShopRenewal: running renewalScript5 (full renewal cycle) for " + testCaseName);
        String screenshotPath;
        String premiumOverrideval="10";
        String CommissionOverrideval="20";
        openRenewalAndSearchPolicy(driver,testData,testCaseName);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_editCommission"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_rateCommission"));
        WebElement input = driver.findElement(By.xpath("//input[@id='mod_CommissionOverride_fe_QuotePremium_0_3_0_DiscountCommissionRateBy']"));
        input.clear();
        commonMethods.editAField(driver, Initialize.getTestObjectPF("IBP_rnwl_rateOverrideCommission"),CommissionOverrideval);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_calCommission"));
        	
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script9 - DD payment not visible with error");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_editPremium"));
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_discountPremium"));
        commonMethods.editAField(driver, Initialize.getTestObjectPF("IBP_rnwl_Overrideload"), premiumOverrideval);
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_calPremium"));
        
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script9 - DD payment not visible with error");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
    }
    
    private void renewalScript17(WebDriver driver, Map<String, String> testData, String testCaseName) throws Exception {
        log.info("ShopRenewal: running renewalScript5 (full renewal cycle) for " + testCaseName);
        String screenshotPath;
        openRenewalAndSearchPolicy(driver,testData,testCaseName);	
        commonMethods.click(driver, Initialize.getTestObjectPF("IBP_rnwl_editPremium"));
        screenshotPath = commonMethods.fullScreenPrint(driver, testData, "Shop Renewal", "Script9 - DD payment not visible with error");
        JiraIntegration.addEvidencePath(testCaseName, screenshotPath);
    }

    private void stampCompletionTimestamp(String testCaseName) {
        try {
            int rowIndex = Common_Methods.findOrCreateRowIndexByTestCaseName(
                    Initialize.getQPNumbersFilePath(), "Shop", testCaseName);
            Common_Methods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex,
                    "TestCaseName", testCaseName);
            Common_Methods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex,
                    "Timestamp", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        } catch (Exception e) {
            log.error("Could not stamp renewal completion timestamp for " + testCaseName + ": ", e);
        }
    }

}