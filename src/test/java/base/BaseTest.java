package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utilities.ConfigReader;
import utilities.DriverFactory;

import java.time.Duration;

/**
 * BaseTest is the parent class for all test classes.
 * It handles WebDriver initialisation and teardown, ensuring every test
 * starts with a clean browser session and a navigated base URL.
 *
 * <p>Thread-safety is guaranteed by {@link DriverFactory}'s ThreadLocal
 * storage, enabling parallel test execution without driver conflicts.</p>
 */
public class BaseTest {

    /** Explicit wait instance available to all subclasses. */
    protected WebDriverWait wait;

    /**
     * Initialises the WebDriver before each test method.
     * Browser type and implicit wait duration are read from config.properties.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        // Allow -Dbrowser=firefox on the command line to override config.properties
        String browser = System.getProperty("browser") != null
                ? System.getProperty("browser")
                : ConfigReader.getProperty("browser");
        int implicitWait = Integer.parseInt(ConfigReader.getProperty("implicit.wait"));
        String baseUrl = ConfigReader.getProperty("base.url");

        WebDriver driver = DriverFactory.initDriver(browser);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.get(baseUrl);

        wait = new WebDriverWait(driver, Duration.ofSeconds(implicitWait));
    }

    /**
     * Quits the WebDriver after each test method, regardless of pass/fail.
     * Delegates to {@link DriverFactory#quitDriver()} which also clears ThreadLocal.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    /**
     * Convenience accessor so subclasses and page objects can retrieve
     * the current thread's driver without importing DriverFactory directly.
     *
     * @return the WebDriver bound to the current thread
     */
    protected WebDriver getDriver() {
        return DriverFactory.getDriver();
    }
}
