/*--------------------------------------------------------------------------------
COMMON FUNCTIONS  LIBRARY - (USER DEFINED FUNCTIONS)
--------------------------------------------------------------------------------*/
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import java.io.InputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.aventstack.extentreports.Status;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import util.ExtentReports.ExtentTestManager;

/* ***************************************************************************************
 PURPOSE: Common Actions performed on Web Application
 RETURN VALUE: None
 INPUT(s): Defined in individual Subclass Methods
 AUTHOR: Sadanand Biradar[sadanand.biradar@uk.rsagroup.com]
 DATE:
 *************************************************************************************** */
public class Common_Methods  extends Initialize{
	
	
	private static int Index = 0;
	
	@SuppressWarnings("unused")
	private String mainpage;
	public WebDriver popup;
	public enum Sel{id,name};
	private Actions DriverActions;
	@SuppressWarnings("unused")
	private Actions KeyboardActions;
	@SuppressWarnings("unused")
	private Actions KeyboardMouseActions;
	//private ReportManager RM = null;
	public static int stepCount = 1;
	private long startTransactionTime;
	private long endTransactionTime;
	private int startTransactionStep;
	private int endTransactionStep;
	public enum SeleniumIdentifiers 

	{className,cssSelector,id,linkText,name,partialLinkText,tagName,xpath, byIndex, byValue, byVisibleText, AndroidUIAutomator}
	
	public Common_Methods(){
		
	}

	static String[] splitTestObject(WebDriver driver, String TestObject) throws IOException{
		System.out.println("Action performed  ---" + Thread.currentThread().getStackTrace()[2].getMethodName()  + "  : Inside a method ---" + Thread.currentThread().getStackTrace()[3].getMethodName());
		String[] sTemp = null;
		try {
		 sTemp = (TestObject.split(",",3));
		System.out.println("Element name "+sTemp[0].toString());
		if (sTemp[0].contains("_")) {
			String[] sTemp1 = sTemp[0].split("_", 2);
			sTemp[0] = new String(sTemp1[1].trim());
				}
		sTemp[1] = new String(sTemp[1].trim());
		sTemp[2] = new String(sTemp[2].trim());
		}catch (Exception e) {
			checkException(driver, e, TestObject + "Object is not valid");
		}
		return sTemp;
		
	}

	static List<WebElement> checkElements(WebDriver driver, String Identifiers, String Locators) {
		SeleniumIdentifiers Identifier = SeleniumIdentifiers.valueOf(Identifiers);
		List<WebElement> Element = null;
		switch (Identifier) {
		case className: {
			try {
				Element = Initialize.getWebDriver().findElements(By.className(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case id: {
			try {
				Element = Initialize.getWebDriver().findElements(By.id(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case linkText: {
			try {
				Element = Initialize.getWebDriver().findElements(By.linkText(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case name: {
			try {
				Element = Initialize.getWebDriver().findElements(By.name(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case xpath: {
			System.out.println("inside xpath");
			try {
				Element = Initialize.getWebDriver().findElements(By.xpath(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		default:
			break;
			
		}
		return Element;
	}

	static WebElement checkElement(WebDriver driver, String Identifiers, String Locators) {

		SeleniumIdentifiers Identifier = SeleniumIdentifiers.valueOf(Identifiers);
		WebElement Element = null;
		//MobileElement Element = null;

		switch (Identifier) {
		case className: {
			try {
				Element = Initialize.getWebDriver().findElement(By.className(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case cssSelector: {
			try {
				Element = Initialize.getWebDriver().findElement(By.cssSelector(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case id: {
			try {
				Element = Initialize.getWebDriver().findElement(By.id(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case linkText: {
			try {
				Element = Initialize.getWebDriver().findElement(By.linkText(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case name: {
			try {
				Element = Initialize.getWebDriver().findElement(By.name(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case partialLinkText: {
			try {
				Element = Initialize.getWebDriver().findElement(By.partialLinkText(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case tagName: {
			try {
				Element = Initialize.getWebDriver().findElement(By.tagName(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		case xpath: {
			System.out.println("inside xpath");
			try {
				Element = Initialize.getWebDriver().findElement(By.xpath(Locators));
			} catch (Exception e) {
				checkException(driver, e, "Unable to find element - "+Locators);
			}
			break;
		}
		default:
			break;

		}
	return Element;

	}

	public static void StartOrEnd(String PageName, String StartOrEnd) throws IOException {

	    if (StartOrEnd.equalsIgnoreCase("Start")) {
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.INFO, "'" + PageName + "' - VERIFICATION START | ' " + PageName + " ' - VERIFICATION STARTED ");
	    } else if (StartOrEnd.equalsIgnoreCase("End")) {
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.INFO, "'" + PageName + "' - VERIFICATION END | ' " + PageName + " ' - VERIFICATION ENDED ");
	    } else if (StartOrEnd.equalsIgnoreCase("Error")) {
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.WARNING, "'" + PageName + "' - SHOULD BE VERIFIED | ' " + PageName + " ' - IS NOT VERIFIED DUE TO ERROR DURING EXECUTION");
	    }
	}

	public void openBrowser(WebDriver driver, String URL) throws IOException {
	    try {
	        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
	        Date date = new Date();
	        System.out.println(formatter.format(date));

	        Initialize.getWebDriver().navigate().to(URL); // launch Browser with the URL
	        Initialize.getWebDriver().get(URL);

	        ReportManager.logToExtent("INFO", "Action logged");

	        ExtentTestManager.getTest().log(Status.PASS, 
	            "Launch The URL - '" + URL + "' | Launched The URL - '" + URL + "' - SUCCESSFUL");
	    } catch (Exception e) {
	        checkException(driver, e, "Launch the URL '" + URL + "'");
	    }
	}
	
	public void editAField(WebDriver driver, String TestObject, String Value) throws IOException {
	    // ✅ Resolve placeholders before sending keys
	    if (Value != null && Value.toUpperCase().contains("TODAY()")) {
	        int offset = 0;
	        try {
	            if (Value.contains("+")) {
	                offset = Integer.parseInt(Value.split("\\+")[1].trim());
	            } else if (Value.contains("-")) {
	                offset = -Integer.parseInt(Value.split("-")[1].trim());
	            }
	        } catch (NumberFormatException e) {
	            System.err.println("Invalid offset in placeholder: " + Value);
	        }
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, offset);
	        Value = new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
	        System.out.println("Resolved placeholder in editAField: " + Value);
	    }

	    String[] sTemp = splitTestObject(driver, TestObject);
	    WebElement we = checkElement(driver, sTemp[1], sTemp[2]);
	    System.out.println("--Value : " + Value);

	    try {
	        we.isEnabled();
	        we.clear();
	        we.sendKeys(Value);

	        ExtentTestManager.getTest().log(Status.PASS,
	            "Entered Value in - " + sTemp[0] + " | Entered Value Is - " + Value + " - SUCCESSFUL");

	        ReportManager.logToExtent("INFO", "Action logged");
	    } catch (Exception e) {
	        checkException(driver, e, "Entered Value in " + sTemp[0] + " as '" + Value + "'");
	    }

	    sTemp = null;
	    Value = null;
	}
	
	public void editAFieldWithEncryptedData(String TestObject, String Encrypted_password) throws IOException {
	    byte[] decodedBytes = Base64.decodeBase64(Encrypted_password);

	    String[] sTemp = splitTestObject(Initialize.getWebDriver(), TestObject);
	    WebElement we = checkElement(Initialize.getWebDriver(), sTemp[1], sTemp[2]);

	    try {
	        we.clear();
	        we.sendKeys(new String(decodedBytes));

	        ReportManager.logToExtent("INFO", "Action logged");

	        ExtentTestManager.getTest().log(Status.PASS,
	            "Enter password - *********** | Entered password - *********** - SUCCESSFUL");
	    } catch (Exception e) {
	        checkException(Initialize.getWebDriver(), e, "Enter password - ***********");
	    }

	    sTemp = null;
	}

	// Decrypt
	public static String decryptData(String Encrypted_password) throws IOException {
	    byte[] decodedBytes = Base64.decodeBase64(Encrypted_password);
	    return new String(decodedBytes);
	}
	
	public static void encryptData(String Data) throws IOException {
	    byte[] encodedBytes = Base64.encodeBase64(Data.getBytes());
	    String STR = new String(encodedBytes);

	    System.out.println("Encrypted Data is - " + STR);

	    try {
	        ReportManager.logToExtent("INFO", "Action logged");

	        ExtentTestManager.getTest().log(Status.PASS,
	            "Actual Data is - '" + Data + "' | Encrypted Data is - '" + STR + "' - SUCCESSFUL");
	    } catch (Exception e) {
	        checkException(Initialize.getWebDriver(), e, "Actual Data should be Encrypted");
	    }
	}

public void click(WebDriver driver, String testObject) throws IOException, Exception {
	    Actions act = new Actions(driver);
	    String[] sTemp = splitTestObject(driver, testObject);
	    WebElement we = checkElement(driver, sTemp[1], sTemp[2]);

	    try {
	        waitTillElementVisible(driver, testObject, 15000);
	        we.click();

	        ReportManager.logToExtent("INFO", "Action logged");

	        ExtentTestManager.getTest().log(Status.PASS,
	            "Click On - '" + sTemp[0] + "' | Clicked On - '" + sTemp[0] + "' - SUCCESSFUL");
	    } catch (Exception e) {
	        System.out.println(e.toString());
	        checkException(driver, e, "Click On - '" + sTemp[0] + "'");
	    }

	    sTemp = null;
	    testObject = null;
	}
	
	public void clickByJS(WebDriver driver, By obj) throws IOException, Exception {
	    WebElement we = Initialize.getWebDriver().findElement(obj);

	    try {
	        JavascriptExecutor executor = (JavascriptExecutor) driver;
	        executor.executeScript("arguments[0].click();", we);

	        ReportManager.logToExtent("INFO", "Action logged");

	        ExtentTestManager.getTest().log(Status.PASS,
	            "Click On the element | Clicked On the element - SUCCESSFUL");
	    } catch (Exception e) {
	        System.out.println(e.toString());
	        checkException(driver, e, "Click On element is failed - " + obj);
	    }
	}

	public void checkBox(String TestObject, String status) throws IOException {
	    String[] sTemp = splitTestObject(Initialize.getWebDriver(), TestObject);
	    WebElement we = checkElement(Initialize.getWebDriver(), sTemp[1], sTemp[2]);

	    if (status.equalsIgnoreCase("check")) {
	        try {
	            if (!we.isSelected()) {
	                we.click();
	                ReportManager.logToExtent("INFO", "Action logged");
	                ExtentTestManager.getTest().log(Status.PASS,
	                        "Check The Checkbox '" + sTemp[0] + "' | Checked The checkbox '" + sTemp[0] + "' - SUCCESSFUL");
	            } else {
	                ReportManager.logToExtent("INFO", "Action logged");
	                ExtentTestManager.getTest().log(Status.WARNING,
	                        "Check The Checkbox '" + sTemp[0] + "' | Checkbox '" + sTemp[0] + "' has already been checked");
	            }
	        } catch (Exception e) {
	            checkException(Initialize.getWebDriver(), e, "Check The Checkbox '" + sTemp[0] + "'");
	        }
	    } else {
	        try {
	            if (we.isSelected()) {
	                we.click();
	                ReportManager.logToExtent("INFO", "Action logged");
	                ExtentTestManager.getTest().log(Status.PASS,
	                        "Uncheck The Checkbox '" + sTemp[0] + "' | Unchecked The Checkbox '" + sTemp[0] + "' - SUCCESSFUL");
	            } else {
	                ReportManager.logToExtent("INFO", "Action logged");
	                ExtentTestManager.getTest().log(Status.WARNING,
	                        "Uncheck The Checkbox '" + sTemp[0] + "' | Checkbox '" + sTemp[0] + "' has already been unchecked");
	            }
	        } catch (Exception e) {
	            checkException(Initialize.getWebDriver(), e, "Uncheck the checkbox '" + sTemp[0] + "'");
	        }
	    }

	    sTemp = null;
	    status = null;
	    Runtime.getRuntime().gc();
	}

	public void radioButton(String TestObject) throws IOException {
	    String[] sTemp = splitTestObject(Initialize.getWebDriver(), TestObject);
	    WebElement we = checkElement(Initialize.getWebDriver(), sTemp[1], sTemp[2]);

	    try {
	        if (!we.isSelected()) {
	            we.click();
	            ReportManager.logToExtent("INFO", "Action logged");

	            ExtentTestManager.getTest().log(Status.PASS, 
	                "Select the Radio Button '" + sTemp[0] + "' | Selected the Radio Button '" + sTemp[0] + "'");
	        } else {
	            ReportManager.logToExtent("INFO", "Action logged");

	            ExtentTestManager.getTest().log(Status.WARNING, 
	                "Select the Radio Button '" + sTemp[0] + "' | Radio Button '" + sTemp[0] + "' has already been selected");
	        }
	    } catch (Exception e) {
	        checkException(Initialize.getWebDriver(), e, "Select the Radio Button '" + sTemp[0] + "'");
	    }

	    sTemp = null;
	    Runtime.getRuntime().gc();
	}
		
	public void listBoxSelect(String TestObject, String Value, String Identifiers) throws IOException {

	    if (Value.startsWith("#")) {
	        String[] nValue = Value.split("#");
	        System.out.println("nValue[1] is - " + nValue[1]);
	        Index = Integer.parseInt(nValue[1]);
	        System.out.println("Index is - " + Index);
	    }

	    SeleniumIdentifiers Identifier = SeleniumIdentifiers.valueOf(Identifiers);
	    String[] sTemp = splitTestObject(Initialize.getWebDriver(), TestObject);
	    Select select = new Select(checkElement(Initialize.getWebDriver(), sTemp[1], sTemp[2]));

	    switch (Identifier) {
	        case byIndex: {
	            try {
	                select.selectByIndex(Integer.parseInt(Value));
	                String sTemp1 = select.getOptions().get(Index).getText();
	                ReportManager.logToExtent("INFO", "Action logged");

	                ExtentTestManager.getTest().log(Status.PASS,
	                    "Select The Value - '" + Value + "' From The Dropdown | Selected The Value - '" + Value + "' From The Dropdown - SUCCESSFUL");
	            } catch (Exception e) {
	                checkException(Initialize.getWebDriver(), e, sTemp[0] + " - Select '" + Value + "' from the dropdown");
	            }
	            break;
	        }
	        case byValue: {
	            try {
	                select.selectByValue(Value);
	                ReportManager.logToExtent("INFO", "Action logged");

	                ExtentTestManager.getTest().log(Status.PASS,
	                    "Select The Value - '" + Value + "' From The Dropdown | Selected The Value - '" + Value + "' From The Dropdown - SUCCESSFUL");
	            } catch (Exception e) {
	                checkException(Initialize.getWebDriver(), e, sTemp[0] + " - Select '" + Value + "' from the dropdown");
	            }
	            break;
	        }
	        case byVisibleText: {
	            try {
	                select.selectByVisibleText(Value);
	                ReportManager.logToExtent("INFO", "Action logged");

	                ExtentTestManager.getTest().log(Status.PASS,
	                    "Select The Value - '" + Value + "' From The Dropdown | Selected The Value - '" + Value + "' From The Dropdown - SUCCESSFUL");
	            } catch (Exception e) {
	                checkException(Initialize.getWebDriver(), e, sTemp[0] + " - Select '" + Value + "' from the dropdown");
	            }
	            break;
	        }
	        default:
	            break;
	    }

	    sTemp = null;
	    Value = null;
	    Runtime.getRuntime().gc();
	}
		
	public void popUpWindow(String Action) {
	    int x = Action.equalsIgnoreCase("accept") ? 1 : 0;

	    switch (x) {
	        case 1: {
	            try {
	                String sTemp = Initialize.getWebDriver().switchTo().alert().getText();
	                Initialize.getWebDriver().switchTo().alert().accept();

	                ReportManager.logToExtent("INFO", "Action logged");

	                ExtentTestManager.getTest().log(Status.PASS,
	                    "Accept PopUp | Accept PopUp with Message '" + sTemp + "' - SUCCESSFUL");
	            } catch (Exception e) {
	                checkException(Initialize.getWebDriver(), e, "Accept PopUp");
	            }
	            break;
	        }
	        case 0: {
	            try {
	                String sTemp = Initialize.getWebDriver().switchTo().alert().getText();
	                Initialize.getWebDriver().switchTo().alert().dismiss();

	                ReportManager.logToExtent("INFO", "Action logged");

	                ExtentTestManager.getTest().log(Status.PASS,
	                    "Dismiss PopUp | Dismiss PopUp with Message '" + sTemp + "' - SUCCESSFUL");
	            } catch (Exception e) {
	                checkException(Initialize.getWebDriver(), e, "Dismiss PopUp");
	            }
	            break;
	        }
	    }

	    Action = null;
	    x = 0;
	}
		
		public void startTransaction() {
			startTransactionTime = ReportManager.currentTimeInMillis();
			ReportManager.logToExtent("INFO", "Action logged");
			int startTransactionStep = stepCount;
			System.out.println(startTransactionStep);
		}
		

		public void endTransaction() {
		    endTransactionStep = stepCount;
		    endTransactionTime = ReportManager.currentTimeInMillis();
		    long transactionTime = endTransactionTime - startTransactionTime;

		    String message = "Transaction completed in " + ReportManager.textExecutionTime(transactionTime);

		    if (transactionTime > 8000) {
		        message += " (Slow)";
		    }

		    ReportManager.logToExtent("INFO", message);
		    ExtentTestManager.getTest().log(Status.INFO, message);

		    // Reset counters
		    startTransactionTime = 0;
		    endTransactionTime = 0;
		    transactionTime = 0;
		    startTransactionStep = 0;
		    endTransactionStep = 0;
		}
		

		public void mouseOver(WebDriver driver, String TestObject) throws IOException {
		    String[] sTemp = splitTestObject(driver, TestObject);

		    try {
		        DriverActions.moveToElement(checkElement(driver, sTemp[1], sTemp[2])).build().perform();

		        ReportManager.logToExtent("INFO", "Action logged");

		        ExtentTestManager.getTest().log(Status.PASS,
		            "Mouse Over on - '" + sTemp[0] + "' | Mouse Overed on - '" + sTemp[0] + "'");
		    } catch (Exception e) {
		        checkException(driver, e, "Mouse Over on - '" + sTemp[0] + "'");
		    }

		    sTemp = null;
		}

		
		public String getText(WebDriver driver, String testObject) throws IOException {
		    String[] sTemp = splitTestObject(driver, testObject);
		    String str = null;
		    WebElement we = checkElement(driver, sTemp[1], sTemp[2]);

		    try {
		        str = we.getText();
		        if (str != null) {
		            ReportManager.logToExtent("INFO", "Action logged");

		            ExtentTestManager.getTest().log(Status.PASS,
		                "Get Text From The Screen - '" + str + "' | Got The Text From The Screen - '" + str + "' - SUCCESSFUL");
		        } else {
		            ReportManager.logToExtent("INFO", "Action logged");

		            ExtentTestManager.getTest().log(Status.WARNING,
		                "Get Text From The Screen - '" + str + "' | Unable to get The Text From The Screen - '" + str + "' - UNSUCCESSFUL");
		        }
		    } catch (Exception e) {
		        checkException(driver, e, "Get text from - '" + sTemp[0] + "'");
		        ReportManager.logToExtent("INFO", "Action logged");

		        ExtentTestManager.getTest().log(Status.FAIL,
		            "Get Text From The Screen - '" + str + "' | Unable to get The Text From The Screen - '" + str + "' - UNSUCCESSFUL");
		    }
		    return str;
		}
		
		public String getPreviousYeardate() throws IOException {
		    Calendar cal = Calendar.getInstance();
		    Date today = cal.getTime();
		    cal.add(Calendar.YEAR, 1); // to get previous year add 1
		    cal.add(Calendar.DAY_OF_MONTH, -1); // to get previous day add -1
		    Date expiryDate = cal.getTime();

		    System.out.println("today is --> " + today);
		    System.out.println("expiryDate is --> " + expiryDate);

		    if (expiryDate != null) {
		        ReportManager.logToExtent("INFO", "Action logged");

		        ExtentTestManager.getTest().log(Status.PASS,
		            "Get Previous year Date - '" + expiryDate + "' | Got Previous year Date - '" + expiryDate + "' - SUCCESSFUL");
		    } else {
		        ReportManager.logToExtent("INFO", "Action logged");

		        ExtentTestManager.getTest().log(Status.WARNING,
		            "Get Previous year Date - '" + expiryDate + "' | Unable to Get Previous year Date - '" + expiryDate + "' - UNSUCCESSFUL");
		    }
		    return null;
		}
		public String getAttribute(WebDriver wd, String wdxPath, String wdAttribute) {

			// return wd.findElement(By.xpath(wdxPath)).getAttribute(wdAttribute);

			String sTemp = null;
			try {

				sTemp = wd.findElement(By.xpath(wdxPath)).getAttribute(wdAttribute)
						.trim();
				ReportManager.logToExtent("INFO", "Action logged");

			} catch (Exception e) {
				String vException = e.getMessage().toString();
				// System.out.println("vException - " + vException);
				if (vException.indexOf("(") != -1) {

					// vException = vException.substring(11,
					// vException.lastIndexOf("]")+1);
					vException = vException.substring(0,
							vException.indexOf("(") - 1);
					System.out.println("Final Message - " + vException);
					ReportManager.logToExtent("INFO", "Action logged");
					ReportManager.logToExtent("INFO", "Action logged");
					Assert.fail("Exiting Script Execution");

				}
			}
			return sTemp;

		}
		
		public String compareText1(WebDriver wd, String wdxPath, String wdText) {

			String wdTextOut = wd.findElement(By.xpath(wdxPath)).getText();
			//String wdTextOut="Provisioning";
			System.out.println(wdTextOut+"****************************************");
			if (wdTextOut.equals(wdText)) {
				return "Pass";
			} else {
				return "Fail";
			}
		}
		
		public String compareAttribute(WebDriver wd, String wdxPath,
				String wdAttribute, String wdText) {

			String wdTextOut = wd.findElement(By.xpath(wdxPath)).getAttribute(
					wdAttribute);
			if (wdTextOut.equals(wdText)) {
				return "Pass";
			} else {
				return "Fail";
			}

		}

		public void SwitchWindowAndClose(WebDriver wd, String windowTitle)

		{
			String parentWindowHandle = wd.getWindowHandle();
			WebDriver popup = null;
			@SuppressWarnings({ "rawtypes", "unused" })
			Set set = wd.getWindowHandles();
			@SuppressWarnings("rawtypes")
			Set windowIterator = wd.getWindowHandles();
			@SuppressWarnings("rawtypes")
			Iterator it = windowIterator.iterator();

			while (it.hasNext()) {

				String windowHandle = (String) it.next();
				popup = wd.switchTo().window(windowHandle);

				if (popup.getTitle().equals(windowTitle)) {
					wd.switchTo().window(windowHandle);
					wd.close();
					wd.switchTo().window(parentWindowHandle);
					break;

				}

			}
		}
		
		public void PopupHandle(WebDriver wd)

		{
			String parentWindowHandle = wd.getWindowHandle();

			Alert alert = wd.switchTo().alert();
			// System.out.println(alert.getText());
			alert.accept();
			wd.switchTo().window(parentWindowHandle);

		}
		

		public void WaitForProgressBar(WebDriver wd, String sObject, int iTimeout) throws Exception {
		    for (int second = 0;; second++) {
		        if (second >= iTimeout)
		            Assert.fail("Timeout - Progress Bar Still Exists After " + iTimeout + " seconds");
		        try {
		            if (!wd.findElement(By.id(sObject)).isDisplayed())
		                break;
		        } catch (Exception e) {
		            checkException(Initialize.getWebDriver(), e, "");
		        }
		        Thread.sleep(1000);
		    }
		}


		public void WaitForObject(WebDriver wd, String oBjString, String xPath, int vTimeout) throws Exception {
		    for (int second = 0; second <= vTimeout; second++) {
		        String sTemp = null;

		        if (wd.findElement(By.xpath(xPath)).getText() != null) {
		            System.out.println("Inside getText()");
		            sTemp = wd.findElement(By.xpath(xPath)).getText().trim();
		            System.out.println("Text Is - " + sTemp);
		            oBjString.equals(sTemp);
		            break;
		        } else if (wd.findElement(By.xpath(xPath)).getAttribute("id") != null) {
		            System.out.println("Inside getAttribute(\"id\")");
		            sTemp = wd.findElement(By.xpath(xPath)).getAttribute("id").trim();
		            System.out.println("ID Is - " + sTemp);
		            oBjString.equals(sTemp);
		            break;
		        } else if (wd.findElement(By.xpath(xPath)).getAttribute("name") != null) {
		            System.out.println("Inside getAttribute(\"name\")");
		            sTemp = wd.findElement(By.xpath(xPath)).getAttribute("name").trim();
		            System.out.println("Name Is - " + sTemp);
		            oBjString.equals(sTemp);
		            break;
		        } else if (wd.findElement(By.xpath(xPath)).getAttribute("value") != null) {
		            System.out.println("Inside getAttribute(\"value\")");
		            sTemp = wd.findElement(By.xpath(xPath)).getAttribute("value").trim();
		            System.out.println("Value Is - " + sTemp);
		            oBjString.equals(sTemp);
		            break;
		        } else {
		            Assert.fail("Object did not appear after - " + vTimeout + " Seconds");
		        }
		        Thread.sleep(1000);
		    }
		}
		
		

		public void WaitForObject_Attribute(WebDriver wd, String oBjValue, String xPath, String vAttValue) throws Exception {
		    for (int second = 0;; second++) {
		        if (second >= 180)
		            Assert.fail("Timeout - Object did not appear after 3 Minutes");

		        try {
		            String sTemp = null;
		            if (oBjValue.equals(wd.findElement(By.xpath(xPath)).getAttribute(vAttValue))) {
		                sTemp = wd.findElement(By.xpath(xPath)).getAttribute(vAttValue);
		                System.out.println(vAttValue + " - value is - " + sTemp);
		                break;
		            }
		        } catch (Exception e) {
		            checkException(Initialize.getWebDriver(), e, "");
		        }
		        Thread.sleep(1000);
		    }
		}
		

public void verify(String TestObject, String Expected) throws IOException {
		    String Actual = null;
		    String[] sTemp = splitTestObject(Initialize.getWebDriver(), TestObject);
		    WebElement we = checkElement(Initialize.getWebDriver(), sTemp[1], sTemp[2]);
		    try {
		        Actual = we.getText();
		        if (Actual.equalsIgnoreCase(Expected)) {
		            ReportManager.logToExtent("INFO", "Action logged");

		            ExtentTestManager.getTest().log(Status.PASS,
		                "Expected Value Is - " + Expected + " | Actual Value Is - " + Actual + " - SUCCESSFUL");
		        } else {
		            ReportManager.logToExtent("INFO", "Action logged");

		            ExtentTestManager.getTest().log(Status.FAIL,
		                "Expected Value Is - " + Expected + " | Actual Value Is - " + Actual + " - UNSUCCESSFUL");
		        }
		    } catch (Exception e) {
		        checkException(Initialize.getWebDriver(), e, sTemp[1] + " Compare string");
		    }
		}

		public void compareString(String Actual, String Expected) throws IOException {
		    if (Actual.equalsIgnoreCase(Expected)) {
		        ReportManager.logToExtent("INFO", "Action logged");

		        ExtentTestManager.getTest().log(Status.PASS,
		            "Expected Value Is - " + Expected + " | Actual Value Is - " + Actual + " - SUCCESSFUL");
		    } else {
		        ReportManager.logToExtent("INFO", "Action logged");

		        ExtentTestManager.getTest().log(Status.FAIL,
		            "Expected Value Is - " + Expected + " | Actual Value Is - " + Actual + " - UNSUCCESSFUL");
		    }
		}

	
	public void  reportError(String actual,String expected) throws IOException
	{
		ReportManager.logToExtent("INFO", "Action logged");
	
	}
	

	
	public void editAFieldUsingActions(WebDriver driver, String TestObject, String value) throws IOException {
	    String[] sTemp = splitTestObject(driver, TestObject);
	    WebElement we = checkElement(driver, sTemp[1], sTemp[2]);
	    System.out.println("--Value : " + value);
	    Actions act = new Actions(driver);

	    try {
	        we.clear();
	        act.sendKeys(we, value).perform();

	        ReportManager.logToExtent("INFO", "Action logged");

	        ExtentTestManager.getTest().log(Status.PASS,
	            "Enter Value As - " + value + " | Entered Value Is - " + value + " - SUCCESSFUL");
	    } catch (Exception e) {
	        checkException(driver, e, sTemp[1] + " Entered Value As '" + value + "'");
	    }

	    sTemp = null;
	    value = null;
	}


	public boolean checkElementDisplayed(WebDriver driver, String testObject) throws IOException {
	    boolean flag = false;
	    String[] sTemp = splitTestObject(driver, testObject);
	    try {
	        WebElement we = checkElement(driver, sTemp[1], sTemp[2]);

	        if (we.isDisplayed()) {
	            flag = true;
	            ReportManager.logToExtent("INFO", "Action logged");

	            ExtentTestManager.getTest().log(Status.PASS,
	                "Verify - '" + sTemp[0] + "' Object Present On the Screen | Verified - '" + sTemp[0] + "' Object Present On the Screen - SUCCESSFUL");
	        } else {
	            flag = false;
	            ReportManager.logToExtent("INFO", "Action logged");

	            ExtentTestManager.getTest().log(Status.FAIL,
	                "Verify - '" + sTemp[0] + "' Object Present On the Screen | Verified - '" + sTemp[0] + "' Object is NOT Present On the Screen - UNSUCCESSFUL");
	        }
	    } catch (Exception e) {
	        checkException(driver, e, sTemp[0] + " Check Element Displayed / Enabled");
	        ReportManager.logToExtent("INFO", "Action logged");

	        ExtentTestManager.getTest().log(Status.FAIL,
	            "Verify - '" + sTemp[0] + "' Object Present On the Screen | Verified - '" + sTemp[0] + "' Object is NOT Present On the Screen - UNSUCCESSFUL");
	    }

	    return flag;
	}

public boolean IsDisplayed(WebDriver driver,String testObject) {

	boolean flag = false;
	
	try {

		String[] sTemp = splitTestObject(driver, testObject);
		Boolean bTemp ;
		if (sTemp[1].equals("xpath"))
			 bTemp =Initialize.getWebDriver().findElement(By.xpath(sTemp[2])).isDisplayed();
		else
			 bTemp =Initialize.getWebDriver().findElement(By.id(sTemp[2])).isDisplayed();
		if(bTemp){
			flag = true;	
		}
	} catch (Exception e) {
		flag = false;
	}
	return flag;
}


public boolean IsElementPresent(WebDriver driver, String Locators) throws IOException {
	boolean flag;
	try {
		flag = Initialize.getWebDriver().findElement(By.id(Locators)).isDisplayed();
	} catch (Exception e) {
		flag=false;
	}
	return flag;
}


public enum KeyAction {
    ENTER(Keys.ENTER),
    ESC(Keys.ESCAPE),
    ARROWDOWN(Keys.ARROW_DOWN),
    ARROWUP(Keys.ARROW_UP),
    PAGEUP(Keys.PAGE_UP),
    PAGEDOWN(Keys.PAGE_DOWN);

    private final Keys key;

    KeyAction(Keys key) {
        this.key = key;
    }

    public Keys getKey() {
        return key;
    }
}

public void sendKey(WebDriver driver, String testObject, String key) throws IOException {
    String[] sTemp = splitTestObject(driver, testObject);
    WebElement element = checkElement(driver, sTemp[1], sTemp[2]);

    try {
        KeyAction action = null;
        try {
            action = KeyAction.valueOf(key.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unsupported key: " + key);
        }

        element.sendKeys(action.getKey());

        logSuccess(key);
    } catch (Exception e) {
        checkException(driver, e, "Unable to perform the keyboard operation - " + key);
    }
}


private void logSuccess(String key) {
    String message = "Performed the keyboard operation - " + key;
    ReportManager.logToExtent("INFO", "Action logged");
    ExtentTestManager.getTest().log(Status.PASS, message);
}


	public void refreshPage() throws InterruptedException
	{
		Initialize.getWebDriver().navigate().refresh();
		
	}

	public void byPassSSLCertificateErrorInIE() throws InterruptedException
	{
		Initialize.getWebDriver().navigate ().to ("javascript:document.getElementById('overridelink').click()");	
	}

	public void waitTime(long wdWait)
	{
		try {
			Thread.sleep(wdWait);
		} catch (InterruptedException e) 
		{
			checkException(Initialize.getWebDriver(), e,"");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void compareText(WebDriver driver, String testObject, String expectedText) throws IOException {
	    String[] sTemp = splitTestObject(driver, testObject);

	    try {
	        WebElement element = checkElement(driver, sTemp[1], sTemp[2]);
	        String actualText = element.getText() != null ? element.getText().trim() : "";

	        System.out.println(actualText + "****************************************");

	        if (actualText.equalsIgnoreCase(expectedText.trim())) {
	            logResult(TestStatus.PASS, expectedText, actualText, true);
	        } else {
	            logResult(TestStatus.FAIL, expectedText, actualText, false);
	        }
	    } catch (NoSuchElementException e) {
	        handleException(driver, e, sTemp[0]);
	    } catch (Exception e) {
	        handleException(driver, e, sTemp[0]);
	    }
	}

	

	private void logResult(TestStatus status, String expected, String actual, boolean isSuccess) {
	    String message = String.format("Compare expected text: '%s' with actual text: '%s'", expected, actual);
	    String details = isSuccess ? "Verified expected text successfully" : "Expected text does not match actual text";

	    ReportManager.logToExtent("INFO", "Action logged");

	    Status logStatus = isSuccess ? Status.PASS : Status.FAIL;
	    ExtentTestManager.getTest().log(logStatus, message + " | " + details);
	}

	private void handleException(WebDriver driver, Exception e, String objectName) {
	    checkException(driver, e, "Selected - '" + objectName + "'");
	    ReportManager.logToExtent("INFO", "Action logged");
	    ExtentTestManager.getTest().fail("Error occurred in text comparison | Text comparison - Unsuccessful");
	}
	

static void checkException(WebDriver driver, Exception e, String action) {
    takeScreenshotOnFailure(driver, "FAIL");
    ExtentTestManager.getTest().fail("Action failed: " + action + "\n" + e.getMessage());
    throw new RuntimeException("Failed during: " + action, e);
}

	public static void takeScreenshotOnFailure(WebDriver driver, String status) {
	    try {
	        // Create folder structure
	        String folderPath = System.getProperty("user.dir") + File.separator + "ErrorScreen" + File.separator + getFolderName();
	        File folder = new File(folderPath);
	        if (!folder.exists()) {
	            folder.mkdirs();
	        }

	        // Define screenshot file path
	        String screenshotPath = folderPath + File.separator + "Error_" + screenShotTime() + "_" + status + ".png";

	        // Capture screenshot as Base64 (Selenium 4 feature)
	        String base64Screenshot = ((TakesScreenshot) Initialize.getWebDriver()).getScreenshotAs(OutputType.BASE64);

	        // Attach to Extent report (v5)
	        ExtentTestManager.getTest().log(Status.INFO, "Error Screen");
	        ExtentTestManager.getTest().addScreenCaptureFromBase64String(base64Screenshot, "Screenshot on " + status);

	        // Optional: Save screenshot file locally
	        File screenshotFile = ((TakesScreenshot) Initialize.getWebDriver()).getScreenshotAs(OutputType.FILE);
	        FileUtils.copyFile(screenshotFile, new File(screenshotPath));

	    } catch (IOException | WebDriverException e) {
	        e.printStackTrace();
	        ExtentTestManager.getTest().log(Status.FAIL, "Failed to capture screenshot: " + e.getMessage());
	    }
	}

	protected static String screenShotTime() {
	    return new SimpleDateFormat("MM_dd_yyyy_hh_mm_ss").format(Calendar.getInstance().getTime());
	}

	protected static String getFolderName() {
	    return new SimpleDateFormat("MM_dd_yyyy").format(Calendar.getInstance().getTime());
	}

	public static void wTUntObjDisplayed(WebDriver driver, String testObject) throws IOException {
	    String[] sTemp = splitTestObject(driver, testObject);
	    WebElement we = checkElement(driver, sTemp[1], sTemp[2]);

	    try {
	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(200)); // 20 attempts * 10 sec = 200 sec
	        wait.until(ExpectedConditions.or(
	            ExpectedConditions.visibilityOf(we),
	            ExpectedConditions.elementToBeClickable(we)
	        ));

	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.PASS, "Object - " + sTemp[0] + " is displayed successfully");

	    } catch (Exception e) {
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.WARNING, "Object - " + sTemp[0] + " is NOT displayed - UNSUCCESSFUL");
	    }
	}
	public String randomNames() {
		  int leftLimit = 97; // letter 'a'
		    int rightLimit = 122; // letter 'z'
		    int targetStringLength = 10;
		    Random random = new Random();
		 
		    String generatedString = random.ints(leftLimit, rightLimit + 1)
		      .limit(targetStringLength)
		      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
		      .toString();
		 
		    System.out.println(generatedString);
			return generatedString;
	}
	public void ScreenPrint(WebDriver driver, HashMap<String, String> testData, String ScriptName,String ScreenName) throws IOException {
        File folder1 = new File(System.getProperty("user.dir")+ "\\ScriptName\\"+ScriptName);
        if (!folder1.exists()) {
            folder1.mkdir();
        }
        String reportFolder = testData.get("strFolderName");
        String screen = System.getProperty("user.dir") + "\\Testresults\\"+"\\"+reportFolder+"\\"+"\\Screenshots\\" + "\\"+ScriptName+"\\"+ScreenName+".PNG";
        File TakeScreenPrint = ((TakesScreenshot) Initialize.getWebDriver()).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(TakeScreenPrint, new File(screen));
        Runtime.getRuntime().gc();
    }


	public void getScreenshot(WebDriver driver, String screenshotName) throws IOException {
	    String dateName = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(new Date());
	    File screenshot = ((TakesScreenshot) Initialize.getWebDriver()).getScreenshotAs(OutputType.FILE);
	    String screenshotPath = System.getProperty("user.dir") + "\\Evidence\\Fail_TestCases_Screenshots\\" + screenshotName + "_" + dateName + ".png";
	    FileUtils.copyFile(screenshot, new File(screenshotPath));

	    // Capture Base64 directly from Selenium
	    String base64Screenshot = ((TakesScreenshot) Initialize.getWebDriver()).getScreenshotAs(OutputType.BASE64);

	    // Log in ExtentReports v5
	    ExtentTestManager.getTest().log(Status.INFO, screenshotName + " Screenshot attached")
	            .addScreenCaptureFromBase64String(base64Screenshot, screenshotName);
	}

public void elementDisplayed(WebDriver driver, String Locator) throws IOException {
    String[] sTemp = splitTestObject(driver, Locator);
    WebElement ele = checkElement(driver, sTemp[1], sTemp[2]);

    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", ele);

    if (ele.isDisplayed()) {
        ReportManager.logToExtent("INFO", "Action logged" + " - Successful");
        ExtentTestManager.getTest().log(Status.PASS, "Element displayed - " + sTemp[0] + " | Text: " + ele.getText());
    } else {
        ReportManager.logToExtent("INFO", "Action logged");
        ExtentTestManager.getTest().log(Status.FAIL, "Element NOT displayed - " + sTemp[0] + " | Text: " + ele.getText());
    }
}
	 

	 public void elementNotDisplayed(WebDriver driver, String Locator) throws IOException {
	     String[] sTemp = splitTestObject(driver, Locator);
	     WebElement ele = checkElement(driver, sTemp[1], sTemp[2]);

	     ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", ele);

	     if (ele.isDisplayed()) {
	         ReportManager.logToExtent("INFO", "Action logged");
	         ExtentTestManager.getTest().log(Status.FAIL, "Element should NOT be displayed - " + sTemp[0] + " | Actual: " + ele.getText());
	     } else {
	         ReportManager.logToExtent("INFO", "Action logged");
	         ExtentTestManager.getTest().log(Status.PASS, "Element is NOT displayed as expected - " + sTemp[0]);
	     }
	 }
		 

		 public void waitTillElementVisible(WebDriver driver, String locator, int seconds) throws Exception {
		     WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
		     String[] sTemp = splitTestObject(driver, locator);
		     WebElement element = checkElement(driver, sTemp[1], sTemp[2]);

		     wait.until(ExpectedConditions.visibilityOf(element));

		     ReportManager.logToExtent("INFO", "Action logged");
		     ExtentTestManager.getTest().log(Status.PASS, "Element displayed - " + sTemp[0]);
		 }
	 

	public void waitTillElementInvisible(WebDriver driver, String locator, int seconds) throws IOException {
		 WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
		String[] sTemp = splitTestObject(driver, locator);
		wait.until(ExpectedConditions.invisibilityOf(checkElement(driver, sTemp[1], sTemp[2])));
	}

	public void waitTillElementInvisible(WebDriver driver, By locator, int seconds) throws IOException {
		 WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
		wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
	}


public void waitTillElementToBeClickable(WebDriver driver, String locator, int seconds) throws Exception {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
    String[] sTemp = splitTestObject(driver, locator);
    wait.until(ExpectedConditions.elementToBeClickable(checkElement(driver, sTemp[1], sTemp[2])));
}


public void selectDropDownByVisibleText(WebDriver driver, String testObject, String value) throws IOException {
    String[] sTemp = splitTestObject(driver, testObject);
    WebElement we = checkElement(driver, sTemp[1], sTemp[2]);

    try {
        String tagName = we.getTagName();

        if (tagName.equalsIgnoreCase("select")) {
            // ✅ Standard dropdown
            Select objSelect = new Select(we);
            objSelect.selectByVisibleText(value);
            System.out.println("Selected from standard dropdown: " + value);
        } else {
            // ✅ Custom dropdown (e.g., Select2)
            we.click(); // Open dropdown
            System.out.println("Clicked custom dropdown: " + sTemp[0]);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            String optionXpath = "//li[contains(@class,'select2-results__option') and normalize-space(text())='" + value + "']";

            WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(optionXpath)));
            option.click();
            System.out.println("Selected option: " + value);
        }

        // ✅ Reporting success
        ReportManager.logToExtent("INFO", "Action logged");
        ExtentTestManager.getTest().log(Status.PASS, "Selected '" + sTemp[0] + "' value as " + value);

    } catch (Exception e) {
        // ✅ Reporting failure
        checkException(driver, e, "Failed to select '" + sTemp[0] + "' value as " + value);
        ExtentTestManager.getTest().log(Status.FAIL, "Dropdown selection failed for '" + sTemp[0] + "' due to: " + e.getMessage());
    }

    sTemp = null;
    testObject = null;
}
	
public void compareString(List<WebElement> list, String expTxt) throws IOException {
	    StringBuilder actTxtBuilder = new StringBuilder();
	    for (WebElement element : list) {
	        actTxtBuilder.append(element.getText()).append("\n");
	    }
	    String actTxt = actTxtBuilder.toString().trim();

	    if (actTxt.equalsIgnoreCase(expTxt.trim())) {
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.PASS, "Expected: " + expTxt + " | Actual: " + actTxt + " - Successful");
	    } else {
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.FAIL, "Expected: " + expTxt + " | Actual: " + actTxt + " - Unsuccessful");
	    }
	}	

public void waitUntilElementNotVisible(WebDriver driver, By ele, int sec) throws Exception
    {
		for (int second = 0; ; second++)
        {
			try { 
				WebElement element = Initialize.getWebDriver().findElement(ele);
				if (second >= sec) 
					break;
	            	Boolean b = element.isDisplayed();
	            	if (!b) 
	            		break;
			} catch (Exception e) { 
            	break;
            }
            Thread.sleep(1000);
        }
    }
	

	public void containsString(String Actual, String Expected) throws IOException {
	    if (Actual.contains(Expected)) {
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.PASS, "Expected Value: " + Expected + " | Actual Value: " + Actual + " - Successful");
	    } else {
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.FAIL, "Expected Value: " + Expected + " | Actual Value: " + Actual + " - Unsuccessful");
	    }
	}
public void waitUntilElementVisible(WebDriver driver, By ele, int sec) throws Exception
   {
		for (int second = 0; ; second++)
       {
			try { 
				WebElement element = Initialize.getWebDriver().findElement(ele);
				if (second >= sec) 
					Assert.fail("Error: Element still not visible");
	            	Boolean b = element.isDisplayed();
	            	if (b) 
	            		break;
			} catch (Exception e) { 
           	continue;
           }
           Thread.sleep(1000);
       }
   }
	
public void writeToExcel(String quoteNo) throws Exception {
	    try (FileInputStream fis = new FileInputStream(new File(System.getProperty("user.dir") + "/TestData/QuoteNumbers.xls"));
	         Workbook workbook = WorkbookFactory.create(fis)) {

	        int tcNum = Integer.parseInt("2");
	        Sheet sheet = workbook.getSheetAt(0);
	        Row row = sheet.getRow(tcNum);
	        if (row == null) {
	            row = sheet.createRow(tcNum);
	        }
	        row.createCell(1).setCellValue(quoteNo);

	        try (FileOutputStream outFile = new FileOutputStream(new File(System.getProperty("user.dir") + "/TestData/QuoteNumbers.xls"))) {
	            workbook.write(outFile);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

public void selectDropDownByValue(WebDriver driver, String testObject, String value) throws IOException {
	    String[] sTemp = splitTestObject(driver, testObject);
	    WebElement we = checkElement(driver, sTemp[1], sTemp[2]);

	    try {
	        Select objSelect = new Select(we);
	        objSelect.selectByValue(value);

	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.PASS, "Selected '" + sTemp[0] + "' value as " + value);

	    } catch (Exception e) {
	        checkException(driver, e, "Selected - '" + sTemp[0] + "'");
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.FAIL, "Failed to select '" + sTemp[0] + "' value as " + value + " due to: " + e.getMessage());
	    }

	    sTemp = null;
	    testObject = null;
	}


	public void clickByJS(WebDriver driver, String testObject) throws IOException {
	    String[] sTemp = splitTestObject(driver, testObject);
	    WebElement we = checkElement(driver, sTemp[1], sTemp[2]);

	    try {
	        waitTillElementVisible(driver, testObject, 15000);
	        JavascriptExecutor executor = (JavascriptExecutor) driver;
	        executor.executeScript("arguments[0].click();", we);

	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.PASS, "Click On - '" + sTemp[0] + "' - Successful");

	    } catch (Exception e) {
	        System.out.println(e.toString());
	        checkException(driver, e, "Click On - '" + sTemp[0] + "'");
	        ExtentTestManager.getTest().log(Status.FAIL, "Failed to click on - '" + sTemp[0] + "' due to: " + e.getMessage());
	    }

	    sTemp = null;
	    testObject = null;
	}

	
	public void clearAField(WebDriver driver, String TestObject) throws IOException {
		String[] sTemp = splitTestObject(driver, TestObject);
		WebElement we = checkElement(driver, sTemp[1], sTemp[2]);
		try {
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", we);
			 we.click();
			 we.clear();
			 we.sendKeys(Keys.chord(Keys.CONTROL,"a", Keys.DELETE));
			 if(!we.getText().isEmpty())
				 we.clear();
		} catch (Exception e) {
			checkException(driver, e, "Clear textBox value");
		}
		sTemp = null;
	}

	public void checkElementNotDisplayed1(WebDriver driver, By path) {
	    List<WebElement> elements = Initialize.getWebDriver().findElements(path);
	    if (elements.isEmpty()) {
	        ReportManager.logToExtent("PASS", "Element is not present on the page");
	        ExtentTestManager.getTest().log(Status.PASS, "Element is not present on the page");
	    } else {
	        WebElement ele = elements.get(0);
	        if (ele.isDisplayed()) {
	            ReportManager.logToExtent("FAIL", "Element should not be displayed but is visible");
	            ExtentTestManager.getTest().log(Status.FAIL, "Element is displayed - " + ele.getText());
	        } else {
	            ReportManager.logToExtent("PASS", "Element is present but hidden");
	            ExtentTestManager.getTest().log(Status.PASS, "Element is present but hidden");
	        }
	    }
	}


	public void checkElementNotDisplayed(WebDriver driver, By path) {
	    List<WebElement> elements = Initialize.getWebDriver().findElements(path);
	    if (elements.isEmpty()) {
	        ReportManager.logToExtent("PASS", "Element is not present on the page");
	        ExtentTestManager.getTest().log(Status.PASS, "Element is not present on the page");
	    } else {
	        WebElement ele = elements.get(0);
	        if (ele.isDisplayed()) {
	            ReportManager.logToExtent("FAIL", "Element should not be displayed but is visible");
	            ExtentTestManager.getTest().log(Status.FAIL, "Element is displayed - " + ele.getText());
	        } else {
	            ReportManager.logToExtent("PASS", "Element is present but hidden");
	            ExtentTestManager.getTest().log(Status.PASS, "Element is present but hidden");
	        }
	    }
	}

	public boolean checkElementPresent(WebDriver driver, String Locators) throws IOException {
		boolean flag;
		try {
			flag = Initialize.getWebDriver().findElement(By.xpath(Locators)).isDisplayed();
				
		} catch (Exception e) {
			flag=false;
		}
		return flag;
	}


	
	public void clickByJS(WebDriver driver, WebElement ele) throws IOException {
		try {
			JavascriptExecutor executor = (JavascriptExecutor)driver;
			executor.executeScript("arguments[0].click();", ele);
			ReportManager.logToExtent("INFO", "Action logged");
			ExtentTestManager.getTest().pass("Clicked by JS | Clicked on element - Successfull");;
		} catch (Exception e) {
			checkException(driver, e, "Clicked by JS ");
		}
	}

	public By getElement (WebDriver driver,String testObject) {
		String[] sTemp=null;
		By by = null;
		try {
			sTemp = splitTestObject(driver,testObject);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(sTemp[1].equals("id")) {
			 by = By.id(sTemp[2]);
		}else if (sTemp[1].equals("xpath")) {
			 by = By.xpath(sTemp[2]);}
		return by;
	}
	

public void uploadFile(WebDriver driver, String TestObject, String FilePath) throws IOException {
	    String[] sTemp = splitTestObject(driver, TestObject);
	    WebElement we = checkElement(driver, sTemp[1], sTemp[2]);
	    System.out.println("--Value : " + FilePath);
	    try {
	        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", we);
	        we.isEnabled();
	        we.sendKeys(FilePath);

	        // Extent Report
	        ExtentTestManager.getTest().log(Status.PASS, "Upload file - " + FilePath + " | Upload successful");

	        // Custom Report
	        ReportManager.logToExtent("INFO", "Action logged");

	    } catch (Exception e) {
	        checkException(driver, e, "Uploaded file '" + FilePath + "'");
	    }
	    sTemp = null;
	    FilePath = null;
	}


	public void checkElementIsDisabled(WebDriver driver, String locator) throws IOException {
	    String[] sTemp = splitTestObject(driver, locator);
	    WebElement ele = checkElement(driver, sTemp[1], sTemp[2]);

	    String message = "Text displayed - " + sTemp[0];
	    String details = "Text displayed - '" + ele.getText() + "'";

	    if (!ele.isEnabled()) {
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.PASS, message + " | " + details + " - Successful");
	    } else {
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.FAIL, message + " | " + details + " - Element is enabled");
	    }
	}

public void dragAndDrop(WebDriver driver, String sourceObject, String destObject) throws IOException {
	    Actions act = new Actions(driver);

	    String[] sTemp = splitTestObject(driver, sourceObject);
	    WebElement sourceLocator = checkElement(driver, sTemp[1], sTemp[2]);
	    WebElement sourceLocator1 = Initialize.getWebDriver().findElement(By.xpath("//*[@id='leftlist']//li[@id='RSABET81']"));
	    sourceLocator1.click();

	    String[] sTemp1 = splitTestObject(driver, destObject);
	    WebElement destLocator = checkElement(driver, sTemp1[1], sTemp1[2]);
	    WebElement destLocator1 = Initialize.getWebDriver().findElement(By.id("rightlist"));

	    try {
	        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", sourceLocator1);

	        // Multiple drag-and-drop strategies
	        act.dragAndDrop(sourceLocator1, destLocator1).perform();
	        act.dragAndDropBy(sourceLocator1, 460, 206).perform();
	        act.clickAndHold(sourceLocator1).moveToElement(destLocator1).release().perform();

	        // Reporting
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.PASS, 
	            "Dragged - '" + sTemp[0] + "' | Dropped to " + sTemp1[0] + " - Successful");

	    } catch (Exception e) {
	        checkException(driver, e, "Selected - '" + sTemp[0] + "'");
	        ReportManager.logToExtent("INFO", "Action logged");
	        ExtentTestManager.getTest().log(Status.FAIL, 
	            "Dragged - '" + sTemp[0] + "' | Dropped to " + sTemp1[0] + " - Unsuccessful");
	    }
	}
	
	 public List<WebElement> getWebElements(WebDriver driver, String object) throws IOException {
		 
		 String[] sTemp = splitTestObject(driver, object);
		 try {
		 	List<WebElement> we = checkElements(driver, sTemp[1], sTemp[2]);
		 	
			return we;
		 } catch (Exception e) {
			checkException(driver, e, "WebElements not fount'"+ sTemp[0]+ "'");
		}
		 return null;		
	 }

	 public WebElement getWebElement(WebDriver driver, String object) throws IOException {
		 String[] sTemp = splitTestObject(driver, object);
		 try {
		 	WebElement we = checkElement(driver, sTemp[1], sTemp[2]);
			return we;
		 } catch (Exception e) {
			checkException(driver, e, "WebElement not fount'"+ sTemp[0]+ "'");
		}
		 return null;
	 }
	 

	 public void mouseHoverOnObjectAndClick(WebDriver driver, String object2) throws IOException {
	     String[] sTemp2 = splitTestObject(driver, object2);
	     Actions actions = new Actions(driver);

	     try {
	         WebElement element = getWebElement(driver, object2);
	         actions.moveToElement(element).click().build().perform();

	         ReportManager.logToExtent("INFO", "Action logged");

	         ExtentTestManager.getTest().log(Status.PASS,
	                 "Clicked on the element - '" + sTemp2[0] + "' | Successful");

	     } catch (Exception e) {
	         checkException(driver, e, "Clicked on - '" + sTemp2[0] + "'");

	         ReportManager.logToExtent("INFO", "Action logged");

	         ExtentTestManager.getTest().log(Status.FAIL,
	                 "Clicked on the element - '" + sTemp2[0] + "' | Unsuccessful");
	     }
	 }


public String fullScreenPrint(WebDriver driver, Map<String, String> testData, String scriptName, String screenName) throws Exception {
    String reportFolder = getConfigValue("FolderName") + "_" + new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(new Date());
    String folderPath = System.getProperty("user.dir") + File.separator + "Testresults" + File.separator + reportFolder + File.separator + "Screenshots";

    File folder1 = new File(folderPath);
    if (!folder1.exists()) {
        folder1.mkdirs();
    }

    File folder2 = new File(folderPath + File.separator + scriptName);
    if (!folder2.exists()) {
        folder2.mkdirs();
    }

    // Hide sticky headers and footers
    ((JavascriptExecutor) driver).executeScript(
        "document.querySelectorAll('header, footer, .sticky, .fixed, [style*=\"position:sticky\"], [style*=\"position:fixed\"]').forEach(el => el.style.display='none');"
    );

    // Capture full-page screenshot using AShot
    Screenshot fpScreenshot = new AShot()
            .shootingStrategy(ShootingStrategies.viewportPasting(2000))
            .takeScreenshot(driver);

    String screenshotPath = folder2 + File.separator + screenName + ".PNG";
    ImageIO.write(fpScreenshot.getImage(), "PNG", new File(screenshotPath));

    return screenshotPath; // ✅ Return the screenshot path
}
	public String getAppUrl(String urlType) throws Exception {
		String env = System.getProperty("Environment");	
		if(env==null)
		  env = getTestDataPF("Environment");
	    if(env.equalsIgnoreCase("SIT"))
	    	return getTestDataPF(urlType + env);
	    if(env.equalsIgnoreCase("UAT"))
	    	return getTestDataPF(urlType + env);
	    if(env.equalsIgnoreCase("Preprod"))
	    	return getTestDataPF(urlType + env);
		return "";   
	}


	public void rsaLoginWindow(WebDriver driver, HashMap<String, String> testData) throws Exception {
        //String strRSAEmail = testData.get("UserEmail");
        String strRSAEmail = getTestDataPF("UserEMail");
        System.out.println("Usr_email "+strRSAEmail);
        Thread.sleep(2000);
        if(checkElementDisplayed(driver, getTestObjectPF("IBSUITE_rsaEmail"))) {
               Thread.sleep(2000);
               editAField(driver, getTestObjectPF("IBSUITE_rsaEmail"), strRSAEmail);
               click(driver, getTestObjectPF("IBSUITE_btnRsaNext")); 
               Thread.sleep(1000);
        }
        else {
               System.out.println("RSA Login is not displayed");
        }
	}
	
	public void SwitchWindow(WebDriver wd, String windowTitle)

	{
//		String parentWindowHandle = wd.getWindowHandle();
		WebDriver popup = null;
		@SuppressWarnings({ "rawtypes", "unused" })
		Set set = wd.getWindowHandles();
		@SuppressWarnings("rawtypes")
		Set windowIterator = wd.getWindowHandles();
		@SuppressWarnings("rawtypes")
		Iterator it = windowIterator.iterator();

		while (it.hasNext()) {

			String windowHandle = (String) it.next();
			popup = wd.switchTo().window(windowHandle);

			if (popup.getTitle().equals(windowTitle)) {
				wd.switchTo().window(windowHandle);


			}

		}
	}

	
	public void SwitchtoChildWindow(WebDriver wd)

	{
	String parentWindow = Initialize.getWebDriver().getWindowHandle();
	Set<String> handles =  Initialize.getWebDriver().getWindowHandles();
	   for(String windowHandle  : handles)
	       {
	       if(!windowHandle.equals(parentWindow))
	          {
	          Initialize.getWebDriver().switchTo().window(windowHandle);
	          }
	       }
	}

	
	public void fullPagecreenshot(WebDriver driver, String fileWithPath, String ScriptName, String ScreenName) throws IOException { 
		
		
        String folder1path =System.getProperty("user.dir") + "\\Testresults\\"+fileWithPath+"\\Screenshots\\";
        File folder1 = new File(folder1path);
        // Create a folder name 'Screenshot'
        if (!folder1.exists()) {
            folder1.mkdir();
        }
        // Create a folder name scriptname wise under Screenshot folder
        File folder2 = new File(folder1path+"\\"+ScriptName);
        if (!folder2.exists()) {
           folder2.mkdir();
        }

    }

	

	public static void takeScreenShotonSuccess(WebDriver driver, String status, String ScreenName) {
	    try {
	        // Create Evidence folder
	        String folderPath = System.getProperty("user.dir") + File.separator + "Evidence" + File.separator + getFolderName();
	        File folder = new File(folderPath);
	        if (!folder.exists()) {
	            folder.mkdirs();
	        }

	        // Define screenshot file path
	        String screenshotPath = folderPath + File.separator + ScreenName + "_" + screenShotTime() + "_" + status + ".png";

	        // Capture screenshot
	        File screenshotFile = ((TakesScreenshot) Initialize.getWebDriver()).getScreenshotAs(OutputType.FILE);
	        FileUtils.copyFile(screenshotFile, new File(screenshotPath));

	        // Convert to Base64 for ExtentReports
	        String base64Screenshot = ((TakesScreenshot) Initialize.getWebDriver()).getScreenshotAs(OutputType.BASE64);

	        // Log in ExtentReports
	        ExtentTestManager.getTest().info("Screenshot: " + ScreenName);
	        ExtentTestManager.getTest().addScreenCaptureFromBase64String(base64Screenshot, ScreenName);

	    } catch (IOException e) {
	        e.printStackTrace();
	        ExtentTestManager.getTest().log(Status.FAIL, "Failed to capture screenshot: " + e.getMessage());
	    }
	}

	 public void scrollThePage(WebDriver driver,  String direction) throws IOException {

		
		
	 if(direction.equalsIgnoreCase("Top")) { 
		 JavascriptExecutor js = (JavascriptExecutor) driver;
		 js.executeScript("window.scrollBy(0,-5000)", "");
	  } else { 
		  JavascriptExecutor js = (JavascriptExecutor) driver;
			 js.executeScript("window.scrollBy(0,5000)", "");
	} 
}	


	 public static int getNextAvailableRowIndex(String excelFilePath, String sheetName) throws Exception {
		    FileInputStream fis = new FileInputStream(excelFilePath);
		    Workbook workbook = WorkbookFactory.create(fis);
		    fis.close();

		    org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
		    int nextRowIndex = sheet.getLastRowNum() + 1;

		    workbook.close();
		    return nextRowIndex;
		}
		
		
	 
	 
	 
	 public static void excelWriteMethod(String excelFilePath, String sheetName, int rowIndex, String ColumnHeader, String Value1) throws Exception {
		    FileInputStream fis = new FileInputStream(excelFilePath);
		    Workbook workbook = WorkbookFactory.create(fis);
		    fis.close();

		    Sheet sheet = workbook.getSheet(sheetName);
		    if (sheet == null) {
		        sheet = workbook.createSheet(sheetName);
		    }
		    Row row = sheet.getRow(rowIndex);
		    if (row == null) {
		        row = sheet.createRow(rowIndex);
		    }

		    if (ColumnHeader.equalsIgnoreCase("TestCaseName")) {
		        Cell cell = row.createCell(0); // Column A
		        cell.setCellValue(Value1);
		    } else if (ColumnHeader.equalsIgnoreCase("QuoteNumber")) {
		        Cell cell = row.createCell(1); // Column B
		        cell.setCellValue(Value1);
		    } else if (ColumnHeader.equalsIgnoreCase("PolicyNumber")) {
		        Cell cell = row.createCell(2); // Column C
		        cell.setCellValue(Value1);
		    }

		    FileOutputStream outputStream = new FileOutputStream(excelFilePath);
		    workbook.write(outputStream);
		    outputStream.close();
		    workbook.close();
		}

public boolean validatePDFContent(String filePath, Map<String, String> expectedValues) {
    try {
        PDDocument document = PDDocument.load(new File(filePath));
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String pdfText = pdfStripper.getText(document);

        for (Map.Entry<String, String> entry : expectedValues.entrySet()) {
            String expectedValue = entry.getValue();
            if (expectedValue == null || expectedValue.trim().isEmpty()) {
                System.out.println("Skipping null or empty expected value for key: " + entry.getKey());
                continue;
            }

            if (!pdfText.contains(expectedValue)) {
                System.out.println("Missing expected value: " + entry.getKey() + " -> " + expectedValue);
                document.close();
                return false;
            }
        }

        document.close();
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}




// policy number use in test-----------

public static String policyNumber;
public static String premiumAmount;
public static String totalAmountDue;

//latest folder find

public String getLatestDownloadedPDF(String downloadDirPath) {
    File dir = new File(downloadDirPath);
    File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));

    if (files == null || files.length == 0) {
        System.out.println("No PDF files found in directory: " + downloadDirPath);
        return null;
    }

    Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
    return files[0].getAbsolutePath(); // Return the most recently modified PDF
}
	 
public static String takeScreenshot(WebDriver driver) {
    String screenshotPath = System.getProperty("user.dir") + "\\Screenshots\\" + System.currentTimeMillis() + ".png";
    try {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File destFile = new File(screenshotPath);
        FileUtils.copyFile(srcFile, destFile);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return screenshotPath;
} 
}
