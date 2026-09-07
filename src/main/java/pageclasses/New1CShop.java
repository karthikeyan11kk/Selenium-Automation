package pageclasses;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.Common_Methods;
import utils.Initialize;
import utils.JiraIntegration;
import utils.ExtentReporter;

public class New1CShop extends Initialize {

    private Common_Methods commonMethods;
  
    public New1CShop() {
        WebDriver driver = Initialize.getWebDriver(); // Thread-safe driver
        commonMethods = new Common_Methods();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }
    public void executeScenario(WebDriver driver, Map<String, String> testData) throws Exception {
    
    	String TestCaseName = testData.get("TestCaseName");


        if ("Yes".equalsIgnoreCase(testData.get("Quote"))) {
            createQuote(driver, testData,TestCaseName);
        }

        if ("Yes".equalsIgnoreCase(testData.get("Policy"))) {
            createPolicy(driver, testData,TestCaseName);
        }
        if ("Yes".equalsIgnoreCase(testData.get("Renewal"))) {
            createRenewal(driver, testData,TestCaseName);
        }
        System.out.println("QuoteFlag: " + testData.get("Quote") + ", PolicyFlag: " + testData.get("Policy"));
    }

    private void createQuote(WebDriver driver, Map<String, String> testData, String TestCaseName) throws Exception {
    	
        // Quote steps
    	try {
    		String screenshotPath;
    		String strLookupcompany=testData.get("Look up Company");
    		String strEmailAddress=testData.get("EmailAddress");
    		String strAgencyNumber = testData.get("AgencyNumber");
    		String strLegalTradingStatus = testData.get("LegalTradingStatus");
    		String strCompanyName = testData.get("CompanyName");
    		String strCorrespondanceAddress = testData.get("CorrespondanceAddress");
    		String strYearBusinessEstablished = testData.get("YearBusinessEstablished");
    		String strAnnualTurnover = testData.get("AnnualTurnover");
    		String strEstimatedAnnualWageRoll = testData.get("EstimatedAnnualWageRoll");
    		String strCoverStartDate = testData.get("CoverStartDate");
    		String strTrade1 = testData.get("Trade1");
    		String strNumberofEmployees=testData.get("NumberofEmployees");
    		String strGeneralContents=testData.get("GeneralContents");
    		String strAllOtherStock=testData.get("AllOtherStock");
    		String strPercentageTurnover=testData.get("PercentageTurnover");
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
    		String strxpathAdditionalSecurityAtPremises = strAdditionalSecurityAtPremises+"']";
    		String strPhoneNumber = testData.get("PhoneNumber");
    		String strEmail = testData.get("Email");
    		String strCurrentOrPreviousInsurer = testData.get("Current/Previous Insurer");
    		String strELTOYorN = testData.get("ELTOYorN");
    		String strPaymentType = testData.get("PaymentType");
    		String strExecution = testData.get("Execution");
    		String NBPolicyRequired = testData.get("NBPolicyRequired");
    		String PaymentType = testData.get("PaymentType");
    		String strBusinessTelephone=testData.get("BusinessTelephone");
    		String strCompRegNumber = testData.get("CompRegNumber");
    		String strSortCode = testData.get("SortCode");
    		String strAccountNumber = testData.get("AccountNumber");
    		String strShopPropertybuild= testData.get("PropertyBuild");
    		String strShopPropertyLocation=testData.get("PropertyLocation");

    		ExtentReporter.getTest().info("Starting New1CShop flow for: " + "testCaseName");
    //to checking empty row to update Quote and policy Number			
    		int rowIndex = commonMethods.getNextAvailableRowIndex(Initialize.getQPNumbersFilePath(), "Shop");
    //Welcome Page
    		commonMethods.scrollThePage(driver,"DOWN");
    		commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_mouseoverToShop"));
    		// FIX attempt for "Unable to find element - //div[@class='next']/a[text()='Get a quote']":
    		// the hover above likely opens a dropdown/flyout menu, and this next
    		// click used to fire immediately with zero wait for it to render -
    		// a classic race condition. Added an explicit wait here; if the
    		// element still isn't found after this, the locator itself likely
    		// no longer matches the live page's DOM (a genuine app UI change)
    		// rather than a timing issue, and needs verifying against the real
    		// site directly.
//    		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_ShopgetQuote"), 10);
    		commonMethods.click(driver, getTestObjectPF("IBP_ShopgetQuote"));
    		
    //Page1-----------------------------------------------------------------------------------------------
    		commonMethods.click(driver, getTestObjectPF("IBP_AddClientManually"));
    		commonMethods.editAField(driver, getTestObjectPF("IBP_CompanyName"),strCompanyName);
    		Thread.sleep(5000);
//    		commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
//    		Thread.sleep(15000);
//    		commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
//    		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelectLegalTrade"), 5000);
//    		commonMethods.click(driver, getTestObjectPF("IBP_SelectLegalTrade"));
    		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_CorrespondAdd"), 5000);
    		commonMethods.editAField(driver, getTestObjectPF("IBP_CorrespondAdd"),strCorrespondanceAddress);
    		commonMethods.click(driver, getTestObjectPF("IBP_ClklookupAdd"));
    		commonMethods.click(driver, getTestObjectPF("IBP_SelAddress"));
    		commonMethods.editAField(driver, getTestObjectPF("IBP_InputEmailadd"),strEmailAddress);
    		commonMethods.editAField(driver, getTestObjectPF("IBP_InputPhonenum"),strPhoneNumber);
    		commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessYear"),strBusinessYearEstablishedatProperty);
    		commonMethods.click(driver, getTestObjectPF("IBP_ClkdrpdwnAgencyNumber"));
    		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_sltAgencyNumber"), 5000);
    		commonMethods.click(driver, getTestObjectPF("IBP_sltAgencyNumber"));
//    		commonMethods.editAField(driver, getTestObjectPF("IBP_Coverstartdate"),strCoverStartDate);
    		commonMethods.click(driver, getTestObjectPF("IBP_drpdwnTrade1"));
    		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_FillTrade1"), 5000);
    		commonMethods.editAField(driver, getTestObjectPF("IBP_FillTrade1"), strTrade1);
    		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelTrade1"), 5000);
    		commonMethods.click(driver, getTestObjectPF("IBP_SelTrade1"));
    	;
    		

    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop First Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);

    		
    		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
    //Page2---------------------------------------------------------------------------------------------
    		commonMethods.scrollThePage(driver, "DOWN");
    		commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_ConfirnStatement"));
    		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Second Page");
    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Second Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    		
    		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
    //Page3-------------------------------------------------------------------------------------------------
       		commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
//    		Thread.sleep(10000);
//    		commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
    		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelectLegalTrade"), 5000);
    		commonMethods.click(driver, getTestObjectPF("IBP_SelectLegalTrade"));
    		commonMethods.editAField(driver, getTestObjectPF("IBP_NumberofEmployee"), strNumberofEmployees);
    		commonMethods.editAField(driver, getTestObjectPF("IBP_Annualturnover"), strAnnualTurnover);
    		commonMethods.editAField(driver, getTestObjectPF("IBP_AnnualWageRoll"), strEstimatedAnnualWageRoll);
    		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Third Page");
    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Third Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    		
    		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
    //Page4----------------------------------------------------------------------------------------------------
    		commonMethods.click(driver, getTestObjectPF("IBP_InsuredERNYes"));
    		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Fourth Page");
    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Fourth Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    		
    		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
    //Page5----------------------------------------------------------------------------------------------------
    		commonMethods.editAField(driver, getTestObjectPF("IBP_PercentageTurnover"), strPercentageTurnover);
    		commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_PropListedBuilding"));
    		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelPropListedBuilding"), 5000);
    		commonMethods.click(driver, getTestObjectPF("IBP_SelPropListedBuilding"));
    		commonMethods.editAField(driver, getTestObjectPF("IBP_GeneralContents"), strGeneralContents);
    		commonMethods.editAField(driver, getTestObjectPF("IBP_AllOtherStock"), strAllOtherStock);
    		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Fifth Page");
    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Fifth Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    		
    		
    		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
    //Page6------------------------------------------------------------------------------------------------------
    		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Sixth Page");
    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Sixth Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
    //QuotePage--------------------------------------------------------------------------------------------------
    		commonMethods.waitTillElementVisible(driver,getTestObjectPF("IBP_QuoteRef"),5000);
    		String QuoteRef = driver.findElement(By.xpath("//p[text()='Quote Ref']/span")).getText();
    		System.out.println("Obtained Quote Ref ==>" + QuoteRef);
    		ExtentReporter.getTest().info("Quote Ref captured: " + QuoteRef);
    		//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "QuoteNumber", QuoteRef);
    		commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "TestCaseName", TestCaseName);
    		commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "QuoteNumber", QuoteRef);
    		commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "Timestamp",
    				new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
    		//commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Quote_Page");	
    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Quote Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    	
    	}
    		catch (Exception e) {
    		    String screenshotPath = Common_Methods.takeScreenshot(driver);
    		    ExtentReporter.getTest().fail("Shop flow failed: " + e.getMessage())
    		                      .addScreenCaptureFromPath(screenshotPath);
    		    throw e;
    		}
    }
    
    private void createPolicy(WebDriver driver, Map<String, String> testData, String TestCaseName) throws Exception {
        // Policy steps
    	try {
    		System.out.println(">>> Inside createPolicy()");
    		 //Reuse the SAME row createQuote() wrote TestCaseName/QuoteNumber to,
    		 //instead of grabbing a fresh empty row (see the comment on
    		 //findOrCreateRowIndexByTestCaseName in Common_Methods.java for why
    		 //this was landing PolicyNumber in a disconnected orphan row before).
    		int rowIndex = commonMethods.findOrCreateRowIndexByTestCaseName(Initialize.getQPNumbersFilePath(), "Shop", TestCaseName);
    		String screenshotPath;
    		String strCompanyName = testData.get("CompanyName");
    		String strPaymentType = testData.get("PaymentType");
    		String strExecution = testData.get("Execution");
    		String NBPolicyRequired = testData.get("NBPolicyRequired");
    		String PaymentType = testData.get("PaymentType");
    		String strBusinessTelephone=testData.get("BusinessTelephone");
    		String strCompRegNumber = testData.get("CompRegNumber");
    		String strSortCode = testData.get("SortCode");
    		String strAccountNumber = testData.get("AccountNumber");
    		String strShopPropertybuild= testData.get("PropertyBuild");
    		String strShopPropertyLocation=testData.get("PropertyLocation");
    		String strEmailAddress=testData.get("EmailAddress");
    		
    		ExtentReporter.getTest().info("Starting New1CShop flow for: " + "testCaseName");
  
    //Converting Quote into NB policy----------------------------------------------------------------------------	
    				
    					commonMethods.click(driver, getTestObjectPF("IBP_ClkBuy"));
    					commonMethods.click(driver, getTestObjectPF("IBP_Clkproceed"));
    				
    					if (PaymentType.equals("Monthly")) {
    						
    						commonMethods.click(driver, getTestObjectPF("IBP_DDPaymentYes"));
    						commonMethods.click(driver, getTestObjectPF("IBP_RiskaccountNo"));
    						commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessTelephone"), strBusinessTelephone);
    						//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Additional Question Page");
    						//((JavascriptExecutor) driver).executeScript("document.getElementById('C4A_btn1').style.display='none';");
    						commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
    						commonMethods.scrollThePage(driver, "DOWN");
    						driver.findElement(By.xpath("//label[@data-testingonly-subidentifier='Yes']")).click();
    						commonMethods.editAField(driver, getTestObjectPF("IBP_CompRegNo"), strCompRegNumber);
    						commonMethods.editAField(driver, getTestObjectPF("IBP_AccountName"), strCompanyName);
    						commonMethods.editAField(driver, getTestObjectPF("IBP_SortCode"), strSortCode);
    						commonMethods.editAField(driver, getTestObjectPF("IBP_AccountNumber"), strAccountNumber);
    						//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Direct Debit Page");
    						//((JavascriptExecutor) driver).executeScript("document.getElementById('C4A_btn1').style.display='none';");
    						commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
    					}
    					
    					if (PaymentType.equals("Annual")){
    						commonMethods.click(driver, getTestObjectPF("IBP_DDPaymentNo"));
    						commonMethods.click(driver, getTestObjectPF("IBP_RiskaccountNo"));
    						commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessTelephone"), strBusinessTelephone);
    						commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessEmail"), strEmailAddress);
    						//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Additional Question Page");
    						//((JavascriptExecutor) driver).executeScript("document.getElementById('C4A_btn1').style.display='none';");
    						commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
    						//((JavascriptExecutor) driver).executeScript("document.getElementById('C4A_btn1').style.display='none';");
    						commonMethods.click(driver, getTestObjectPF("IBP_Clkconfirm"));
    						
    					}
    					
    					String PolicyNumber = driver.findElement(By.xpath("(//h1[@class='startTitle topTitle'])[2]")).getText();
    					System.out.println(PolicyNumber);
    					String PolicyNo = PolicyNumber.substring(14);
    					System.out.println("Policy Number ==>" + PolicyNo);
    					ExtentReporter.getTest().info("Policy Number captured: " + PolicyNo);
    					//commonMethods.policyNumber = PolicyNo; // Store it for later use
    					//commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Policy_Number_Page");
    					//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "PolicyNumber", PolicyNo);
    					screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Policy_Number Page");
    		    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    					
    					commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "PolicyNumber", PolicyNo);
    					commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "Shop", rowIndex, "Timestamp",
    							new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
    					System.out.println("-----Property Owner NB created Successfully-----");			
    		}
    		catch (Exception e) {
    		    String screenshotPath = Common_Methods.takeScreenshot(driver);
    		    ExtentReporter.getTest().fail("Shop flow failed: " + e.getMessage())
    		                      .addScreenCaptureFromPath(screenshotPath);
    		    throw e;
    		}	
    }
    
    
    private void createRenewal(WebDriver driver, Map<String, String> testData, String TestCaseName) throws Exception {
    	
        // Quote steps
    	try {
    		String screenshotPath;
    		String strRenewalPolicy=testData.get("Policy Number");
    		String strRenewalAmend=testData.get("Renewal Amend");
    		String strRenewalAmendTrade=testData.get("Amend Renewal Trade");

    	
    		ExtentReporter.getTest().info("Starting New1CShop flow for: " + "testCaseName");

    		commonMethods.click(driver, getTestObjectPF("IBP_rnwl_RenewalTab"));
    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Renewal Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    		
    //Page1-----------------------------------------------------------------------------------------------
    		
    		commonMethods.editAField(driver, getTestObjectPF("IBP_rnwl_RenewalPolicysearch"),strRenewalPolicy);
    		
    		commonMethods.click(driver, getTestObjectPF("IBP_rnwl_FilterRenewal"));
    		
    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Renewal Invite List Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    		
    		commonMethods.click(driver, getTestObjectPF("IBP_rnwl_ViewRenewalQuote"));

    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Renewal Summary Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    		if(strRenewalAmend=="Yes")
    		{
    			
    			commonMethods.click(driver, getTestObjectPF("IBP_rnwl_AmendRenewalQuote"));
    			
    			commonMethods.click(driver, getTestObjectPF("IBP_drpdwnTrade1"));
        		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_FillTrade1"), 5000);
        		commonMethods.editAField(driver, getTestObjectPF("IBP_FillTrade1"), strRenewalAmendTrade);
        		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelTrade1"), 5000);
        		commonMethods.click(driver, getTestObjectPF("IBP_SelTrade1"));
        		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Renewal Amend Trade Page");
        		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
        		
           		commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_ConfirnStatement"));
        		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Second Page");
        		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Renewal Amend Second Page");
        		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
        		
        		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
        		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
        		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
        		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
        		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
        		
        		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Renewal Amend Summary Page");
        		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    			
    		}
    		
    		else {
    			commonMethods.click(driver, getTestObjectPF("IBP_rnwl_BuyRenewalQuote"));
    			
    			commonMethods.click(driver, getTestObjectPF("IBP_rnwl_NoDDRenewalPay"));
    			screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Renewal Payment Page");
        		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
        		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
        		commonMethods.click(driver, getTestObjectPF("IBP_Clkconfirm"));
 
        		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Shop Renewal Policy Summary Page");
        		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    			
    			
    		}
    		
    		
    		screenshotPath = commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Quote Page");
    		JiraIntegration.addEvidencePath(TestCaseName, screenshotPath);
    	
    	}
    		catch (Exception e) {
    		    String screenshotPath = Common_Methods.takeScreenshot(driver);
    		    ExtentReporter.getTest().fail("Shop flow failed: " + e.getMessage())
    		                      .addScreenCaptureFromPath(screenshotPath);
    		    throw e;
    		}
    }
    
    
    
}