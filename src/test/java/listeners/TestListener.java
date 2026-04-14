package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utilities.DriverFactory;
import utilities.ScreenshotUtil;

/**
 * TestListener hooks into the TestNG lifecycle to build a rich ExtentReports
 * HTML report. Screenshots are automatically captured and embedded on failure.
 *
 * <p>Register this class in testng.xml under {@code <listeners>} or via
 * the {@code @Listeners} annotation on test classes.</p>
 */
public class TestListener implements ITestListener {

    private static ExtentReports extent;

    /** ThreadLocal so each parallel thread maintains its own ExtentTest node. */
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Called once before any test in the suite runs.
     * Initialises ExtentReports with a Spark (HTML) reporter.
     *
     * @param context TestNG suite context
     */
    @Override
    public void onStart(ITestContext context) {
        ExtentSparkReporter sparkReporter =
                new ExtentSparkReporter("./reports/ExtentReport.html");
        sparkReporter.config().setDocumentTitle("OrangeHRM Automation Report");
        sparkReporter.config().setReportName("UI Automation Test Results");
        sparkReporter.config().setTheme(Theme.DARK);
        sparkReporter.config().setEncoding("UTF-8");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Application", "OrangeHRM");
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
    }

    /**
     * Called before each test method. Creates a new ExtentTest node for the test.
     *
     * @param result TestNG result object for the about-to-run test
     */
    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(
                result.getTestClass().getName() + " :: " + result.getMethod().getMethodName(),
                result.getMethod().getDescription()
        );
        extentTest.set(test);
        test.info("Test started: " + result.getMethod().getMethodName());
    }

    /**
     * Called when a test method passes. Logs a pass status to the report.
     *
     * @param result TestNG result object for the passed test
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        extentTest.get().pass("Test passed: " + result.getMethod().getMethodName());
    }

    /**
     * Called when a test method fails. Captures a screenshot, attaches it to
     * the report, and logs the failure cause.
     *
     * @param result TestNG result object for the failed test
     */
    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = extentTest.get();
        test.fail("Test failed: " + result.getThrowable().getMessage());

        try {
            String screenshotPath = ScreenshotUtil.takeScreenshot(
                    DriverFactory.getDriver(),
                    result.getMethod().getMethodName()
            );
            if (screenshotPath != null) {
                test.fail("Screenshot on failure:",
                        MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            }
        } catch (Exception e) {
            test.warning("Could not capture screenshot: " + e.getMessage());
        }
    }

    /**
     * Called when a test method is skipped. Logs a skip status to the report.
     *
     * @param result TestNG result object for the skipped test
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        extentTest.get().skip("Test skipped: " + result.getMethod().getMethodName());
    }

    /**
     * Called once after all tests in the suite have run.
     * Flushes the ExtentReports instance to write the HTML file to disk.
     *
     * @param context TestNG suite context
     */
    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }
}
