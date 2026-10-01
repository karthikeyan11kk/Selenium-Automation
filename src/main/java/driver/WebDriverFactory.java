package driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.PropertyfileUtility;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Owns all browser-creation concerns, matching the demo framework's
 * driver.WebDriverFactory: singleton instance holder + createDriver().
 * DriverManager (ThreadLocal lifecycle) calls into this; this class doesn't
 * know or care about ThreadLocal at all, same separation of concerns as the
 * reference project.
 */
public class WebDriverFactory {
    private static final Logger log = LoggerFactory.getLogger(WebDriverFactory.class);


    private static volatile WebDriverFactory instance;

    private WebDriverFactory() {
    }

    public static WebDriverFactory getInstance() {
        if (instance == null) {
            synchronized (WebDriverFactory.class) {
                if (instance == null) {
                    instance = new WebDriverFactory();
                }
            }
        }
        return instance;
    }

    private static final java.util.concurrent.atomic.AtomicInteger createDriverCallCount =
            new java.util.concurrent.atomic.AtomicInteger(0);

    public static WebDriver createDriver() throws Exception {
        // Diagnostic for "two browsers opening": if this prints "call #2"
        // or higher, createDriver() is genuinely being invoked more than
        // once from code (a real bug - the stack trace below will show
        // exactly where from). If you only ever see "call #1" in the
        // console yet still see two Edge windows, it's NOT a code-level
        // duplicate driver - it's something Edge/Windows itself is doing
        // (a restore-session prompt, a second unrelated Edge window already
        // open, etc.) and needs a different fix entirely.
        int callNumber = createDriverCallCount.incrementAndGet();
        log.info("=== WebDriverFactory.createDriver() call #" + callNumber
                + " on thread " + Thread.currentThread().getId() + " ===");
        if (callNumber > 1) {
            log.error("WARNING: createDriver() called more than once in this JVM run! Stack trace:");
            Thread.dumpStack();
        }

        String browser = System.getProperty("browser");
        if (browser == null || browser.trim().isEmpty()) {
            browser = PropertyfileUtility.getProperty("browser");
        }
        if (browser == null) {
            throw new IllegalStateException("No 'browser' property set (system property or TestData.properties).");
        }
        browser = browser.toLowerCase();

        long threadId = Thread.currentThread().getId();
        WebDriver driver;

        switch (browser) {
            case "chrome":
                driver = createChromeDriver(threadId);
                break;
            case "firefox":
                driver = createFirefoxDriver(threadId);
                break;
            case "edge":
                driver = createEdgeDriver(threadId);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        // Capture the real main window handle IMMEDIATELY, before any extra
        // window/popup has a chance to appear. Picking "the main window"
        // later via Set iteration order is not reliable - it could just as
        // easily pick a popup as "main" and close the real automation window
        // instead (this was a real, confirmed bug in an earlier version).
        String mainWindowHandle = driver.getWindowHandle();
        configureTimeouts(driver, threadId);
        waitForDocumentReady(driver, 5);
        closeExtraPopupWindows(driver, threadId, mainWindowHandle);
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            log.error("[Thread-" + threadId + "] Failed to maximize window: " , e);
        }

        log.info("[Thread-" + threadId + "] WebDriver created successfully (" + browser + ")");
        return driver;
    }

    private static WebDriver createChromeDriver(long threadId) {
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
        chromeOptions.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation", "enable-logging"));

        try {
            WebDriverManager.chromedriver().setup();
        } catch (Exception e) {
            log.info("[Thread-" + threadId + "] WebDriverManager failed, using system chromedriver");
        }

        return new ChromeDriver(chromeOptions);
    }

    private static WebDriver createFirefoxDriver(long threadId) {
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
            log.info("[Thread-" + threadId + "] WebDriverManager failed, using system geckodriver");
        }

        return new FirefoxDriver(firefoxOptions);
    }

    /**
     * Local msedgedriver.exe (not WebDriverManager) - some networks can't
     * reach msedgedriver.azureedge.net. Path is configurable via
     * "edgeDriverPath" in TestData.properties, falling back to
     * <project root>\All_Drivers\msedgedriver.exe. Includes a pre-flight
     * version check against the installed Edge browser, since a mismatch
     * there produces Selenium's own hard-to-parse SessionNotCreatedException.
     */
    private static WebDriver createEdgeDriver(long threadId) throws Exception {
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
        edgePrefs.put("download.default_directory", downloadDir());
        edgePrefs.put("download.prompt_for_download", false);
        edgeOptions.setExperimentalOption("prefs", edgePrefs);
        edgeOptions.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation", "enable-logging"));

        String edgeDriverPath = PropertyfileUtility.getProperty("edgeDriverPath");
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
        log.info("[Thread-" + threadId + "] Edge browser initialized (driver: " + edgeDriverPath + ")");
        return driver;
    }

    private static String downloadDir() {
        String override = PropertyfileUtility.getProperty("downloadDir");
        if (override != null && !override.trim().isEmpty()) {
            return override.trim();
        }
        return System.getProperty("user.home") + File.separator + "Downloads";
    }

    private static void verifyEdgeDriverVersionMatches(String edgeDriverPath) {
        log.info("Checking msedgedriver.exe version against installed Edge...");

        String driverVersionOutput;
        try {
            driverVersionOutput = runCommand(edgeDriverPath, "--version");
        } catch (Exception e) {
            log.error("Could not run '" + edgeDriverPath + " --version' to check its version "
                    + "(continuing without pre-check): " + e.getMessage());
            return;
        }
        Integer driverMajor = extractMajorVersion(driverVersionOutput);
        log.info("msedgedriver.exe reports: " + driverVersionOutput.trim()
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
            log.error("Could not find msedge.exe at either standard install path - skipping "
                    + "version comparison (Selenium will attempt to launch regardless).");
            return;
        }

        String edgeVersionOutput;
        try {
            edgeVersionOutput = runCommand(edgeBinary, "--version");
        } catch (Exception e) {
            log.error("Could not run '" + edgeBinary + " --version' to check its version "
                    + "(continuing without pre-check): " + e.getMessage());
            return;
        }
        Integer edgeMajor = extractMajorVersion(edgeVersionOutput);
        log.info("Installed Edge reports: " + edgeVersionOutput.trim()
                + (edgeMajor != null ? " (major version " + edgeMajor + ")" : " (could not parse a version number)"));

        if (driverMajor == null || edgeMajor == null) {
            log.error("Could not parse a version number from one or both --version outputs above - "
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
            log.error(banner);
            throw new IllegalStateException(banner);
        }

        log.info("Driver and Edge versions match (" + driverMajor + ".x) - proceeding.");
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
            throw new java.util.concurrent.TimeoutException(String.join(" ", command) + " did not exit within 5 seconds");
        }
        return output;
    }

    private static void configureTimeouts(WebDriver driver, long threadId) {
        try {
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
            driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("[Thread-" + threadId + "] Failed to set timeouts: " , e);
        }
    }

    /**
     * Closes any extra browser-controlled windows/tabs that pop up right
     * after launch (e.g. "what's new" pages), keeping the window captured
     * at launch time as "main" - see the comment in createDriver() for why
     * that matters.
     */
    private static void closeExtraPopupWindows(WebDriver driver, long threadId, String mainWindowHandle) {
        try {
            // FluentWait polling for a second window to appear, up to 5s -
            // replaces a fixed-interval Thread.sleep loop with a real,
            // condition-based Selenium wait. In the common case (no popup
            // ever appears) this legitimately times out after 5s having
            // polled the whole way, same total wait as before but now via
            // the framework's own wait abstraction rather than a hand-rolled
            // sleep loop.
            Set<String> allWindows;
            try {
                allWindows = new org.openqa.selenium.support.ui.FluentWait<>(driver)
                        .withTimeout(java.time.Duration.ofSeconds(5))
                        .pollingEvery(java.time.Duration.ofMillis(500))
                        .ignoring(org.openqa.selenium.NoSuchWindowException.class)
                        .until(d -> {
                            Set<String> handles = d.getWindowHandles();
                            return handles.size() > 1 ? handles : null;
                        });
            } catch (org.openqa.selenium.TimeoutException te) {
                allWindows = driver.getWindowHandles(); // no second window ever appeared - expected/common case
            }

            log.info("[Thread-" + threadId + "] Window count after polling: " + allWindows.size()
                    + " (handles: " + allWindows + ")");
            if (allWindows.size() <= 1) {
                return;
            }

            log.info("[Thread-" + threadId + "] Multiple windows detected (" + allWindows.size() + "), closing extras...");
            String mainWindow = allWindows.contains(mainWindowHandle) ? mainWindowHandle : allWindows.iterator().next();

            int closedCount = 0;
            for (String windowHandle : allWindows) {
                if (!windowHandle.equals(mainWindow)) {
                    try {
                        driver.switchTo().window(windowHandle);
                        log.info("[Thread-" + threadId + "] Closing extra window - title: '"
                                + safeTitle(driver) + "', url: '" + safeUrl(driver) + "'");
                        driver.close();
                        closedCount++;
                    } catch (Exception e) {
                        log.error("[Thread-" + threadId + "] Failed to close popup " + windowHandle + ": " , e);
                    }
                }
            }

            driver.switchTo().window(mainWindow);
            log.info("[Thread-" + threadId + "] Closed " + closedCount + " popup window(s), back on main window.");
        } catch (Exception e) {
            log.error("[Thread-" + threadId + "] Popup handling error: " , e);
        }
    }

    private static String safeTitle(WebDriver driver) {
        try {
            return driver.getTitle();
        } catch (Exception e) {
            return "(unavailable)";
        }
    }

    private static String safeUrl(WebDriver driver) {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            return "(unavailable)";
        }
    }

    /**
     * Condition-based replacement for a blind Thread.sleep after a
     * navigation/launch: waits for document.readyState == "complete" (and,
     * if jQuery is present on the page, for jQuery.active == 0 too - a
     * standard combined signal that both the DOM and any in-flight AJAX
     * calls have settled). Never throws - a timeout here just means "waited
     * as long as reasonable, moving on", the same as a fixed sleep would,
     * except this returns immediately once the page is actually ready
     * instead of always waiting the full duration.
     */
    static void waitForDocumentReady(WebDriver driver, int timeoutSeconds) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(timeoutSeconds))
                    .until(d -> {
                        Object readyState = ((org.openqa.selenium.JavascriptExecutor) d)
                                .executeScript("return document.readyState");
                        boolean domReady = "complete".equals(readyState);
                        if (!domReady) {
                            return false;
                        }
                        try {
                            Object jQueryActive = ((org.openqa.selenium.JavascriptExecutor) d)
                                    .executeScript("return (typeof jQuery === 'undefined') ? 0 : jQuery.active");
                            return jQueryActive == null || Long.parseLong(jQueryActive.toString()) == 0;
                        } catch (Exception jqEx) {
                            return true; // no jQuery on this page - DOM ready is enough
                        }
                    });
        } catch (org.openqa.selenium.TimeoutException te) {
            log.warn("waitForDocumentReady timed out after " + timeoutSeconds + "s - continuing anyway "
                    + "(best-effort readiness check, not a hard requirement).");
        }
    }
}
