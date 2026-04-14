# Interview Preparation Guide — Selenium TestNG Automation Framework

This guide walks through every file and folder in the framework in the order
you would naturally explain it during a technical interview. Each section
covers what the file does, why it exists, and the likely follow-up questions
with strong answers.

---

## How to Walk Through the Framework in an Interview

A good structure to follow when asked *"explain your framework"*:

1. Start with the **tech stack and design pattern**
2. Explain the **project structure**
3. Walk through **execution flow** (pom → testng.xml → BaseTest → Test → Page → Listener)
4. Talk about **cross-cutting concerns** (config, driver management, reporting, data)
5. Mention **CI/CD integration**

---

## 1. pom.xml

### What it is
The Maven build descriptor. It defines all dependencies, the Java version, and
how the test suite is triggered.

### Key points to mention
- Java compiler set to **version 11** via `maven.compiler.source/target`
- **maven-surefire-plugin** is configured to pick up `testng.xml` instead of
  the default test discovery — this is how `mvn test` knows to run TestNG
- All dependency versions are centralised in `<properties>` so upgrading a
  library is a one-line change
- **WebDriverManager** removes the need to manually download or maintain
  browser drivers

### Likely interview questions

**Q: Why use Maven over Gradle?**
Maven has a stricter, more predictable lifecycle which suits CI pipelines well.
Gradle is more flexible but adds complexity. For a Selenium framework, Maven's
convention-over-configuration approach keeps things simple.

**Q: How does `mvn test` know to run TestNG and not JUnit?**
The `maven-surefire-plugin` auto-detects TestNG on the classpath and switches
providers. We also explicitly point it at `testng.xml` via `<suiteXmlFiles>`,
giving us full control over which tests run and in what configuration.

**Q: What is WebDriverManager and why use it?**
WebDriverManager (by Boni Garcia) automatically downloads the correct
chromedriver/geckodriver/edgedriver binary that matches the installed browser
version. Without it, you'd have to manually download drivers and keep them in
sync with browser updates — a maintenance nightmare in CI.

---

## 2. config.properties

### What it is
A flat key-value file that externalises every configurable value from the code.

```properties
base.url=https://opensource-demo.orangehrmlive.com/...
browser=chrome
implicit.wait=10
screenshot.path=./screenshots/
username=Admin
password=admin123
```

### Key points to mention
- **Zero hardcoded values** in test code — everything is read from here
- Switching environments (QA → Staging → Prod) means changing one file,
  not touching test code
- Browser can also be overridden at runtime: `mvn test -Dbrowser=firefox`

### Likely interview questions

**Q: How do you manage different environments (QA, staging, prod)?**
You can maintain separate property files (`config-qa.properties`,
`config-staging.properties`) and load the right one based on a Maven profile
or a system property passed at runtime.

**Q: Why not use a YAML or JSON config instead?**
`.properties` files are natively supported by Java's `Properties` class with
no extra dependency. For a framework of this size it's the simplest choice.
YAML would make sense if the config grew hierarchical.

---

## 3. utilities/ConfigReader.java

### What it is
A utility class that loads `config.properties` once (in a `static` block) and
exposes a single `getProperty(String key)` method.

### Key points to mention
- **Static initialiser block** — the file is read exactly once when the class
  is first loaded, not on every call
- Throws a `RuntimeException` with a clear message if a key is missing —
  fail-fast behaviour prevents silent misconfiguration
- Private constructor enforces the utility-class pattern (no instantiation)

### Likely interview questions

**Q: Why use a static block instead of loading in the method?**
Loading in the method would re-read the file on every `getProperty()` call,
which is wasteful. The static block loads once at class initialisation and
every subsequent call just does a map lookup.

**Q: What happens if config.properties is missing?**
The static block throws a `RuntimeException` immediately when the class is
loaded, which surfaces as a clear error before any test even starts.

---

## 4. utilities/DriverFactory.java

### What it is
The central place where WebDriver instances are created and managed. Uses
`ThreadLocal<WebDriver>` for thread-safe parallel execution.

### Key points to mention
- Supports **Chrome, Firefox, Edge** — browser name is passed as a parameter
- `ThreadLocal` ensures each parallel test thread gets its **own isolated
  driver instance** — no shared state, no race conditions
- `initDriver()` creates and stores the driver; `getDriver()` retrieves it;
  `quitDriver()` closes it and removes it from ThreadLocal
- WebDriverManager handles driver binary setup automatically

### Likely interview questions

**Q: Why ThreadLocal? What problem does it solve?**
When tests run in parallel, multiple threads share the same class. Without
ThreadLocal, all threads would share one `WebDriver` reference and interfere
with each other. ThreadLocal gives each thread its own independent copy of the
driver, making parallel execution safe.

**Q: What happens if you forget to call quitDriver()?**
The browser process stays open, consuming memory. Over a large parallel run
this causes resource exhaustion. That's why `quitDriver()` is called in
`@AfterMethod(alwaysRun = true)` — the `alwaysRun` flag ensures it runs even
if the test throws an exception.

**Q: How would you add headless mode?**
Add a check in `initDriver()` — if a `headless` property is `true` in config,
add `--headless=new` to `ChromeOptions` before creating the driver.

---

## 5. base/BaseTest.java

### What it is
The parent class that all test classes extend. Handles the WebDriver lifecycle
so individual tests don't repeat setup/teardown boilerplate.

### Key points to mention
- `@BeforeMethod` — runs before every `@Test` method: initialises driver,
  maximises window, sets implicit wait, navigates to base URL
- `@AfterMethod(alwaysRun = true)` — quits the driver after every test,
  even on failure
- Reads browser from system property first (`-Dbrowser=firefox`), falls back
  to `config.properties` — supports runtime overrides
- Exposes a `getDriver()` helper so subclasses don't import DriverFactory

### Likely interview questions

**Q: Why `alwaysRun = true` on `@AfterMethod`?**
Without it, if `@BeforeMethod` fails (e.g. the site is down), TestNG skips
`@AfterMethod` and the browser is never closed. `alwaysRun = true` guarantees
cleanup regardless of what happened before.

**Q: What is implicit wait and how is it different from explicit wait?**
Implicit wait tells the driver to poll the DOM for up to N seconds when
finding any element. Explicit wait (`WebDriverWait`) waits for a specific
condition on a specific element. Explicit waits are preferred because they
are more precise and don't mask timing issues the way a blanket implicit wait
can.

**Q: Why does BaseTest not have any `@Test` methods?**
It's a base class, not a test class. Its only job is lifecycle management.
Mixing test logic into it would violate the Single Responsibility Principle.

---

## 6. pages/LoginPage.java

### What it is
The Page Object for the OrangeHRM login screen. Encapsulates all locators and
interactions for that page.

### Key points to mention
- Uses **PageFactory** and `@FindBy` annotations — locators are declared as
  private fields, keeping them hidden from test code
- All locators are **private**; all action/query methods are **public** —
  this is the core POM encapsulation principle
- Uses `WebDriverWait` with `ExpectedConditions` (explicit waits) — no
  `Thread.sleep()` anywhere
- Two separate error-reading methods:
  - `getErrorMessage()` — reads the red alert banner (wrong credentials)
  - `getFieldValidationMessage()` / `areFieldValidationErrorsDisplayed()` —
    reads inline "Required" field errors (empty submission)

### Likely interview questions

**Q: What is the Page Object Model and why use it?**
POM separates the *what* (test logic) from the *how* (UI interactions). Each
page is a class. If a locator changes, you update it in one place — the page
class — not in every test that uses it. It makes tests readable, maintainable,
and reusable.

**Q: What is PageFactory?**
PageFactory is a Selenium utility that initialises `@FindBy`-annotated
WebElement fields lazily — the element is only looked up in the DOM when it's
first accessed, not when the page object is constructed. This avoids stale
element issues on dynamic pages.

**Q: Why are there two different error methods?**
OrangeHRM uses two different UI patterns for errors: a red alert banner for
wrong credentials, and inline per-field labels for empty submissions. They have
different CSS selectors, so they need separate methods. Mixing them into one
would make the locator fragile.

---

## 7. pages/DashboardPage.java

### What it is
The Page Object for the Dashboard — the page the user lands on after a
successful login.

### Key points to mention
- `isUserLoggedIn()` checks visibility of the dashboard title element —
  used as the primary post-login assertion
- `getDashboardTitle()` returns the raw title text — intentionally not
  compared to a hardcoded English string because the demo server may return
  a localised value
- `getLoggedInUsername()` clicks the user dropdown to reveal the name —
  demonstrates handling multi-step interactions in a page method

### Likely interview questions

**Q: Why not assert the exact dashboard title text?**
The OrangeHRM demo server is shared globally and may render in the user's
browser locale. Asserting `"Dashboard"` would fail for Spanish-locale users
who see `"Pizarra de pendientes"`. Asserting non-empty is locale-agnostic and
more robust.

**Q: Should page objects contain assertions?**
Generally no. Page objects should return data; assertions belong in test
classes. This keeps page objects reusable across different tests that may have
different assertion requirements.

---

## 8. listeners/TestListener.java

### What it is
A TestNG `ITestListener` that builds the ExtentReports HTML report and
captures screenshots on failure.

### Key points to mention
- `onStart` — initialises `ExtentReports` with a Spark (HTML) reporter
- `onTestStart` — creates a new test node in the report
- `onTestSuccess` — logs a pass
- `onTestFailure` — captures a screenshot via `ScreenshotUtil`, embeds it
  in the report, logs the failure message
- `onFinish` — **flushes** the report (writes HTML to disk) — forgetting
  this means the report file is empty
- Uses `ThreadLocal<ExtentTest>` so parallel threads each write to their
  own report node without collision

### Likely interview questions

**Q: How do you attach a screenshot to ExtentReports?**
`ScreenshotUtil.takeScreenshot()` saves the PNG and returns the file path.
We pass that path to `MediaEntityBuilder.createScreenCaptureFromPath()` and
attach it to the `ExtentTest` node via `test.fail("...", mediaEntity)`.

**Q: Why ThreadLocal for ExtentTest?**
Same reason as for WebDriver — parallel threads would overwrite each other's
test node reference without ThreadLocal. Each thread needs its own
`ExtentTest` instance to log to the correct report entry.

**Q: What happens if you don't call `extent.flush()`?**
The report HTML file is either empty or not written at all. `flush()` is what
actually serialises the in-memory report data to disk.

---

## 9. utilities/ScreenshotUtil.java

### What it is
A utility that captures the current browser state as a PNG and saves it with
a timestamp-based filename.

### Key points to mention
- Uses Selenium's `TakesScreenshot` interface — cast the driver to it and
  call `getScreenshotAs(OutputType.FILE)`
- Filename format: `testName_yyyyMMdd_HHmmss_SSS.png` — timestamp prevents
  overwrites across runs
- `FileUtils.forceMkdirParent()` auto-creates the screenshots directory if
  it doesn't exist
- Returns the absolute file path so the listener can embed it in the report

### Likely interview questions

**Q: How does Selenium take a screenshot?**
Any `WebDriver` instance that implements `TakesScreenshot` (all major drivers
do) can be cast to it. `getScreenshotAs(OutputType.FILE)` returns a temp
`File` which you then copy to your desired location.

**Q: How do you capture a screenshot only on failure?**
In `TestListener.onTestFailure()` — it's called by TestNG automatically when
a test fails. We call `ScreenshotUtil.takeScreenshot()` there and attach the
result to the report.

---

## 10. utilities/ExcelReader.java

### What it is
A utility that reads `.xlsx` test data files using Apache POI and returns a
2-D `Object[][]` array compatible with TestNG's `@DataProvider`.

### Key points to mention
- Row 0 is treated as a header and skipped
- Uses `DataFormatter` to convert any cell type (numeric, date, string) to
  a consistent String — avoids type-mismatch issues
- `Row.MissingCellPolicy.CREATE_NULL_AS_BLANK` handles sparse rows gracefully
- Returns `Object[][]` which is exactly what `@DataProvider` expects

### Likely interview questions

**Q: Why Apache POI for Excel instead of a CSV?**
Excel is what most business stakeholders use to maintain test data. POI reads
`.xlsx` natively. CSV is simpler but loses formatting, data types, and the
ability to have multiple sheets in one file.

**Q: How does @DataProvider work with ExcelReader?**
The `@DataProvider` method calls `ExcelReader.getTestData()` and returns the
`Object[][]`. TestNG then calls the `@Test` method once per row, passing each
row's values as method parameters.

---

## 11. utilities/TestDataGenerator.java

### What it is
A standalone utility with a `main()` method that programmatically creates
`testdata.xlsx` using Apache POI.

### Key points to mention
- Not part of the test suite — it's a one-time generator
- Useful for onboarding: new team members can regenerate the data file
  without needing Excel installed
- Demonstrates Apache POI write operations (create workbook, sheet, rows,
  cells, styles)

### Likely interview questions

**Q: Why generate the Excel file programmatically?**
It makes the data file reproducible from code, keeps it in version control
as source (the generator), and removes the dependency on having Excel
installed in CI environments.

---

## 12. tests/LoginTest.java

### What it is
The test class containing all login-related test cases.

### Key points to mention
- Extends `BaseTest` — inherits driver setup/teardown automatically
- `@Listeners(TestListener.class)` — wires the ExtentReports listener
- Uses **SoftAssert** — collects all assertion failures in a test and reports
  them together, rather than stopping at the first failure
- Four independent tests:
  - `verifyValidLogin` — happy path, credentials from config
  - `verifyInvalidLogin` — data-driven via Excel `@DataProvider`
  - `verifyLoginPageTitle` — browser title check
  - `verifyEmptyCredentialsError` — inline field validation check
- `@DataProvider` filters out empty-credential rows — those belong to a
  different test with a different assertion strategy

### Likely interview questions

**Q: What is SoftAssert and when do you use it?**
`SoftAssert` collects all assertion failures within a test and reports them
all at once when you call `assertAll()`. Use it when you want to verify
multiple things in one test and see all failures, not just the first one.
Use hard assertions (`Assert`) when a failure means the rest of the test
cannot meaningfully continue.

**Q: How do you make tests independent?**
Each test gets a fresh browser session via `@BeforeMethod`. No test reads
state written by another test. `@DataProvider` supplies its own data. This
means tests can run in any order or in parallel without affecting each other.

**Q: Why separate `verifyInvalidLogin` and `verifyEmptyCredentialsError`?**
They test different UI behaviours. Wrong credentials trigger a red alert
banner. Empty fields trigger inline per-field "Required" labels. They use
different locators and different assertion logic, so they belong in separate
test methods.

---

## 13. testng.xml

### What it is
The TestNG suite configuration file. Controls which tests run, in what order,
and with what parallelism.

```xml
<suite name="OrangeHRM-Automation-Suite"
       parallel="methods"
       thread-count="3">
    <listeners>
        <listener class-name="listeners.TestListener"/>
    </listeners>
    <test name="Login Tests">
        <classes>
            <class name="tests.LoginTest"/>
        </classes>
    </test>
</suite>
```

### Key points to mention
- `parallel="methods"` — each `@Test` method runs in its own thread
- `thread-count="3"` — up to 3 tests run simultaneously
- Listener registered here applies to the entire suite
- Surefire plugin in `pom.xml` points to this file, so `mvn test` uses it

### Likely interview questions

**Q: What are the parallel options in TestNG?**
- `methods` — each test method runs in a separate thread
- `tests` — each `<test>` block runs in a separate thread
- `classes` — each test class runs in a separate thread
- `instances` — each instance of a test class runs in a separate thread

**Q: How do you ensure thread safety with parallel="methods"?**
`DriverFactory` uses `ThreadLocal<WebDriver>` so each thread has its own
driver. `TestListener` uses `ThreadLocal<ExtentTest>` so each thread writes
to its own report node. No static mutable state is shared between threads.

---

## 14. .github/workflows/ci.yml

### What it is
The GitHub Actions CI pipeline that runs the test suite automatically on every
push and pull request to `main`.

### Key points to mention
- Triggers on `push` and `pull_request` to `main`
- Steps: checkout → Java 11 (Temurin) → install Chrome → `mvn test`
- Uploads `screenshots/` as an artifact **only on failure** — keeps storage
  clean on passing runs
- Uploads `reports/` always — so you can always view the ExtentReport

### Likely interview questions

**Q: Why run tests in CI?**
To catch regressions automatically before code is merged. Every PR gets
validated against the real application, so broken changes are caught early
without manual intervention.

**Q: How would you run tests on multiple browsers in CI?**
Use a matrix strategy in GitHub Actions:
```yaml
strategy:
  matrix:
    browser: [chrome, firefox]
steps:
  - run: mvn test -Dbrowser=${{ matrix.browser }}
```
This spins up parallel jobs, one per browser.

---

## 15. Overall Framework — Big Picture Questions

**Q: Explain your framework architecture end to end.**
> "We use a Page Object Model with Selenium WebDriver 4 and TestNG. The entry
> point is `testng.xml`, which Surefire picks up when you run `mvn test`. Each
> test class extends `BaseTest`, which handles driver initialisation and
> teardown via `DriverFactory`. `DriverFactory` uses ThreadLocal so parallel
> tests each get their own browser. Page classes use PageFactory and explicit
> waits — no hardcoded sleeps. `ConfigReader` externalises all config.
> `TestListener` hooks into TestNG to build an ExtentReports HTML report and
> capture screenshots on failure. Test data for data-driven tests comes from
> Excel via Apache POI. The whole suite runs in CI via GitHub Actions."

**Q: How do you handle test failures in parallel execution?**
Each thread has its own driver (ThreadLocal) and its own report node
(ThreadLocal ExtentTest). On failure, `TestListener.onTestFailure()` captures
a screenshot using that thread's driver and attaches it to that thread's
report node. Failures are fully isolated.

**Q: How would you scale this framework?**
- Add more page classes under `pages/` for new screens
- Add more test classes under `tests/`
- Add new sheets to `testdata.xlsx` for new data sets
- Add Maven profiles for environment switching
- Add Allure or keep ExtentReports for richer reporting
- Add retry logic via `IRetryAnalyzer` for flaky tests

**Q: What design patterns does this framework use?**
- **Page Object Model** — encapsulates UI interactions in page classes
- **Factory Pattern** — `DriverFactory` creates and manages driver instances
- **Singleton-like pattern** — `ConfigReader` loads config once via static block
- **Observer Pattern** — `TestListener` implements `ITestListener` (event-driven)
- **ThreadLocal Pattern** — thread-safe driver and report node management
