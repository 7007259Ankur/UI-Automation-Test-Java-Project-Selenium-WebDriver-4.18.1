# Selenium-TestNG-Automation-Framework

![CI](https://github.com/<your-org>/<your-repo>/actions/workflows/ci.yml/badge.svg)

Production-ready UI Automation Framework for [OrangeHRM Demo](https://opensource-demo.orangehrmlive.com) built with Selenium WebDriver 4, TestNG, and ExtentReports 5.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 11+ |
| Maven | 3.8+ |
| Chrome / Firefox / Edge | Latest |

No manual WebDriver downloads needed — WebDriverManager handles that automatically.

---

## Project Structure

```
UI-Automation-Framework/
├── src/test/java/
│   ├── base/           BaseTest.java          — driver setup/teardown
│   ├── pages/          LoginPage, DashboardPage — POM page classes
│   ├── tests/          LoginTest.java         — test cases
│   ├── utilities/      DriverFactory, ConfigReader, ScreenshotUtil, ExcelReader
│   └── listeners/      TestListener.java      — ExtentReports integration
├── src/test/resources/
│   ├── config.properties                      — all configuration
│   └── testdata.xlsx                          — Excel-driven test data
├── .github/workflows/ci.yml                   — GitHub Actions pipeline
├── pom.xml
├── testng.xml
└── README.md
```

---

## Setup

```bash
git clone https://github.com/<your-org>/<your-repo>.git
cd UI-Automation-Framework
mvn clean install -DskipTests
```

---

## Running Tests

Run the full suite (uses browser from config.properties):
```bash
mvn test
```

Override browser at runtime:
```bash
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge
mvn test -Dbrowser=chrome
```

Run a specific test class:
```bash
mvn test -Dtest=LoginTest
```

---

## Switching Browsers

Edit `src/test/resources/config.properties`:
```properties
browser=firefox   # chrome | firefox | edge
```

Or pass it as a Maven property (overrides config.properties):
```bash
mvn test -Dbrowser=edge
```

> Note: `DriverFactory` reads the browser value passed to `initDriver()`.  
> To support the `-Dbrowser` override, `BaseTest` checks `System.getProperty("browser")` first.

---

## Viewing Reports

After a test run, open the HTML report:
```
reports/ExtentReport.html
```

Screenshots captured on failure are saved to:
```
screenshots/<testName>_<timestamp>.png
```

---

## Regenerating Test Data

The Excel file is already committed. To regenerate it:
```bash
# After mvn test-compile
java -cp "target/test-classes;<classpath>" utilities.TestDataGenerator
```

Or add new rows directly to `src/test/resources/testdata.xlsx` in the `InvalidLogin` sheet.  
Columns: `username | password | expectedError`

---

## CI/CD

The GitHub Actions workflow (`.github/workflows/ci.yml`) triggers on every push and pull request to `main`:

1. Checks out the code
2. Sets up Java 11 (Temurin)
3. Installs Chrome
4. Runs `mvn test`
5. Uploads `screenshots/` as an artifact on failure
6. Uploads `reports/` as an artifact always

---

## Configuration Reference

| Property | Description | Default |
|----------|-------------|---------|
| `base.url` | Application URL | OrangeHRM login URL |
| `browser` | Browser to use | `chrome` |
| `implicit.wait` | Implicit wait in seconds | `10` |
| `screenshot.path` | Screenshot output directory | `./screenshots/` |
| `username` | Login username | `Admin` |
| `password` | Login password | `admin123` |
