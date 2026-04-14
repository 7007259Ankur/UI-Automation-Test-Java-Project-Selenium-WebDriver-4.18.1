package utilities;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * DriverFactory manages WebDriver lifecycle using ThreadLocal to ensure
 * thread-safe parallel test execution. Supports Chrome, Firefox, and Edge
 * via WebDriverManager (no manual driver downloads required).
 */
public class DriverFactory {

    /** ThreadLocal ensures each test thread gets its own WebDriver instance. */
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    /** Private constructor — utility class, no instantiation. */
    private DriverFactory() {}

    /**
     * Initialises and stores a WebDriver for the given browser.
     * Call this once per test thread (typically in @BeforeMethod).
     *
     * @param browser browser name: "chrome", "firefox", or "edge" (case-insensitive)
     * @return the newly created WebDriver instance
     * @throws IllegalArgumentException for unsupported browser names
     */
    public static WebDriver initDriver(String browser) {
        WebDriver driver;

        switch (browser.toLowerCase().trim()) {
            case "chrome": {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--disable-notifications");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                driver = new ChromeDriver(options);
                break;
            }
            case "firefox": {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--disable-notifications");
                driver = new FirefoxDriver(options);
                break;
            }
            case "edge": {
                WebDriverManager.edgedriver().setup();
                EdgeOptions options = new EdgeOptions();
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--disable-notifications");
                driver = new EdgeDriver(options);
                break;
            }
            default:
                throw new IllegalArgumentException(
                        "Unsupported browser: '" + browser + "'. Use chrome, firefox, or edge.");
        }

        driverThreadLocal.set(driver);
        return driver;
    }

    /**
     * Returns the WebDriver instance bound to the current thread.
     *
     * @return current thread's WebDriver
     * @throws IllegalStateException if initDriver() has not been called on this thread
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver not initialised for this thread. Call initDriver() first.");
        }
        return driver;
    }

    /**
     * Quits the WebDriver and removes it from ThreadLocal storage.
     * Call this in @AfterMethod to prevent driver leaks.
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }
}
