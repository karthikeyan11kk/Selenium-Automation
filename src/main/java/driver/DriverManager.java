package driver;

import org.openqa.selenium.WebDriver;
import utils.PropertyfileUtility;

import java.util.concurrent.Semaphore;

/**
 * Matches the demo framework's driver.DriverManager exactly: ThreadLocal
 * WebDriver holder, a semaphore capping concurrent browser sessions, and
 * initializeDriver()/getDriver()/quitDriver(). The semaphore permit count
 * defaults to a generous number if "max.concurrent.threads" isn't set in
 * TestData.properties, since this suite currently runs sequentially with a
 * single shared login (see stepdefinitions.Hooks) rather than in parallel.
 */
public class DriverManager {

    private DriverManager() {
    }

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final int MAX_CONCURRENT_DRIVERS =
            PropertyfileUtility.getIntPropertyOrDefault("max.concurrent.threads", 10);
    private static final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_DRIVERS);

    public static void initializeDriver() throws Exception {
        if (driver.get() == null) {
            WebDriverFactory.getInstance();
            semaphore.acquire();
            WebDriver newDriver = WebDriverFactory.createDriver();
            driver.set(newDriver);
        }
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void setDriver(WebDriver newDriver) {
        driver.set(newDriver);
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            try {
                driver.get().quit();
            } finally {
                driver.remove();
                semaphore.release();
            }
        }
    }
}
