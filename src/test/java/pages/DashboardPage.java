package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * DashboardPage encapsulates interactions and verifications on the
 * OrangeHRM Dashboard — the landing page after a successful login.
 */
public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Locators ────────────────────────────────────────────────────────────

    @FindBy(css = ".oxd-topbar-header-breadcrumb h6")
    private WebElement dashboardTitle;

    @FindBy(css = ".oxd-userdropdown-tab")
    private WebElement userDropdown;

    @FindBy(css = ".oxd-userdropdown-name")
    private WebElement loggedInUsername;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Initialises the page and its WebDriverWait using the provided driver.
     *
     * @param driver active WebDriver instance
     */
    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns the main heading text of the Dashboard page.
     * Note: the demo server may return a localised string (e.g. Spanish),
     * so callers should check {@code isUserLoggedIn()} rather than comparing
     * this value to a hardcoded English string.
     *
     * @return dashboard title text as rendered by the browser
     */
    public String getDashboardTitle() {
        wait.until(ExpectedConditions.visibilityOf(dashboardTitle));
        return dashboardTitle.getText().trim();
    }

    /**
     * Checks whether the user is currently logged in by verifying
     * that the dashboard title element is visible.
     *
     * @return {@code true} if the dashboard title is displayed
     */
    public boolean isUserLoggedIn() {
        try {
            wait.until(ExpectedConditions.visibilityOf(dashboardTitle));
            return dashboardTitle.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the display name of the currently logged-in user as shown
     * in the top-right user dropdown.
     *
     * @return logged-in username string
     */
    public String getLoggedInUsername() {
        wait.until(ExpectedConditions.visibilityOf(userDropdown));
        userDropdown.click();
        wait.until(ExpectedConditions.visibilityOf(loggedInUsername));
        return loggedInUsername.getText().trim();
    }

    /**
     * Returns the browser-level page title.
     *
     * @return document title string
     */
    public String getPageTitle() {
        return driver.getTitle();
    }
}
