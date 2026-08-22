package WebApp.Pages.IBP.Products;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import util.Common_Methods;
import util.Initialize;
public class ShopPdfValidation extends Initialize {
	
	public static WebDriver driver;
	public long vScriptStartTime = 0;
	private static Common_Methods commonMethods;
	
	public ShopPdfValidation(WebDriver driver) {
		ShopPdfValidation.driver = driver;
		commonMethods = new Common_Methods();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	public void shoppdfvalidation(WebDriver driver, HashMap<String, String> testData) throws Exception {
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
		int rowIndex = commonMethods.getNextAvailableRowIndex("C:\\Users\\UKAmarSapkal\\BRM_Agency\\New1C\\TestData\\Q&P-Numbers.xls", "Shop");

		commonMethods.scrollThePage(driver, "Top");
		commonMethods.click(driver, getTestObjectPF("IBP_Policysearch"));
		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_Policysearch2"), 5000);
		commonMethods.editAField(driver, getTestObjectPF("IBP_Policysearch2"),"RICA0000054");
		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_ClkViewClient"), 5000);
		commonMethods.click(driver, getTestObjectPF("IBP_ClkViewClient"));
		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_ClkView"), 5000);
		commonMethods.click(driver, getTestObjectPF("IBP_ClkView"));
		
		
		
		
		String PolicyNumber = driver.findElement(By.xpath("(//h1[@class='startTitle topTitle'])[2]")).getText();
		System.out.println(PolicyNumber);
		String PolicyNo = PolicyNumber.substring(14);
		System.out.println("Policy Number ==>" + PolicyNo);
		commonMethods.policyNumber = PolicyNo; // Store it for later use
		commonMethods.click(driver, getTestObjectPF("IBP_ClkPolicySchedule"));
		Thread.sleep(10000);
		//commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Policy_Number_Page");
		commonMethods.excelWriteMethod("C:\\Users\\UKAmarSapkal\\BRM_Agency\\New1C\\TestData\\Q&P-Numbers.xls", "Shop", rowIndex, "PolicyNumber", PolicyNo);
		System.out.println("-----Property Owner NB created Successfully-----");

		    // Get latest PDF and validate
		    String downloadDir = "C:\\Users\\UKAmarSapkal\\Downloads";
		    String latestPDFPath = commonMethods.getLatestDownloadedPDF(downloadDir);
		    System.out.println(latestPDFPath);
		    System.out.println("-----file found-----");
		    Map<String, String> expectedValues = new HashMap<>();
		    expectedValues.put("Policy Number", Common_Methods.policyNumber);
		    expectedValues.put("Policyholder", strCompanyName); // or from testData
		    expectedValues.put("Business", "Car Accessory Shop");
		    expectedValues.put("Premium", "£289.07");
		    expectedValues.put("Total Amount Due", "£323.76");
		    expectedValues.put("Sum Insured", "£11,500");
		    expectedValues.put("All Other Stock", "£10,000");
		    expectedValues.put("Business Interruption", "£500,000");
		    expectedValues.put("Employers’ Liability", "£10,000,000");
		    expectedValues.put("Public Liability", "£2,000,000");

		   
		   
		    System.out.println("-----file valdated successfully found-----");
		    boolean isPDFValid = commonMethods.validatePDFContent(latestPDFPath, expectedValues);
		    if (!isPDFValid) {
		        throw new Exception("PDF validation failed.");
		    }
		}
}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
/*		
//to checking empty row to update Quote and policy Number			
		int rowIndex = commonMethods.getNextAvailableRowIndex("C:\\Users\\UKAmarSapkal\\BRM_Agency\\New1C\\TestData\\Q&P-Numbers.xls", "Shop");
//Welcome Page
		if(strExecution.equals("Yes")) {
		commonMethods.scrollThePage(driver,"DOWN");
		commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_mouseoverToShop"));
//Page1-----------------------------------------------------------------------------------------------
		commonMethods.click(driver, getTestObjectPF("IBP_AddClientManually"));
		commonMethods.editAField(driver, getTestObjectPF("IBP_CompanyName"),strCompanyName);
		Thread.sleep(5000);
		commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
		Thread.sleep(10000);
		commonMethods.click(driver, getTestObjectPF("IBP_LegalTradingstatus"));
		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelectLegalTrade"), 5000);
		commonMethods.click(driver, getTestObjectPF("IBP_SelectLegalTrade"));
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
		commonMethods.editAField(driver, getTestObjectPF("IBP_Coverstartdate"),strCoverStartDate);
		commonMethods.click(driver, getTestObjectPF("IBP_drpdwnTrade1"));
		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_FillTrade1"), 5000);
		commonMethods.editAField(driver, getTestObjectPF("IBP_FillTrade1"), strTrade1);
		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelTrade1"), 5000);
		commonMethods.click(driver, getTestObjectPF("IBP_SelTrade1"));
		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop First Page");
		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//Page2---------------------------------------------------------------------------------------------
		commonMethods.scrollThePage(driver, "DOWN");
		commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_ConfirnStatement"));
		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Second Page");
		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//Page3-------------------------------------------------------------------------------------------------	
		commonMethods.editAField(driver, getTestObjectPF("IBP_NumberofEmployee"), strNumberofEmployees);
		commonMethods.editAField(driver, getTestObjectPF("IBP_Annualturnover"), strAnnualTurnover);
		commonMethods.editAField(driver, getTestObjectPF("IBP_AnnualWageRoll"), strEstimatedAnnualWageRoll);
		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Third Page");
		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//Page4----------------------------------------------------------------------------------------------------
		commonMethods.click(driver, getTestObjectPF("IBP_InsuredERNYes"));
		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Fourth Page");
		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//Page5----------------------------------------------------------------------------------------------------
		commonMethods.editAField(driver, getTestObjectPF("IBP_PercentageTurnover"), strPercentageTurnover);
		commonMethods.mouseHoverOnObjectAndClick(driver, getTestObjectPF("IBP_PropListedBuilding"));
		commonMethods.waitTillElementVisible(driver, getTestObjectPF("IBP_SelPropListedBuilding"), 5000);
		commonMethods.click(driver, getTestObjectPF("IBP_SelPropListedBuilding"));
		commonMethods.editAField(driver, getTestObjectPF("IBP_GeneralContents"), strGeneralContents);
		commonMethods.editAField(driver, getTestObjectPF("IBP_AllOtherStock"), strAllOtherStock);
		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Fifth Page");
		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//Page6------------------------------------------------------------------------------------------------------
		commonMethods.scrollThePage(driver, "DOWN");
		//commonMethods.fullScreenPrint(driver, testData,"New 1 C Shop", "Shop Sixth Page");
		commonMethods.click(driver, getTestObjectPF("IBP_clkNext"));
//QuotePage--------------------------------------------------------------------------------------------------
		String QuoteRef = driver.findElement(By.xpath("//p[text()='Quote Ref']/span")).getText();
		System.out.println("Obtained Quote Ref ==>" + QuoteRef);
		commonMethods.excelWriteMethod("C:\\Users\\UKAmarSapkal\\BRM_Agency\\New1C\\TestData\\Q&P-Numbers.xls", "Shop", rowIndex, "QuoteNumber", QuoteRef);
		//commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Quote_Page");	
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
					commonMethods.policyNumber = PolicyNo; // Store it for later use
					commonMethods.click(driver, getTestObjectPF("IBP_ClkPolicySchedule"));
					Thread.sleep(5000);
					//commonMethods.fullScreenPrint(driver, testData, "New 1 C Shop", "Policy_Number_Page");
					commonMethods.excelWriteMethod("C:\\Users\\UKAmarSapkal\\BRM_Agency\\New1C\\TestData\\Q&P-Numbers.xls", "Shop", rowIndex, "PolicyNumber", PolicyNo);
					System.out.println("-----Property Owner NB created Successfully-----");
					
					
				

					 // Extract premium and total amount from UI
					  //  String premiumText = driver.findElement(By.xpath("//xpath_to_premium")).getText();
					    //String totalAmountText = driver.findElement(By.xpath("//xpath_to_total_amount")).getText();

					    //Common_Methods.premiumAmount = premiumText;
					    //Common_Methods.totalAmountDue = totalAmountText;

					    // Get latest PDF and validate
					    String downloadDir = "C:\\Users\\UKAmarSapkal\\Downloads";
					    String latestPDFPath = commonMethods.getLatestDownloadedPDF(downloadDir);
					    System.out.println("-----file found-----");
					    Map<String, String> expectedValues = new HashMap<>();
					    expectedValues.put("Policy Number", Common_Methods.policyNumber);
					    expectedValues.put("Policyholder", strCompanyName); // or from testData
					   // expectedValues.put("Premium", Common_Methods.premiumAmount);
					  //  expectedValues.put("Total Amount Due", Common_Methods.totalAmountDue);
					    System.out.println("-----file valdated successfully found-----");
					    boolean isPDFValid = commonMethods.validatePDFContent(latestPDFPath, expectedValues);
					    if (!isPDFValid) {
					        throw new Exception("PDF validation failed for Shop product.");
					    }
					}

					
					
					commonMethods.scrollThePage(driver, "Top");
					commonMethods.click(driver, getTestObjectPF("IBP_HomeButton"));
					Thread.sleep(2000);
					
					
				}
	}
*/



