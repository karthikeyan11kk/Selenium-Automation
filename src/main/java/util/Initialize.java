package util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import util.ExtentReports.ExtentTestManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    public static WebDriver initializeDriver(Map<String, String> testData) throws Exception {
        if (getWebDriver() != null) {
            System.out.println("[Thread-" + Thread.currentThread().getId() + "] WebDriver already initialized, reusing...");
            return getWebDriver();
        }

        if (config == null) {
            loadConfig();
        }

        String baseBrowser = System.getProperty("browser");
        if (baseBrowser == null || baseBrowser.trim().isEmpty()) {
            baseBrowser = getTestDataPF("browser");
        }

        WebDriver driver;
        long threadId = Thread.currentThread().getId();

        switch (baseBrowser.toLowerCase()) {
            case "chrome":
                driver = initializeChromeDriver(threadId);
                break;
            case "firefox":
                driver = initializeFirefoxDriver(threadId);
                break;
            case "edge":
                driver = initializeEdgeDriver(threadId);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + baseBrowser);
        }

        setWebDriver(driver);
        configureDriverTimeouts(driver, threadId);
        Thread.sleep(2000);
        handleBrowserPopups(driver, threadId);
        maximizeWindow(driver, threadId);
        initializeExtentReports(driver, testData, threadId);

        System.out.println("[Thread-" + threadId + "] WebDriver initialized successfully");
        return driver;
    }

    private static WebDriver initializeChromeDriver(long threadId) throws Exception {
        ChromeOptions chromeOptions = new ChromeOptions();

        chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("--disable-notifications");
        chromeOptions.addArguments("--disable-popup-blocking");
        chromeOptions.addArguments("--disable-infobars");
        chromeOptions.addArguments("--disable-extensions");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
        chromeOptions.addArguments("--remote-allow-origins=*");
        chromeOptions.addArguments("--no-first-run");
        chromeOptions.addArguments("--no-service-autorun");
        chromeOptions.addArguments("--password-store=basic");
        chromeOptions.addArguments("--disable-features=ChromeWhatsNewUI");

        String chromeUserData = System.getProperty("user.home") + File.separator
                + "selenium" + File.separator + "chrome_profile_thread_" + threadId;
        chromeOptions.addArguments("--user-data-dir=" + chromeUserData);
        chromeOptions.addArguments("--profile-directory=Default");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_settings.popups", 0);
        chromeOptions.setExperimentalOption("prefs", prefs);
        chromeOptions.setExperimentalOption("excludeSwitches",
                Arrays.asList("enable-automation", "enable-logging"));

        try {
            WebDriverManager.chromedriver().setup();
        } catch (Exception e) {
            System.out.println("[Thread-" + threadId + "] WebDriverManager failed, using system chromedriver");
        }

        WebDriver driver = new ChromeDriver(chromeOptions);
        System.out.println("[Thread-" + threadId + "] Chrome browser initialized");
        return driver;
    }

    private static WebDriver initializeFirefoxDriver(long threadId) throws Exception {
        FirefoxOptions firefoxOptions = new FirefoxOptions();

        firefoxOptions.addArguments("--start-maximized");
        firefoxOptions.addArguments("--disable-notifications");

        String firefoxProfile = System.getProperty("user.home") + File.separator
                + "selenium" + File.separator + "firefox_profile_thread_" + threadId;
        firefoxOptions.addArguments("-profile", firefoxProfile);

        firefoxOptions.addPreference("browser.startup.homepage_override.mstone", "ignore");
        firefoxOptions.addPreference("browser.rights.3.shown", true);
        firefoxOptions.addPreference("datareporting.policy.firstRunURL", "");
        firefoxOptions.addPreference("browser.startup.homepage_override.buildID", "20100101");

        try {
            WebDriverManager.firefoxdriver().setup();
        } catch (Exception e) {
            System.out.println("[Thread-" + threadId + "] WebDriverManager failed, using system geckodriver");
        }

        WebDriver driver = new FirefoxDriver(firefoxOptions);
        System.out.println("[Thread-" + threadId + "] Firefox browser initialized");
        return driver;
    }

    private static WebDriver initializeEdgeDriver(long threadId) throws Exception {
        EdgeOptions edgeOptions = new EdgeOptions();

        edgeOptions.addArguments("--no-first-run");
        edgeOptions.addArguments("--no-default-browser-check");
        edgeOptions.addArguments("--disable-popup-blocking");
        edgeOptions.addArguments("--disable-features=msEdgeProfilePickerOnStartup");
        edgeOptions.addArguments("--disable-features=msEdgeWelcomeExperience");
        edgeOptions.addArguments("--disable-features=EdgeWhatsNewUI");
        edgeOptions.addArguments("--start-maximized");
        edgeOptions.addArguments("--disable-notifications");
        edgeOptions.addArguments("--disable-infobars");
        edgeOptions.addArguments("--disable-extensions");
        edgeOptions.addArguments("--no-sandbox");
        edgeOptions.addArguments("--disable-dev-shm-usage");
        edgeOptions.addArguments("--disable-blink-features=AutomationControlled");
        edgeOptions.addArguments("--remote-allow-origins=*");
        edgeOptions.addArguments("--no-service-autorun");
        edgeOptions.addArguments("--password-store=basic");

        String edgeUserData = System.getProperty("user.home") + File.separator
                + "selenium" + File.separator + "edge_profile_thread_" + threadId;
        edgeOptions.addArguments("--user-data-dir=" + edgeUserData);
        edgeOptions.addArguments("--profile-directory=Default");

        Map<String, Object> edgePrefs = new HashMap<>();
        edgePrefs.put("profile.default_content_setting_values.notifications", 2);
        edgePrefs.put("credentials_enable_service", false);
        edgePrefs.put("profile.password_manager_enabled", false);
        edgePrefs.put("profile.default_content_settings.popups", 0);
        edgePrefs.put("download.default_directory", getDownloadDir());
        edgePrefs.put("download.prompt_for_download", false);
        edgeOptions.setExperimentalOption("prefs", edgePrefs);
        edgeOptions.setExperimentalOption("excludeSwitches",
                Arrays.asList("enable-automation", "enable-logging"));

        String edgeDriverPath = getTestDataPF("edgeDriverPath");
        if (edgeDriverPath == null || edgeDriverPath.trim().isEmpty()) {
            edgeDriverPath = System.getProperty("user.dir") + File.separator
                    + "All_Drivers" + File.separator + "msedgedriver.exe";
        }
        if (!new File(edgeDriverPath).exists()) {
            throw new IllegalStateException("msedgedriver.exe not found at: " + edgeDriverPath
                    + " - download the version matching your installed Edge from "
                    + "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/ "
                    + "and either place it there or set edgeDriverPath in TestData.properties.");
        }
        verifyEdgeDriverVersionMatches(edgeDriverPath);
        System.setProperty("webdriver.edge.driver", edgeDriverPath);

        WebDriver driver = new EdgeDriver(edgeOptions);
        System.out.println("[Thread-" + threadId + "] Edge browser initialized with enhanced settings (driver: " + edgeDriverPath + ")");
        return driver;
    }

    private static void verifyEdgeDriverVersionMatches(String edgeDriverPath) {
        System.out.println("Checking msedgedriver.exe version against installed Edge...");

        String driverVersionOutput;
        try {
            driverVersionOutput = runCommand(edgeDriverPath, "--version");
        } catch (Exception e) {
            System.err.println("Could not run '" + edgeDriverPath + " --version' to check its version "
                    + "(continuing without pre-check): " + e.getMessage());
            return;
        }
        Integer driverMajor = extractMajorVersion(driverVersionOutput);
        System.out.println("msedgedriver.exe reports: " + driverVersionOutput.trim()
                + (driverMajor != null ? " (major version " + driverMajor + ")" : " (could not parse a version number)"));

        String edgeBinary = null;
        for (String candidate : new String[]{
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe"}) {
            if (new File(candidate).exists()) {
                edgeBinary = candidate;
                break;
            }
        }
        if (edgeBinary == null) {
            System.err.println("Could not find msedge.exe at either standard install path - skipping "
                    + "version comparison (Selenium will attempt to launch regardless).");
            return;
        }

        String edgeVersionOutput;
        try {
            edgeVersionOutput = runCommand(edgeBinary, "--version");
        } catch (Exception e) {
            System.err.println("Could not run '" + edgeBinary + " --version' to check its version "
                    + "(continuing without pre-check): " + e.getMessage());
            return;
        }
        Integer edgeMajor = extractMajorVersion(edgeVersionOutput);
        System.out.println("Installed Edge reports: " + edgeVersionOutput.trim()
                + (edgeMajor != null ? " (major version " + edgeMajor + ")" : " (could not parse a version number)"));

        if (driverMajor == null || edgeMajor == null) {
            System.err.println("Could not parse a version number from one or both --version outputs above - "
                    + "skipping the mismatch check.");
            return;
        }

        if (!driverMajor.equals(edgeMajor)) {
            String banner = "\n"
                    + "================================================================\n"
                    + "  EDGE DRIVER VERSION MISMATCH - THIS IS A FILE PROBLEM, NOT CODE\n"
                    + "================================================================\n"
                    + "  msedgedriver.exe : version " + driverMajor + ".x  (at " + edgeDriverPath + ")\n"
                    + "  Installed Edge   : version " + edgeMajor + ".x\n"
                    + "\n"
                    + "  FIX: download the msedgedriver build matching " + edgeMajor + ".x from\n"
                    + "  https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/\n"
                    + "  and REPLACE the file at " + edgeDriverPath + "\n"
                    + "================================================================\n";
            System.err.println(banner);
            throw new IllegalStateException(banner);
        }

        System.out.println("Driver and Edge versions match (" + driverMajor + ".x) - proceeding.");
    }

    private static Integer extractMajorVersion(String versionOutput) {
        if (versionOutput == null || versionOutput.isBlank()) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\.\\d+\\.\\d+\\.\\d+").matcher(versionOutput);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        m = java.util.regex.Pattern.compile("\\b(\\d{2,3})\\.\\d").matcher(versionOutput);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private static String runCommand(String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String output;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(p.getInputStream()))) {
            output = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
        }
        boolean finished = p.waitFor(5, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new java.util.concurrent.TimeoutException(
                    String.join(" ", command) + " did not exit within 5 seconds");
        }
        return output;
    }

    private static void configureDriverTimeouts(WebDriver driver, long threadId) {
        try {
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
            driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
            System.out.println("[Thread-" + threadId + "] Timeouts configured");
        } catch (Exception e) {
            System.err.println("[Thread-" + threadId + "] Failed to set timeouts: " + e.getMessage());
        }
    }

    private static void handleBrowserPopups(WebDriver driver, long threadId) {
        try {
            Thread.sleep(1500);

            Set<String> allWindows = driver.getWindowHandles();
            int windowCount = allWindows.size();

            System.out.println("[Thread-" + threadId + "] Initial window count: " + windowCount);

            if (windowCount > 1) {
                System.out.println("[Thread-" + threadId + "] Multiple windows detected (" + windowCount + "), handling popups...");

                String mainWindow = allWindows.iterator().next();
                System.out.println("[Thread-" + threadId + "] Main window (FIRST): " + mainWindow);

                int closedCount = 0;

                for (String windowHandle : allWindows) {
                    if (!windowHandle.equals(mainWindow)) {
                        try {
                            System.out.println("[Thread-" + threadId + "] Attempting to close popup: " + windowHandle);
                            driver.switchTo().window(windowHandle);
                            Thread.sleep(300);
                            driver.close();
                            closedCount++;
                            System.out.println("[Thread-" + threadId + "] Successfully closed popup window #" + closedCount);
                        } catch (Exception e) {
                            System.err.println("[Thread-" + threadId + "] Failed to close popup " + windowHandle + ": " + e.getMessage());
                        }
                    }
                }

                try {
                    driver.switchTo().window(mainWindow);
                    System.out.println("[Thread-" + threadId + "] Switched back to main window");

                    String currentWindow = driver.getWindowHandle();
                    if (!currentWindow.equals(mainWindow)) {
                        System.err.println("[Thread-" + threadId + "] WARNING: Not on main window! Current: " + currentWindow);
                    }

                    int finalCount = driver.getWindowHandles().size();
                    System.out.println("[Thread-" + threadId + "] Final window count: " + finalCount + " (closed " + closedCount + " popups)");

                } catch (Exception e) {
                    System.err.println("[Thread-" + threadId + "] CRITICAL: Failed to switch to main window: " + e.getMessage());
                    throw e;
                }

            } else {
                System.out.println("[Thread-" + threadId + "] Single window detected, no popup handling needed");
            }

        } catch (Exception e) {
            System.err.println("[Thread-" + threadId + "] Window handling error: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("no such window")) {
                throw new RuntimeException("Browser window was closed unexpectedly during popup handling", e);
            }
        }
    }

    private static void maximizeWindow(WebDriver driver, long threadId) {
        try {
            driver.manage().window().maximize();
            System.out.println("[Thread-" + threadId + "] Window maximized");
        } catch (Exception e) {
            System.err.println("[Thread-" + threadId + "] Failed to maximize window: " + e.getMessage());
        }
    }

    private static void initializeExtentReports(WebDriver driver, Map<String, String> testData, long threadId) {
        try {
            String testName = (testData != null && testData.get("TestCaseName") != null)
                    ? testData.get("TestCaseName")
                    : "Suite Initialization - Thread " + threadId;

            String description = (testData != null && testData.get("TestcaseDescription") != null)
                    ? testData.get("TestcaseDescription")
                    : "Initializing browser and environment";

            Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();

            synchronized (ExtentTestManager.class) {
                ExtentTestManager.startTest(testName,
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
        return driverThreadLocal.get();
    }

    public static void setWebDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static void quitWebDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                long threadId = Thread.currentThread().getId();
                System.out.println("[Thread-" + threadId + "] Closing WebDriver...");
                driver.quit();
                System.out.println("[Thread-" + threadId + "] WebDriver closed successfully");
            } catch (Exception e) {
                System.err.println("Error closing WebDriver: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
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
}
