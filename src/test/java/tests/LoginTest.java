package tests;

import base.BaseTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.DashboardPage;
import pages.LoginPage;
import listeners.TestListener;
import utilities.ConfigReader;
import utilities.ExcelReader;

/**
 * LoginTest covers all login-related scenarios for the OrangeHRM application.
 *
 * <ul>
 *   <li>Valid login with credentials from config.properties</li>
 *   <li>Invalid login with data-driven credentials from Excel (wrong username/password)</li>
 *   <li>Login page title verification</li>
 *   <li>Empty credentials field-validation error verification</li>
 * </ul>
 *
 * <p>All tests are independent — no shared state between methods.</p>
 */
@Listeners(TestListener.class)
public class LoginTest extends BaseTest {

    // ── Data Providers ────────────────────────────────────────────────────────

    /**
     * Supplies invalid credential pairs (wrong username/password combos) from Excel.
     * Sheet "InvalidLogin" columns: username | password | expectedError
     * Rows with empty username or password are excluded here — they trigger
     * field-level validation, not the alert banner, and are covered by
     * {@link #verifyEmptyCredentialsError()}.
     *
     * @return 2-D array of [username, password, expectedError]
     */
    @DataProvider(name = "invalidLoginData", parallel = false)
    public Object[][] invalidLoginData() {
        String filePath = "src/test/resources/testdata.xlsx";
        Object[][] allRows = ExcelReader.getTestData(filePath, "InvalidLogin");

        // Keep only rows where both username and password are non-empty
        // (empty-field rows belong to verifyEmptyCredentialsError)
        java.util.List<Object[]> filtered = new java.util.ArrayList<>();
        for (Object[] row : allRows) {
            if (row.length >= 2
                    && row[0] != null && !row[0].toString().trim().isEmpty()
                    && row[1] != null && !row[1].toString().trim().isEmpty()) {
                filtered.add(row);
            }
        }
        return filtered.toArray(new Object[0][]);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    /**
     * Verifies that a user can log in successfully with valid credentials
     * read from config.properties, and that the Dashboard is displayed.
     * The dashboard title is locale-agnostic — we only assert it is non-empty
     * because the demo server may return a localised string.
     */
    @Test(description = "Verify successful login with valid credentials",
          priority = 1)
    public void verifyValidLogin() {
        String username = ConfigReader.getProperty("username");
        String password = ConfigReader.getProperty("password");

        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login(username, password);

        DashboardPage dashboardPage = new DashboardPage(getDriver());

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(dashboardPage.isUserLoggedIn(),
                "Dashboard should be visible after valid login");
        // Title is locale-dependent on the demo server — assert it is non-empty
        String title = dashboardPage.getDashboardTitle();
        softAssert.assertFalse(title.isEmpty(),
                "Dashboard title should not be empty after login");
        softAssert.assertAll();
    }

    /**
     * Verifies that logging in with wrong (but non-empty) credentials shows
     * the red alert banner. Test data is supplied by {@code invalidLoginData}
     * (Excel-driven, empty-field rows excluded).
     *
     * @param username        invalid username from Excel
     * @param password        invalid password from Excel
     * @param expectedError   expected substring of the alert banner text
     */
    @Test(description = "Verify alert banner for wrong credentials",
          dataProvider = "invalidLoginData",
          priority = 2)
    public void verifyInvalidLogin(String username, String password, String expectedError) {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login(username, password);

        SoftAssert softAssert = new SoftAssert();
        String actualError = loginPage.getErrorMessage();
        softAssert.assertFalse(actualError.isEmpty(),
                "Alert banner should be displayed for wrong credentials");
        softAssert.assertTrue(actualError.contains(expectedError),
                "Alert should contain: '" + expectedError + "' but was: '" + actualError + "'");
        softAssert.assertAll();
    }

    /**
     * Verifies that the browser page title on the login page contains "OrangeHRM".
     */
    @Test(description = "Verify the login page browser title",
          priority = 3)
    public void verifyLoginPageTitle() {
        LoginPage loginPage = new LoginPage(getDriver());

        SoftAssert softAssert = new SoftAssert();
        String pageTitle = loginPage.getPageTitle();
        softAssert.assertFalse(pageTitle.isEmpty(), "Page title should not be empty");
        softAssert.assertTrue(pageTitle.contains("OrangeHRM"),
                "Page title should contain 'OrangeHRM' but was: '" + pageTitle + "'");
        softAssert.assertAll();
    }

    /**
     * Verifies that submitting the login form with empty fields shows
     * inline field-level "Required" validation messages (not the alert banner).
     * OrangeHRM renders these as per-field error labels, not the red alert.
     */
    @Test(description = "Verify inline Required errors when submitting empty credentials",
          priority = 4)
    public void verifyEmptyCredentialsError() {
        LoginPage loginPage = new LoginPage(getDriver());
        // Submit without entering any credentials
        loginPage.clickLogin();

        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(loginPage.areFieldValidationErrorsDisplayed(),
                "Inline 'Required' field errors should appear when credentials are empty");
        String firstError = loginPage.getFieldValidationMessage();
        softAssert.assertFalse(firstError.isEmpty(),
                "Field validation message text should not be empty");
        softAssert.assertAll();
    }
}
