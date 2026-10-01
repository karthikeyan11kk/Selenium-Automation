package pageclasses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import utils.Common_Methods;
import utils.Initialize;
import utils.ExtentReporter;

public class New1CPropertyOwners extends Initialize {
    private static final Logger log = LoggerFactory.getLogger(New1CPropertyOwners.class);

	
	private Common_Methods commonMethods;	

	public New1CPropertyOwners() {
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
    }
	
	 private void createQuote(WebDriver driver, Map<String, String> testData, String TestCaseName) throws Exception {
	    	
	        // Quote steps
	    	try {
	    	
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
	    		String strBusinessDescription= testData.get("BusinessDescription");
	    		String strPrimiseTypeBunglow=testData.get("PrimiseType");
	    		String strPrimiseUsePrivateDwelling=testData.get("PrimiseUse");
	    		String strResidentialOccupancy=testData.get("ResidentialOccupancy");
	    		String strNumberOfOccupants= testData.get("NumberOfOccupants");
	    		String strPrimiseBuiltYear= testData.get("PrimiseBuiltYear");
	    		String strPrimisePurchaseYear= testData.get("PrimisePurchaseYear");
	    		String strNoOfConcreteFloor= testData.get("NoOfConcreteFloor");
	    		String strNoOfWoodenFloor= testData.get("NoOfWoodenFloor");
	    		String strBuildingDeclaredValue= testData.get("BuildingDeclaredValue");
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

	    //to checking empty row to update Quote and policy Number		
	    		int rowIndex = commonMethods.getNextAvailableRowIndex(Initialize.getQPNumbersFilePath(), "PO");	
	    //Welcome Page------------------------------------------------------------------------------------------------
	    				if(strExecution.equals("Yes")) {
	    //Page1-------------------------------------------------------------------------------------------------------
	    	    		commonMethods.scrollThePage(driver,"DOWN");
	    	    		commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_mouseoverToPropertyOwners"));
	    	    		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_POgetQuote"), 15);
	    	    		commonMethods.click(driver, getTestObjectPF("IBP_POgetQuote"));
	    				
	    				commonMethods.click(driver, getTestObjectPF("IBP_AddClientManually"));
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_CompanyName"),strCompanyName);
//	    				commonMethods.waitForPageSettled(driver, 8);
//	    				commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
//	    				commonMethods.waitForPageSettled(driver, 12);
//	    				commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
//	    				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelectLegalTrade"), 15);
//	    				commonMethods.click(driver, getTestObjectPF("IBP_SelectLegalTrade"));
	    			
	    				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_CorrespondAdd"), 15);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_CorrespondAdd"),strCorrespondanceAddress);
	    				commonMethods.click(driver, getTestObjectPF("IBP_ClklookupAdd"));
	    				commonMethods.click(driver, getTestObjectPF("IBP_SelAddress"));
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_InputEmailadd"),strEmailAddress);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_InputPhonenum"),strPhoneNumber);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessYear"),strBusinessYearEstablishedatProperty);
	    				commonMethods.click(driver, getTestObjectPF("IBP_ClkdrpdwnAgencyNumber"));
	    				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_sltAgencyNumber"), 15);
	    				commonMethods.click(driver, getTestObjectPF("IBP_sltAgencyNumber"));
	    				
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO First Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    //Page2--------------------------------------------------------------------------------------------------
	    				commonMethods.scrollThePage(driver, "DOWN");
	    				commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_ConfirnStatement"));
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Second Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    //Page3-------------------------------------------------------------------------------------------------				
	    				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_ClickLegalTradeDropdown"), strTrade1);
	    				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_SelectBusinessDisciption"), strBusinessDescription);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessYearPO"),strBusinessYearEstablishedatProperty);
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Third Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    //Page4---------------------------------------------------------------------------------------------------------	
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_POPremisesPostcode"),strCorrespondanceAddress);
	    				commonMethods.click(driver, getTestObjectPF("IBP_POClklookupbutton"));
	    				commonMethods.click(driver, getTestObjectPF("IBP_POSelectAddress"));
	    				commonMethods.waitForPageSettled(driver, 5);
	    				commonMethods.click(driver, getTestObjectPF("IBP_POClkAddpremisebutton"));
	    				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_POSelectResidential"), 15);
	    				commonMethods.click(driver, getTestObjectPF("IBP_POSelectResidential"));
	    				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_POPrimiseTypeBunglow"), strPrimiseTypeBunglow);
	    				commonMethods.click(driver, getTestObjectPF("IBP_POHMO_NO"));
	    				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_POPrimiseuse"), strPrimiseUsePrivateDwelling);
	    						   				
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_PONumberofOccupants"),strNumberOfOccupants);
	    				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_POOccupanyType"), 5);
	    				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_POOccupanyType"), strResidentialOccupancy);
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_POPrimiseBuiltYear"),strPrimiseBuiltYear);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_POPrimisePurchaseYear"),strPrimisePurchaseYear);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_PONoOfConcreteFloors"),strNoOfConcreteFloor);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_PONoOfWoodenFloors"),strNoOfWoodenFloor);
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    				
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_POBuildingDeclaredValue"),strBuildingDeclaredValue);
	    				// Replaced 9x repeated "click Next / Thread.sleep(3000)" with one condition-based call - waits for the page to settle after each click instead of always sleeping the full 3s.
	    				commonMethods.clickRepeatedly(driver, "IBP_clkNext", 9, 10);
//	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    				
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Fourth Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    	//Page5-------------------------------------------------------------------------------------------------------
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Fifth Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    	//Page6-------------------------------------------------------------------------------------------------------
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Sixth Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    	//QuotePage--------------------------------------------------------------------------------------------------
	    			String QuoteRef = driver.findElement(By.xpath("//p[text()='Quote Ref']/span")).getText();
	    			log.info("Obtained Quote Ref ==>" + QuoteRef);
	    			//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "QuoteNumber", QuoteRef);
	    			//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, TcName, "QuoteNumber", QuoteRef);
	    			commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "TestCaseName", TestCaseName);
	    			commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "QuoteNumber", QuoteRef);
	    			commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "Timestamp", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
	    			commonMethods.fullScreenPrint(driver, testData, "New 1 C Property Owners", "Quote_Page");		
	    		
	    	}
	    	}
	    	catch (Exception e) {
    		    String screenshotPath = Common_Methods.takeScreenshot(driver);
    		    ExtentReporter.getTest().fail("PO flow failed: " + e.getMessage())
    		                      .addScreenCaptureFromPath(screenshotPath);
    		    throw e;
    		}
	 }
	 	
	 private void createPolicy(WebDriver driver, Map<String, String> testData, String TestCaseName) throws Exception {
	        // Policy steps
	    	try {
	    		
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
	    		String strBusinessDescription= testData.get("BusinessDescription");
	    		String strPrimiseTypeBunglow=testData.get("PrimiseType");
	    		String strPrimiseUsePrivateDwelling=testData.get("PrimiseUse");
	    		String strResidentialOccupancy=testData.get("ResidentialOccupancy");
	    		String strNumberOfOccupants= testData.get("NumberOfOccupants");
	    		String strPrimiseBuiltYear= testData.get("PrimiseBuiltYear");
	    		String strPrimisePurchaseYear= testData.get("PrimisePurchaseYear");
	    		String strNoOfConcreteFloor= testData.get("NoOfConcreteFloor");
	    		String strNoOfWoodenFloor= testData.get("NoOfWoodenFloor");
	    		String strBuildingDeclaredValue= testData.get("BuildingDeclaredValue");
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

	    //to checking empty row to update Quote and policy Number		
	    		int rowIndex = commonMethods.getNextAvailableRowIndex(Initialize.getQPNumbersFilePath(), "PO");	
	    //Welcome Page------------------------------------------------------------------------------------------------
	    				if(strExecution.equals("Yes")) {
	    				commonMethods.scrollThePage(driver,"DOWN");
	    				commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_mouseoverToPropertyOwners"));
	    //Page1-------------------------------------------------------------------------------------------------------
	    				commonMethods.click(driver, getTestObjectPF("IBP_AddClientManually"));
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_CompanyName"),strCompanyName);
	    				commonMethods.waitForPageSettled(driver, 8);
	    				commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
	    				commonMethods.waitForPageSettled(driver, 12);
	    				commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
	    				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelectLegalTrade"), 15);
	    				commonMethods.click(driver, getTestObjectPF("IBP_SelectLegalTrade"));
	    			
	    				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_CorrespondAdd"), 15);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_CorrespondAdd"),strCorrespondanceAddress);
	    				commonMethods.click(driver, getTestObjectPF("IBP_ClklookupAdd"));
	    				commonMethods.click(driver, getTestObjectPF("IBP_SelAddress"));
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_InputEmailadd"),strEmailAddress);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_InputPhonenum"),strPhoneNumber);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessYear"),strBusinessYearEstablishedatProperty);
	    				commonMethods.click(driver, getTestObjectPF("IBP_ClkdrpdwnAgencyNumber"));
	    				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_sltAgencyNumber"), 15);
	    				commonMethods.click(driver, getTestObjectPF("IBP_sltAgencyNumber"));
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_Coverstartdate"),strCoverStartDate);
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO First Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    //Page2--------------------------------------------------------------------------------------------------
	    				commonMethods.scrollThePage(driver, "DOWN");
	    				commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_ConfirnStatement"));
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Second Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    //Page3-------------------------------------------------------------------------------------------------				
	    				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_ClickLegalTradeDropdown"), strTrade1);
	    				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_SelectBusinessDisciption"), strBusinessDescription);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessYearPO"),strBusinessYearEstablishedatProperty);
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Third Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    //Page4---------------------------------------------------------------------------------------------------------	
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_POPremisesPostcode"),strCorrespondanceAddress);
	    				commonMethods.click(driver, getTestObjectPF("IBP_POClklookupbutton"));
	    				commonMethods.click(driver, getTestObjectPF("IBP_POSelectAddress"));
	    				commonMethods.waitForPageSettled(driver, 5);
	    				commonMethods.click(driver, getTestObjectPF("IBP_POClkAddpremisebutton"));
	    				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_POSelectResidential"), 15);
	    				commonMethods.click(driver, getTestObjectPF("IBP_POSelectResidential"));
	    				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_POPrimiseTypeBunglow"), strPrimiseTypeBunglow);
	    				commonMethods.click(driver, getTestObjectPF("IBP_POHMO_NO"));
	    				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_POPrimiseuse"), strPrimiseUsePrivateDwelling);
	    				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_POResidentialOccupanyType"), strResidentialOccupancy);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_PONumberofOccupants"),strNumberOfOccupants);
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_POPrimiseBuiltYear"),strPrimiseBuiltYear);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_POPrimisePurchaseYear"),strPrimisePurchaseYear);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_PONoOfConcreteFloors"),strNoOfConcreteFloor);
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_PONoOfWoodenFloors"),strNoOfWoodenFloor);
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    				commonMethods.editAField(driver, getTestObjectPF("IBP_POBuildingDeclaredValue"),strBuildingDeclaredValue);
	    				// Replaced 9x repeated "click Next / Thread.sleep(3000)" with one condition-based call - waits for the page to settle after each click instead of always sleeping the full 3s.
	    				commonMethods.clickRepeatedly(driver, "IBP_clkNext", 9, 5);
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Fourth Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    	//Page5-------------------------------------------------------------------------------------------------------
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Fifth Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    	//Page6-------------------------------------------------------------------------------------------------------
	    				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Sixth Page");
	    				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    	//QuotePage--------------------------------------------------------------------------------------------------
	    			String QuoteRef = driver.findElement(By.xpath("//p[text()='Quote Ref']/span")).getText();
	    			log.info("Obtained Quote Ref ==>" + QuoteRef);
	    			//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "QuoteNumber", QuoteRef);
	    			//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, TcName, "QuoteNumber", QuoteRef);
	    			commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "TestCaseName", TestCaseName);
	    			commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "QuoteNumber", QuoteRef);
	    			commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "Timestamp", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
	    			commonMethods.fullScreenPrint(driver, testData, "New 1 C Property Owners", "Quote_Page");	
	    		
	    	//Converting Quote into NB policy----------------------------------------------------------------------------	

	    					
	    					if (NBPolicyRequired.equals("Yes")) {
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
	    							commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "Additional Question Page");
	    							//((JavascriptExecutor) driver).executeScript("document.getElementById('C4A_btn1').style.display='none';");
	    							commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	    							//((JavascriptExecutor) driver).executeScript("document.getElementById('C4A_btn1').style.display='none';");
	    							commonMethods.click(driver, getTestObjectPF("IBP_Clkconfirm"));
	    							
	    						}
	    						
	    						String PolicyNumber = driver.findElement(By.xpath("(//h1[@class='startTitle topTitle'])[2]")).getText();
	    						log.info(PolicyNumber);
	    						String PolicyNo = PolicyNumber.substring(14);
	    						log.info("Policy Number ==>" + PolicyNo);
	    						commonMethods.fullScreenPrint(driver, testData, "New 1 C Property Owners", "Policy_Number_Page");
	    						//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "PolicyNumber", PolicyNo);
	    						//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, TcName, "PolicyNumber", PolicyNo);
	    						
	    						commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "PolicyNumber", PolicyNo);
	    						commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "Timestamp", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
	    						log.info("-----Property Owner NB created Successfully-----");
	    						commonMethods.scrollThePage(driver, "Top");
	    						commonMethods.click(driver, getTestObjectPF("IBP_HomeButton"));
	    						commonMethods.waitForPageSettled(driver, 5);
	    						
	    						
	    					}
	    		}
	 

	    		
	    		
		
	    	}
	    	catch (Exception e) {
    		    String screenshotPath = Common_Methods.takeScreenshot(driver);
    		    ExtentReporter.getTest().fail("PO flow failed: " + e.getMessage())
    		                      .addScreenCaptureFromPath(screenshotPath);
    		    throw e;
    		}
	 }
	
}	
	
	
	
/*	
	
	public void new1cpropertyowners(WebDriver driver, HashMap<String, String> testData, String TestCaseName) throws Exception {
//------------ used for to take data from Excel---------------------------------
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
		String strBusinessDescription= testData.get("BusinessDescription");
		String strPrimiseTypeBunglow=testData.get("PrimiseType");
		String strPrimiseUsePrivateDwelling=testData.get("PrimiseUse");
		String strResidentialOccupancy=testData.get("ResidentialOccupancy");
		String strNumberOfOccupants= testData.get("NumberOfOccupants");
		String strPrimiseBuiltYear= testData.get("PrimiseBuiltYear");
		String strPrimisePurchaseYear= testData.get("PrimisePurchaseYear");
		String strNoOfConcreteFloor= testData.get("NoOfConcreteFloor");
		String strNoOfWoodenFloor= testData.get("NoOfWoodenFloor");
		String strBuildingDeclaredValue= testData.get("BuildingDeclaredValue");
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

//to checking empty row to update Quote and policy Number		
		int rowIndex = commonMethods.getNextAvailableRowIndex(Initialize.getQPNumbersFilePath(), "PO");	
//Welcome Page------------------------------------------------------------------------------------------------
				if(strExecution.equals("Yes")) {
				commonMethods.scrollThePage(driver,"DOWN");
				commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_mouseoverToPropertyOwners"));
//Page1-------------------------------------------------------------------------------------------------------
				commonMethods.click(driver, getTestObjectPF("IBP_AddClientManually"));
				commonMethods.editAField(driver, getTestObjectPF("IBP_CompanyName"),strCompanyName);
				commonMethods.waitForPageSettled(driver, 8);
				commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
				commonMethods.waitForPageSettled(driver, 12);
				commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelectLegalTrade"), 15);
				commonMethods.click(driver, getTestObjectPF("IBP_SelectLegalTrade"));
			
				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_CorrespondAdd"), 15);
				commonMethods.editAField(driver, getTestObjectPF("IBP_CorrespondAdd"),strCorrespondanceAddress);
				commonMethods.click(driver, getTestObjectPF("IBP_ClklookupAdd"));
				commonMethods.click(driver, getTestObjectPF("IBP_SelAddress"));
				commonMethods.editAField(driver, getTestObjectPF("IBP_InputEmailadd"),strEmailAddress);
				commonMethods.editAField(driver, getTestObjectPF("IBP_InputPhonenum"),strPhoneNumber);
				commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessYear"),strBusinessYearEstablishedatProperty);
				commonMethods.click(driver, getTestObjectPF("IBP_ClkdrpdwnAgencyNumber"));
				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_sltAgencyNumber"), 15);
				commonMethods.click(driver, getTestObjectPF("IBP_sltAgencyNumber"));
				commonMethods.editAField(driver, getTestObjectPF("IBP_Coverstartdate"),strCoverStartDate);
				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO First Page");
				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//Page2--------------------------------------------------------------------------------------------------
				commonMethods.scrollThePage(driver, "DOWN");
				commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_ConfirnStatement"));
				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Second Page");
				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//Page3-------------------------------------------------------------------------------------------------				
				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_ClickLegalTradeDropdown"), strTrade1);
				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_SelectBusinessDisciption"), strBusinessDescription);
				commonMethods.editAField(driver, getTestObjectPF("IBP_BusinessYearPO"),strBusinessYearEstablishedatProperty);
				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Third Page");
				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//Page4---------------------------------------------------------------------------------------------------------	
				commonMethods.editAField(driver, getTestObjectPF("IBP_POPremisesPostcode"),strCorrespondanceAddress);
				commonMethods.click(driver, getTestObjectPF("IBP_POClklookupbutton"));
				commonMethods.click(driver, getTestObjectPF("IBP_POSelectAddress"));
				commonMethods.waitForPageSettled(driver, 5);
				commonMethods.click(driver, getTestObjectPF("IBP_POClkAddpremisebutton"));
				commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_POSelectResidential"), 15);
				commonMethods.click(driver, getTestObjectPF("IBP_POSelectResidential"));
				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_POPrimiseTypeBunglow"), strPrimiseTypeBunglow);
				commonMethods.click(driver, getTestObjectPF("IBP_POHMO_NO"));
				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_POPrimiseuse"), strPrimiseUsePrivateDwelling);
				commonMethods.selectDropDownByVisibleText(driver, getTestObjectPF("IBP_POResidentialOccupanyType"), strResidentialOccupancy);
				commonMethods.editAField(driver, getTestObjectPF("IBP_PONumberofOccupants"),strNumberOfOccupants);
				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
				commonMethods.editAField(driver, getTestObjectPF("IBP_POPrimiseBuiltYear"),strPrimiseBuiltYear);
				commonMethods.editAField(driver, getTestObjectPF("IBP_POPrimisePurchaseYear"),strPrimisePurchaseYear);
				commonMethods.editAField(driver, getTestObjectPF("IBP_PONoOfConcreteFloors"),strNoOfConcreteFloor);
				commonMethods.editAField(driver, getTestObjectPF("IBP_PONoOfWoodenFloors"),strNoOfWoodenFloor);
				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
				commonMethods.editAField(driver, getTestObjectPF("IBP_POBuildingDeclaredValue"),strBuildingDeclaredValue);
				// Replaced 9x repeated "click Next / Thread.sleep(3000)" with one condition-based call - waits for the page to settle after each click instead of always sleeping the full 3s.
				commonMethods.clickRepeatedly(driver, "IBP_clkNext", 9, 5);
				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Fourth Page");
				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	//Page5-------------------------------------------------------------------------------------------------------
				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Fifth Page");
				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	//Page6-------------------------------------------------------------------------------------------------------
				commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "PO Sixth Page");
				commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
	//QuotePage--------------------------------------------------------------------------------------------------
			String QuoteRef = driver.findElement(By.xpath("//p[text()='Quote Ref']/span")).getText();
			log.info("Obtained Quote Ref ==>" + QuoteRef);
			//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "QuoteNumber", QuoteRef);
			//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, TcName, "QuoteNumber", QuoteRef);
			commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "TestCaseName", TestCaseName);
			commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "QuoteNumber", QuoteRef);
			commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "Timestamp", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
			commonMethods.fullScreenPrint(driver, testData, "New 1 C Property Owners", "Quote_Page");	
		}
	//Converting Quote into NB policy----------------------------------------------------------------------------	

					
					if (NBPolicyRequired.equals("Yes")) {
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
							commonMethods.fullScreenPrint(driver, testData,"New 1 C Property Owners", "Additional Question Page");
							//((JavascriptExecutor) driver).executeScript("document.getElementById('C4A_btn1').style.display='none';");
							commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
							//((JavascriptExecutor) driver).executeScript("document.getElementById('C4A_btn1').style.display='none';");
							commonMethods.click(driver, getTestObjectPF("IBP_Clkconfirm"));
							
						}
						
						String PolicyNumber = driver.findElement(By.xpath("(//h1[@class='startTitle topTitle'])[2]")).getText();
						log.info(PolicyNumber);
						String PolicyNo = PolicyNumber.substring(14);
						log.info("Policy Number ==>" + PolicyNo);
						commonMethods.fullScreenPrint(driver, testData, "New 1 C Property Owners", "Policy_Number_Page");
						//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "PolicyNumber", PolicyNo);
						//commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, TcName, "PolicyNumber", PolicyNo);
						
						commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "PolicyNumber", PolicyNo);
						commonMethods.excelWriteMethod(Initialize.getQPNumbersFilePath(), "PO", rowIndex, "Timestamp", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
						log.info("-----Property Owner NB created Successfully-----");
						commonMethods.scrollThePage(driver, "Top");
						commonMethods.click(driver, getTestObjectPF("IBP_HomeButton"));
						commonMethods.waitForPageSettled(driver, 5);
						
						
					}
		}
}*/
