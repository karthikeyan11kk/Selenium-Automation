package utils;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Initialize {

    // Thread-safe driver management
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    // Thread-safe data maps
    private static ThreadLocal<Map<String, String>> TDMap = ThreadLocal.withInitial(ConcurrentHashMap::new);
    private static ThreadLocal<Map<String, String>> TOMap = ThreadLocal.withInitial(ConcurrentHashMap::new);

    // Thread-safe step counting
    private static ThreadLocal<Integer> stepCount = ThreadLocal.withInitial(() -> 1);
    private static Map<Long, Integer> stepNumHTML = new ConcurrentHashMap<>();

    // Shared (thread-safe) configuration - loaded once, read-only after initialization
    private static volatile Properties config;
    private static final Object configLock = new Object();

    public static final String dateFormat = ReportManager.getDateFormat(ReportManager.vDatetype8);
    public static final String dateName = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(new Date());

    private static ThreadLocal<Excel_Class> XL = ThreadLocal.withInitial(Excel_Class::new);

    private static final File T = new File("TestData");
    private static final File Tstdt = new File(T, "TestData.properties");
    private static final File expTxtdt = new File(T, "ExpectedText.properties");
    private static final File TstObj = new File(T, "TestObjects.properties");
    private static final File IBP = new File("TestData/IBP");
    private static final File IBP_TestData = new File(IBP, "TestData.properties");

    // FIX: only TestData/IBP/TestObjects.properties actually exists on disk.
    // The array this used to loop over also referenced
    // "OfficeTestObjects.properties", which doesn't exist - FileInputStream
    // would throw FileNotFoundException the moment getTestObjectPF() was
    // called (i.e. on the very first login locator lookup), breaking the
    // whole automation right after the browser successfully launched.
    private static final File IBP_PageObject = new File(IBP, "TestObjects.properties");

    public enum updateValue {
        bName, tEndTime, execTime, execStatus
    }

    public enum TestStatus {
        PASS, FAIL, WARNING, Done, Start, End
    }

    public static void loadConfig() throws Exception {
        if (config == null) {
            synchronized (configLock) {
                if (config == null) {
                    String configPath = System.getProperty("user.dir") + File.separator + "TestData"
                            + File.separator + "IBP" + File.separator + "TestData.properties";
                    Properties tempConfig = new Properties();
                    try (FileInputStream fis = new FileInputStream(configPath)) {
                        tempConfig.load(fis);
                    }
                    config = tempConfig;
                    System.out.println("Config Loaded Successfully");
                }
            }
        }
    }

    public static String getConfigValue(String key) {
        if (config == null) {
            throw new RuntimeException("Config not loaded yet! Call loadConfig() first.");
        }
        return config.getProperty(key);
    }

    public static String getTestDataPF(String key) throws Exception {
        if (config == null) {
            loadConfig();
        }
        return config.getProperty(key);
    }

    public static String getExpTxtDataPF(String KeyName) throws Exception {
        Properties prop = new Properties();
        try (InputStream IP = new FileInputStream(expTxtdt.getAbsolutePath())) {
            prop.load(IP);
        }
        return prop.getProperty(KeyName);
    }

    public static String getTestObjectPF(String KeyName) throws Exception {
        Properties prop = new Properties();
        try (InputStream IP = new FileInputStream(IBP_PageObject.getAbsolutePath())) {
            prop.load(IP);
        }
        return prop.getProperty(KeyName);
    }

    public void readTestData(String excelDataSheet, String sheetName) throws Exception {
        Map<String, String> localTDMap = TDMap.get();
        localTDMap.clear();

        try (FileInputStream fis = new FileInputStream(excelDataSheet);
             org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found in " + excelDataSheet);
            }

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null && row.getCell(1) != null) {
                    String key = getCellValueAsString(row.getCell(0));
                    String value = getCellValueAsString(row.getCell(1));
                    localTDMap.put(key, value);
                }
            }
        }

        System.out.println("[Thread-" + Thread.currentThread().getId() + "] Test Data Loaded: " + localTDMap.size() + " entries");
    }

    public void readTestObject(String excelDataSheet, String sheetName) throws Exception {
        Map<String, String> localTOMap = TOMap.get();
        localTOMap.clear();

        try (FileInputStream fis = new FileInputStream(new File(excelDataSheet));
             org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found");
            }

            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                org.apache.poi.ss.usermodel.Cell cell0 = row.getCell(0);
                org.apache.poi.ss.usermodel.Cell cell1 = row.getCell(1);
                org.apache.poi.ss.usermodel.Cell cell2 = row.getCell(2);

                if (cell0 != null && cell0.getStringCellValue() != null && !cell0.getStringCellValue().trim().isEmpty()) {
                    String key = getCellValueAsString(cell0);
                    String val1 = (cell1 != null) ? getCellValueAsString(cell1) : "";
                    String val2 = (cell2 != null) ? getCellValueAsString(cell2) : "";
                    localTOMap.put(key, key + "," + val1 + "," + val2);
                }
            }
        }

        System.out.println("[Thread-" + Thread.currentThread().getId() + "] Test Objects Loaded: " + localTOMap.size() + " entries");
    }

    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public static String getTestData(String key) throws Exception {
        Map<String, String> localTDMap = TDMap.get();
        String value = localTDMap.get(key);

        if (value == null) {
            String errorMsg = "Key '" + key + "' not found in Test Data for Thread-" + Thread.currentThread().getId();
            System.err.println(errorMsg);

            ReportManager.writeToNotepad("FAIL", "Read Test Data From Hash Map",
                    "Check - '*KeyName*' Used As Parameter For Method - '*getTestData*' In Main Script. Key: " + key);
            ReportManager.writeToExcel("FAIL", "Read Test Data From Hash Map",
                    "Check - '*KeyName*' Used As Parameter For Method - '*getTestData*' In Main Script. Key: " + key);
            ReportManager.writeToHTML("FAIL", "Read Test Data From Hash Map",
                    "Check - '*KeyName*' Used As Parameter For Method - '*getTestData*' In Main Script. Key: " + key);

            throw new IllegalArgumentException(errorMsg);
        }

        return value;
    }

    public static String getTestObject(String KeyName) throws Exception {
        Map<String, String> localTOMap = TOMap.get();
        String value = localTOMap.get(KeyName);

        if (value == null) {
            String errorMsg = "Key '" + KeyName + "' not found in Test Objects for Thread-" + Thread.currentThread().getId();
            System.err.println(errorMsg);

            ReportManager.writeToNotepad("FAIL", "Read Test Object From Hash Map",
                    "Check - '*KeyName*' Used As Parameter For Method - '*getTestObject*' In Main Script. Key: " + KeyName);
            ReportManager.writeToExcel("FAIL", "Read Test Object From Hash Map",
                    "Check - '*KeyName*' Used As Parameter For Method - '*getTestObject*' In Main Script. Key: " + KeyName);
            ReportManager.writeToHTML("FAIL", "Read Test Object From Hash Map",
                    "Check - '*KeyName*' Used As Parameter For Method - '*getTestObject*' In Main Script. Key: " + KeyName);

            throw new IllegalArgumentException(errorMsg);
        }

        return value;
    }

    public void clearHashMap() {
        TOMap.get().clear();
        TDMap.get().clear();
        System.out.println("[Thread-" + Thread.currentThread().getId() + "] HashMaps cleared");
    }

    public static int getStepCount() {
        return stepCount.get();
    }

    public static void incrementStepCount() {
        stepCount.set(stepCount.get() + 1);
    }

    public static void resetStepCount() {
        stepCount.set(1);
    }

    public String getBrowserName(String BrowserName) {
        if (BrowserName.contains("ie")) {
            return "INTERNET EXPLORER";
        } else if (BrowserName.contains("firefox")) {
            return "FIREFOX";
        } else if (BrowserName.contains("chrome")) {
            return "CHROME";
        } else if (BrowserName.contains("edge")) {
            return "EDGE";
        } else if (BrowserName.contains("htmlunit")) {
            return "HTMLUNIT DRIVER";
        } else if (BrowserName.contains("Browserstackweb")) {
            return "BrowserStack Web";
        } else {
            return "BrowserStack mobile";
        }
    }

    /**
     * Thin facade over driver.DriverManager / driver.WebDriverFactory, which
     * now own all the real browser-creation logic (this mirrors the demo
     * framework's architecture: driver package owns lifecycle + creation,
     * separate from config/utility classes). Kept here, under the same
     * name, so the dozens of existing call sites across Common_Methods,
     * LoginPage, New1CShop, New1CPropertyOwners, ShopPdfValidation etc.
     * don't all need touching - they still call Initialize.getWebDriver()
     * etc. exactly as before, it just delegates now.
     */
    public static WebDriver initializeDriver(Map<String, String> testData) throws Exception {
        if (getWebDriver() != null) {
            return getWebDriver();
        }
        if (config == null) {
            loadConfig();
        }
        driver.DriverManager.initializeDriver();
        WebDriver webDriver = driver.DriverManager.getDriver();
        initializeExtentReports(webDriver, testData, Thread.currentThread().getId());
        return webDriver;
    }

    private static void initializeExtentReports(WebDriver webDriver, Map<String, String> testData, long threadId) {
        try {
            String testName = (testData != null && testData.get("TestCaseName") != null)
                    ? testData.get("TestCaseName")
                    : "Suite Initialization - Thread " + threadId;

            String description = (testData != null && testData.get("TestcaseDescription") != null)
                    ? testData.get("TestcaseDescription")
                    : "Initializing browser and environment";

            Capabilities cap = ((RemoteWebDriver) webDriver).getCapabilities();

            synchronized (ExtentReporter.class) {
                ExtentReporter.startTest(testName,
                        "<b>Test description:</b> " + description +
                                "<b>, Browser:</b> " + cap.getBrowserName().toLowerCase() +
                                "<b>, Platform:</b> " + cap.getPlatformName().toString() +
                                "<b>, Thread:</b> " + threadId);
            }
        } catch (Exception e) {
            System.err.println("[Thread-" + threadId + "] Failed to create ExtentTest: " + e.getMessage());
        }
    }

    public Map<String, String> setUp(Method method, String sheetKey) throws Exception {
        Excel_Class xl = XL.get();
        String sheetName = sheetKey;
        Map<String, String> testData = xl.getData(getDataTablePath("TestData"), sheetName, method.getName());

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
        return driver.DriverManager.getDriver();
    }

    public static void setWebDriver(WebDriver webDriver) {
        driver.DriverManager.setDriver(webDriver);
    }

    public static void quitWebDriver() {
        driver.DriverManager.quitDriver();
    }


    public static String getDataTablePath(String vTablename) {
        if ("TestData".equalsIgnoreCase(vTablename)) {
            try {
                String override = getTestDataPF("testDataFilePath");
                if (override != null && !override.trim().isEmpty()) {
                    return override.trim();
                }
            } catch (Exception ignored) {
                // property not set / config not loaded yet - use default below
            }
        }
        return System.getProperty("user.dir") + File.separator + "TestData" + File.separator + vTablename + ".xls";
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

    public String getMonth() {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December"};
        Calendar cal = Calendar.getInstance();
        return months[cal.get(Calendar.MONTH)];
    }

    public static String getQPNumbersFilePath() {
        try {
            String override = getTestDataPF("qpNumbersFilePath");
            if (override != null && !override.trim().isEmpty()) {
                return override.trim();
            }
        } catch (Exception ignored) {
            // property not set / config not loaded yet - use default below
        }
        return System.getProperty("user.dir") + File.separator + "TestData" + File.separator + "Q&P-Numbers.xls";
    }

    protected static String getFolderName() throws Exception {
        String folder = getTestDataPF("FolderName");

        if (folder == null || folder.trim().isEmpty()) {
            folder = "DefaultFolder_";
        }
        return folder + dateName;
    }

    public static String getDownloadDir() {
        try {
            String override = getTestDataPF("downloadDir");
            if (override != null && !override.trim().isEmpty()) {
                return override.trim();
            }
        } catch (Exception ignored) {
            // property not set / config not loaded yet - use default below
        }
        return System.getProperty("user.home") + File.separator + "Downloads";
    }

    // ================================================================
    // Centralized screenshot folder (single source of truth)
    // ================================================================
    // Previously Common_Methods.java had SIX different screenshot-saving
    // methods, each with its own folder scheme (ErrorScreen/, Evidence/,
    // Testresults/.../Screenshots/, a flat Screenshots/ with just a
    // timestamp filename, etc) - screenshots for the same test case ended
    // up scattered across five or six different top-level folders. This
    // ThreadLocal is the single source of truth every screenshot method now
    // reads from: one root "Screenshots" folder, one subfolder per test
    // case (name + timestamp), everything for that test case lands there.
    private static final ThreadLocal<String> currentScreenshotFolder = new ThreadLocal<>();

    /**
     * Call once per scenario/test case (Hooks does this as soon as the test
     * case name is known) to start a fresh, uniquely-named subfolder for
     * every screenshot taken during that test case.
     */
    public static void setCurrentScreenshotFolder(String testCaseName) {
        String safeName = (testCaseName == null || testCaseName.trim().isEmpty()) ? "UnknownTestCase" : testCaseName.trim();
        String folderName = safeName + "_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        currentScreenshotFolder.set(folderName);
    }

    /**
     * Returns (creating if needed) the folder every screenshot method should
     * write into: <screenshotsRootDir or project root>/Screenshots/<TestCaseName_Timestamp>/
     * Configurable root via TestData.properties "screenshotsRootDir"; falls
     * back to the project root if unset.
     */
    public static String getScreenshotDir() {
        String folder = currentScreenshotFolder.get();
        if (folder == null || folder.isBlank()) {
            // No scenario has set a folder yet (e.g. a screenshot taken
            // outside the normal Hooks flow) - start one now rather than
            // scattering into yet another ad-hoc location.
            folder = "UnassignedTestCase_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            currentScreenshotFolder.set(folder);
        }

        String root;
        try {
            String override = getTestDataPF("screenshotsRootDir");
            root = (override != null && !override.trim().isEmpty())
                    ? override.trim()
                    : System.getProperty("user.dir") + File.separator + "Screenshots";
        } catch (Exception e) {
            root = System.getProperty("user.dir") + File.separator + "Screenshots";
        }

        String fullPath = root + File.separator + folder;
        new File(fullPath).mkdirs();
        return fullPath;
    }
}
