package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Matches the demo framework's utils.PropertyfileUtility pattern: a static
 * Properties block, loaded once, with simple getProperty()/getIntProperty()
 * accessors. Points at the SAME TestData/IBP/TestData.properties file the
 * project already uses (LoginUrl, JiraURL, mailTo, edgeDriverPath, etc.) -
 * deliberately not a new/separate FrameworkConfiguration.properties, so all
 * your existing filled-in credentials and settings keep working unchanged.
 *
 * Loaded lazily (not in a static initializer block like the demo's version)
 * because Maven's test working directory needs to be resolved first, and
 * because Initialize.loadConfig() already owns the "config not loaded yet"
 * error-reporting path other classes expect.
 */
public class PropertyfileUtility {

    private static volatile Properties properties;
    private static final Object lock = new Object();

    private PropertyfileUtility() {
    }

    private static Properties load() {
        if (properties == null) {
            synchronized (lock) {
                if (properties == null) {
                    String configPath = System.getProperty("user.dir") + File.separator + "TestData"
                            + File.separator + "IBP" + File.separator + "TestData.properties";
                    Properties p = new Properties();
                    try (FileInputStream fis = new FileInputStream(configPath)) {
                        p.load(fis);
                    } catch (IOException e) {
                        throw new RuntimeException("Could not load " + configPath, e);
                    }
                    properties = p;
                }
            }
        }
        return properties;
    }

    public static String getProperty(String key) {
        return load().getProperty(key);
    }

    /**
     * Same as getProperty but never throws for a missing/blank key - returns
     * the given default instead. Useful for optional settings (the demo's
     * getIntProperty() throws on anything missing, which is too strict for
     * settings like max.concurrent.threads that most setups never configure).
     */
    public static String getPropertyOrDefault(String key, String defaultValue) {
        String v = load().getProperty(key);
        return (v == null || v.trim().isEmpty()) ? defaultValue : v.trim();
    }

    public static int getIntProperty(String key) {
        String value = load().getProperty(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Property '" + key + "' is not a valid integer.");
        }
    }

    public static int getIntPropertyOrDefault(String key, int defaultValue) {
        String value = load().getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
