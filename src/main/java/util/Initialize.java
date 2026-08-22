package util;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;
import util.ExtentReports.ExtentTestManager;

public class Initialize {
	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	private static Map<String, String> TDMap = new ConcurrentHashMap<>();
	private static Map<String, String> TOMap = new ConcurrentHashMap<>();
	public static String dateFormat = ReportManager.getDateFormat(ReportManager.vDatetype8);
	public static String dateName = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(new Date());
	public static HashMap<Long, Integer> stepNumHTML = new HashMap<Long, Integer>();
	private static Excel_Class XL = null;

	protected static Properties config;  // ✅ or public if needed


public static void loadConfig() throws Exception {
    String configPath = System.getProperty("user.dir") + File.separator + "TestData" + File.separator + "IBP" + File.separator + "TestData.properties";
    config = new Properties();
    FileInputStream fis = new FileInputStream(configPath);
    config.load(fis);
    System.out.println("✅ Config Loaded Successfully");
}

public static String getConfigValue(String key) {
    if (config == null) {
        throw new RuntimeException("Config not loaded yet!");
    }
    return config.getProperty(key);
}


	public enum updateValue {
		bName, tEndTime, execTime, execStatus
	};

	public enum TestStatus {
		PASS, FAIL, WARNING, Done, Start, End
	};

	

	public static int stepCount = 1;
	// Notepad Variables
	static boolean flag = false;
	public static String nStatus = null;
	static File T = new File("TestData");
	static File Tstdt = new File(T, "TestData.properties");
	static File expTxtdt = new File(T, "ExpectedText.properties");
	static File TstObj = new File(T, "TestObjects.properties");
	static File IBP = new File("TestData/IBP");
	static File IBP_TestData = new File(IBP, "TestData.properties");
	static File IBP_PageObject = new File(IBP, "TestObjects.properties");

	public String getMonth() {

		String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
				"October", "November", "December" };
		Calendar cal = Calendar.getInstance();
		String month = months[cal.get(Calendar.MONTH)];
		return month;
	}

	public static String getTestDataPF(String key) throws Exception {
	    if (config == null) {
	        throw new RuntimeException("Config not loaded yet!");
	    }
	    return config.getProperty(key);
	}

	public static String getExpTxtDataPF(String KeyName) throws Exception {
		Properties prop = null;
		InputStream IP = new FileInputStream(expTxtdt.getAbsolutePath());
		prop = new Properties();
		prop.load(IP);
		String Val = prop.getProperty(KeyName);
		return Val;

	}

	public static String getTestObjectPF(String KeyName) throws Exception {
		Properties prop = null;
		prop = new Properties();
		//String ProjName = System.getProperty("ProjectName");
		// prop.load(IP);
		String ProjName="New1C";
		if (ProjName.equalsIgnoreCase("New1C")) {
			InputStream IP = new FileInputStream(IBP_PageObject.getAbsolutePath());
			prop.load(IP);
		}
		String Val = prop.getProperty(KeyName);
		return Val;

	}


	private static Object PropertyResourceBundle(String string, Locale locale) {
		return null;
	}

	public static String getDataTablePath(String vTablename) {

		String vDTP = System.getProperty("user.dir") + "\\TestData\\" + vTablename + ".xls";
		return vDTP;
	}

	public static String formatIntoHHMMSS(long diffSeconds) {
		diffSeconds = diffSeconds / 1000;
		long hours = diffSeconds / 3600;
		long remainder = diffSeconds % 3600;
		long minutes = remainder / 60;
		long seconds = remainder % 60;

		return ((hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":"
				+ (seconds < 10 ? "0" : "") + seconds);

	}



	public void readTestData(String excelDataSheet, String sheetName) throws Exception {
		Map<String, String> TDMap = new ConcurrentHashMap<>();
	    FileInputStream fis = new FileInputStream(excelDataSheet);
	    Workbook workbook = WorkbookFactory.create(fis);
	    Sheet sheet = workbook.getSheet(sheetName);

	    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
	        Row row = sheet.getRow(i);
	        if (row != null && row.getCell(0) != null) {
	            String key = row.getCell(0).getStringCellValue();
	            String value = row.getCell(1).getStringCellValue();
	            TDMap.put(key, value);
	        }
	    }

	    workbook.close();
	    fis.close();
	    System.out.println("Test Data: " + TDMap);

	}
	public static String getTestData(String Ke) throws Exception {
		String sTemp = null;
		try {
			sTemp = TDMap.get(Ke).toString();
		} catch (Exception e) {

			ReportManager.writeToNotepad("FAIL", "Read Test Data From Hash Map",
					"Check - '*KeyName*' Used As Parameter For Method - '*getTestData*' In Main Script");
			ReportManager.writeToExcel("FAIL", "Read Test Data From Hash Map",
					"Check - '*KeyName*' Used As Parameter For Method - '*getTestData*' In Main Script");
			ReportManager.writeToHTML("FAIL", "Read Test Data From Hash Map",
					"Check - '*KeyName*' Used As Parameter For Method - '*getTestData*' In Main Script");

		}
		return (sTemp);
	}



	public void readTestObject(String excelDataSheet, String sheetName) throws Exception {
	    FileInputStream fis = new FileInputStream(new File(excelDataSheet));
	    Workbook workbook = WorkbookFactory.create(fis);
	    Sheet sheet = workbook.getSheet(sheetName);

	    for (Row row : sheet) {
	        Cell cell0 = row.getCell(0);
	        Cell cell1 = row.getCell(1);
	        Cell cell2 = row.getCell(2);

	        if (cell0 != null && cell0.getStringCellValue() != null) {
	            TOMap.put(cell0.getStringCellValue(),
	                cell0.getStringCellValue() + "," +
	                (cell1 != null ? cell1.getStringCellValue() : "") + "," +
	                (cell2 != null ? cell2.getStringCellValue() : ""));
	        }
	    }

	    workbook.close();
	    fis.close();
	}

	public static String getTestObject(String KeyName) throws Exception {
		String sTemp = null;
		try {
			sTemp = TOMap.get(KeyName).toString();
		} catch (Exception e) {
			ReportManager.writeToNotepad("FAIL", "Read Test Data From Hash Map",
					"Check - '*KeyName*' Used As Parameter For Method - '*getTestData*' In Main Script");
			ReportManager.writeToExcel("FAIL", "Read Test Data From Hash Map",
					"Check - '*KeyName*' Used As Parameter For Method - '*getTestData*' In Main Script");
			ReportManager.writeToHTML("FAIL", "Read Test Data From Hash Map",
					"Check - '*KeyName*' Used As Parameter For Method - '*getTestData*' In Main Script");
		}
		return (sTemp);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void clearHashMap() {
		TOMap.clear();
		TDMap.clear();
	}

	public String getBrowserName(String BrowserName) {

		if (BrowserName.contains("ie")) {
			BrowserName = "INTERNET EXPLORER";
		} else if (BrowserName.contains("firefox")) {
			BrowserName = "FIREFOX";
		} else if (BrowserName.contains("chrome")) {
			BrowserName = "CHROME";
		} else if (BrowserName.contains("edge")) {
			BrowserName = "EDGE";
		} else if (BrowserName.contains("htmlunit")) {
			BrowserName = "HTMLUNIT DRIVER";
		} else if (BrowserName.contains("Browserstackweb")) {
			BrowserName = "BrowserStack Web";
		} else {
			BrowserName = "BrowserStack mobile";
		}
		return BrowserName;
	}

	// Newly ad
	
public static WebDriver initializeDriver(Map<String, String> testData) throws Exception {
    // If driver already exists for this thread, reuse it
    if (getWebDriver() != null) {
        return getWebDriver();
    }

    // ✅ Remove dependency on Execution for suite setup
    String baseBrowser = System.getProperty("browser");
    if (baseBrowser == null || baseBrowser.trim().isEmpty()) {
        baseBrowser = getTestDataPF("browser");
    }

    switch (baseBrowser.toLowerCase()) {
        case "chrome":
            io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
            setDriver(new ChromeDriver());
            break;
        case "firefox":
            io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
            setDriver(new FirefoxDriver());
            break;


	        case "edge":

	        	//io.github.bonigarcia.wdm.WebDriverManager.edgedriver().setup();
	        	//setDriver(new EdgeDriver());

	        	System.setProperty("webdriver.edge.driver", "C:\\Users\\UKAmarSapkal\\BRM_Agency\\All_Drivers\\msedgedriver.exe");
	        	setDriver(new EdgeDriver());
	            break;


	        default:
	                  throw new IllegalArgumentException("Unsupported browser: " + baseBrowser);
	          }


	    // Use getWebDriver() everywhere instead of driver
	    Robot r = new Robot();
	    r.keyPress(KeyEvent.VK_TAB);
	    r.keyPress(KeyEvent.VK_TAB);
	    r.keyPress(KeyEvent.VK_ENTER);
	    r.keyRelease(KeyEvent.VK_ENTER);

	    Thread.sleep(5000);
	    r.keyPress(KeyEvent.VK_ENTER);
	    r.keyRelease(KeyEvent.VK_ENTER);
	    Thread.sleep(5000);

	    Set<String> windowHandles = getWebDriver().getWindowHandles();
	    for (String w : windowHandles) {
	        Thread.sleep(5000);
	        getWebDriver().switchTo().window(w);
	        break;
	    }


	    getWebDriver().manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	       getWebDriver().manage().window().maximize();

	       // ✅ Handle ExtentReports gracefully

	       String testName = (testData != null && testData.get("TestCaseName") != null)
	    	        ? testData.get("TestCaseName")
	    	        : "Suite Initialization";

	    	String description = (testData != null && testData.get("TestcaseDescription") != null)
	    	        ? testData.get("TestcaseDescription")
	    	        : "Initializing browser and environment";


	       Capabilities cap = ((RemoteWebDriver) getWebDriver()).getCapabilities();

ExtentTestManager.startTest(testName,
    "<b>Test description :</b> " + description +
    "<b>, Browser :</b> " + cap.getBrowserName().toLowerCase() +
    "<b>, Platform :</b> " + cap.getPlatformName().toString());


	       return getWebDriver();
	   }

	public Map<String, String> setUp(Method method, String sheetKey) throws Exception {
	    XL = new Excel_Class();
	    String sheetName =sheetKey; // e.g., "SheetName", "SheetName1"
	    Map<String, String> testData = XL.getData(getDataTablePath("TestData"), sheetName, method.getName());
	    
	    String strTestCaseName = method.getName() + "_" + dateName;
	    testData.put("strTestCaseName", strTestCaseName);

	    String group = System.getProperty("group");
	    if (group == null || group.trim().isEmpty()) {
	        group = getTestDataPF("Group");
	    }
	    testData.put("strSysVarGroup", group);

	    return testData;
	}

public static WebDriver getWebDriver() {
    return driver.get();  // ✅ Access ThreadLocal variable
}

// Correct setter
public static void setDriver(WebDriver driverInstance) {
    driver.set(driverInstance);  // ✅ Set value in ThreadLocal
}

	protected static String getFolderName() throws Exception {
		String folder = getTestDataPF("FolderName"); 
		
			if (folder == null | folder.trim().isEmpty()) {
				folder = "DefaultFolder_"; // fallback name
    }		
		String folderName = folder+dateName;
		
		return folderName;
	}
	



	
}
