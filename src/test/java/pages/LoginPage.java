package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * LoginPage encapsulates all interactions with the OrangeHRM login screen.
 * Locators are declared as private fields via {@code @FindBy}; all
 * interaction methods are public, keeping implementation details hidden.
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Locators ────────────────────────────────────────────────────────────

    @FindBy(name = "username")
    private WebElement usernameField;

    @FindBy(name = "password")
    private WebElement passwordField;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    /** Alert banner shown for invalid credentials (wrong username/password). */
    @FindBy(css = ".oxd-alert-content-text")
    private WebElement alertErrorMessage;

    /** Inline field-level validation messages (e.g. "Required"). */
    @FindBy(css = ".oxd-input-field-error-message")
    private java.util.List<WebElement> fieldErrorMessages;

    @FindBy(css = ".oxd-topbar-header-breadcrumb h6")
    private WebElement dashboardHeader;

    @FindBy(css = ".orangehrm-login-title")
    private WebElement loginTitle;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Initialises the page and its WebDriverWait using the provided driver.
     *
     * @param driver active WebDriver instance
     */
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    /**
     * Types the given username into the username input field.
     *
     * @param username value to enter
     */
    public void enterUsername(String username) {
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        usernameField.clear();
        usernameField.sendKeys(username);
    }

    /**
     * Types the given password into the password input field.
     *
     * @param password value to enter
     */
    public void enterPassword(String password) {
        wait.until(ExpectedConditions.visibilityOf(passwordField));
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    /**
     * Clicks the Login button and waits for the page to transition.
     */
    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();
    }

    /**
     * Convenience method: enters credentials and submits the login form.
     *
     * @param username login username
     * @param password login password
     */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    // ── Assertions / Queries ─────────────────────────────────────────────────

    /**
     * Checks whether the Dashboard header is visible after login,
     * indicating a successful authentication.
     *
     * @return {@code true} if the dashboard header is displayed
     */
    public boolean isDashboardDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(dashboardHeader));
            return dashboardHeader.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the alert banner error message shown after submitting wrong credentials.
     * This is the red banner (e.g. "Invalid credentials").
     *
     * @return trimmed alert text, or empty string if not visible
     */
    public String getErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOf(alertErrorMessage));
            return alertErrorMessage.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns the first inline field-level validation message (e.g. "Required").
     * These appear below the input fields when the form is submitted empty.
     *
     * @return trimmed validation text, or empty string if not visible
     */
    public String getFieldValidationMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOfAllElements(fieldErrorMessages));
            return fieldErrorMessages.get(0).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns true if any inline field validation messages are displayed.
     *
     * @return true if at least one field error is visible
     */
    public boolean areFieldValidationErrorsDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfAllElements(fieldErrorMessages));
            return !fieldErrorMessages.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the title text displayed on the login page (e.g. "Login").
     *
     * @return login page title text
     */
    public String getLoginTitle() {
        wait.until(ExpectedConditions.visibilityOf(loginTitle));
        return loginTitle.getText().trim();
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
