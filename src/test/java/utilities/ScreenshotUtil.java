package utilities;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScreenshotUtil captures browser screenshots and saves them to the
 * configured screenshots directory with a timestamp-based filename.
 * The returned file path can be embedded directly into ExtentReports.
 */
public class ScreenshotUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    /** Private constructor — utility class, no instantiation. */
    private ScreenshotUtil() {}

    /**
     * Captures a screenshot of the current browser state and saves it to disk.
     *
     * @param driver   the active WebDriver instance
     * @param testName name of the test (used in the filename for easy identification)
     * @return absolute path of the saved screenshot file,
     *         or {@code null} if the capture fails
     */
    public static String takeScreenshot(WebDriver driver, String testName) {
        try {
            String screenshotDir = ConfigReader.getProperty("screenshot.path");
            String timestamp = LocalDateTime.now().format(FORMATTER);
            // Sanitise test name so it is safe as a filename
            String safeName = testName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String fileName = safeName + "_" + timestamp + ".png";

            File destFile = new File(screenshotDir + fileName);
            // Ensure the screenshots directory exists
            FileUtils.forceMkdirParent(destFile);

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, destFile);

            return destFile.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("[ScreenshotUtil] Failed to save screenshot: " + e.getMessage());
            return null;
        }
    }
}
