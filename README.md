# IoT Smart Monitoring System — QA Automation Suite

Selenium WebDriver end-to-end automation suite for the **Smart IoT Monitoring System** frontend.
Built with **Java**, **TestNG**, **Apache POI**, and **Allure Reports** following the **Page Object Model (POM)** design pattern with full **data-driven** testing via Excel.

---

## Table of Contents

- [Overview](#overview)
- [Sprint Coverage](#sprint-coverage)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [Generating the Allure Report](#generating-the-allure-report)
- [Troubleshooting](#troubleshooting)
- [Extending the Framework](#extending-the-framework)

---

## Overview

This suite exercises the full frontend of the Smart IoT Monitoring System end-to-end using a real Chrome browser. Tests are grouped by sprint and by feature, and can be run individually, as a full suite, as regression, or as sanity smoke tests.

**Highlights**
- **Page Object Model (POM):** every page = 1 Java class with private locators + public actions.
- **Data-driven testing:** test data lives in Excel sheets under `src/test/resources/testdata/`. Add rows to add scenarios — no code changes required.
- **Suite XML organization:** the same test methods are grouped into different `.xml` files (full / regression / sanity) with no duplicate code.
- **Automatic failure evidence:** screenshots are auto-captured to `screenshots/` on any test failure.
- **Allure reporting:** rich HTML reports with pie charts, timings, per-suite breakdowns, and attached screenshots.

---

## Sprint Coverage

### Test Suites at a Glance

| Suite XML | Purpose | Approx. Tests | When to Run |
|-----------|---------|---------------|-------------|
| `sanity.xml` | Fast critical-path smoke test | ~9 | Before deep testing / after every deploy |
| `regression.xml` | Verify Sprints 1-3 still work | ~120 | Before sprint end |
| `testng.xml` | Full suite (regression + Sprint 3 + Sprint 4 & 5) | ~230+ | Final validation / demo |

### Feature Coverage

| Feature | Suite | Tests | Sprint |
|---------|-------|-------|--------|
| F#1–F#5 | SignUp / Login / Profile / Settings / Home / Session | ~70 | Sprints 1 & 2 |
| F#6 Entry Point | EntryPointTests | 7 | Sprint 3 |
| F#7 Traffic Dashboard | TrafficDashboardTests | 13 | Sprint 3 |
| F#8 Filtering & Sorting | TrafficDashboardTests (DDT) | 26 | Sprint 3 |
| F#9 Traffic Notifications | NotificationTests | 17 | Sprint 3 |
| End-to-End Flows | EndToEndTests | — | Sprint 3 |
| **F#10 Street Light Dashboard** | **StreetLightDashboardTests** | **12** | **Sprint 4 & 5** |
| **F#11 Street Light Filtering & Sorting** | **StreetLightDashboardTests (DDT)** | **~34** | **Sprint 4 & 5** |
| **F#12 Street Light Notifications** | **StreetLightNotificationTests** | **4** | **Sprint 4 & 5** |
| **F#13 Air Pollution Dashboard** | **AirPollutionDashboardTests** | **12** | **Sprint 4 & 5** |
| **F#14 Air Pollution Filtering & Sorting** | **AirPollutionDashboardTests (DDT)** | **~35** | **Sprint 4 & 5** |
| **F#15 Air Pollution Notifications** | **AirPollutionNotificationTests** | **4** | **Sprint 4 & 5** |

---

## Architecture

```
s2-selenium/
├── pom.xml                              # Maven build & dependencies
├── testng.xml                           # Full suite
├── regression.xml                       # Regression subset (Sprints 1-3)
├── sanity.xml                           # Critical happy-paths only
├── config.properties                    # base URL, test credentials
│
├── src/main/java/com/dxc/iot/
│   ├── base/
│   │   ├── BasePage.java                # Shared page-level helpers (click, type, wait)
│   │   ├── BaseTest.java                # WebDriver setup / teardown / screenshot on fail
│   │   └── NotificationTestsBase.java   # Seeds guaranteed-breach thresholds once per suite (@BeforeSuite)
│   ├── pages/                           # Page Object Model classes
│   │   ├── LoginPage.java
│   │   ├── SignUpPage.java
│   │   ├── HomePage.java
│   │   ├── ProfilePage.java
│   │   ├── SettingsPage.java
│   │   ├── EntryPointPage.java
│   │   ├── TrafficDashboardPage.java
│   │   ├── StreetLightDashboardPage.java     # NEW Sprint 4 & 5
│   │   ├── AirPollutionDashboardPage.java    # NEW Sprint 4 & 5
│   │   └── NotificationPage.java
│   └── utils/
│       ├── ConfigReader.java            # reads config.properties
│       ├── ExcelUtils.java              # reads Excel test data
│       └── ScreenshotUtils.java         # captures screenshot on failure
│
├── src/test/java/com/dxc/iot/tests/
│   ├── LoginTests.java
│   ├── SignUpTests.java
│   ├── ProfileTests.java
│   ├── SettingsTests.java
│   ├── HomeTests.java
│   ├── SessionTests.java
│   ├── EntryPointTests.java
│   ├── TrafficDashboardTests.java
│   ├── NotificationTests.java
│   ├── EndToEndTests.java
│   ├── StreetLightDashboardTests.java         # NEW Sprint 4 & 5
│   ├── StreetLightNotificationTests.java      # NEW Sprint 4 & 5
│   ├── AirPollutionDashboardTests.java        # NEW Sprint 4 & 5
│   └── AirPollutionNotificationTests.java     # NEW Sprint 4 & 5
│
├── src/test/resources/testdata/         # Excel data-driven test data
│   ├── SignInData.xlsx
│   ├── SignUpData.xlsx
│   ├── ProfileData.xlsx
│   ├── SettingsData.xlsx
│   ├── HomeData.xlsx
│   ├── entry_point_data.xlsx
│   ├── notification_data.xlsx
│   ├── traffic_dashboard_data.xlsx
│   ├── street_light_dashboard_data.xlsx        # NEW Sprint 4 & 5
│   └── air_pollution_dashboard_data.xlsx       # NEW Sprint 4 & 5
│
├── allure-results/                      # generated per run — feeds Allure
└── screenshots/                         # auto-captured on failed tests
```

### Page Object Model in a Nutshell

- **BasePage** — provides `click()`, `type()`, `waitVisible()`, `isDisplayed()`. Every Page Object extends it.
- **Page Objects** — declare **private locators** (fixed elements like buttons, tables) and expose **public actions** (`clickNextPage()`, `selectLocationOption(String)`). Dynamic locators (options in a dropdown) are built inside methods using the input value.
- **Test classes** — never touch selectors. They call Page Object methods and assert on the returned data.

### Data-Driven Testing

Data-driven tests are marked with `@DataProvider` and read rows from Excel:

```java
@DataProvider(name = "locationFilterData")
public Object[][] locationFilterData() {
    return ExcelUtils.getSheetData(
        "src/test/resources/testdata/street_light_dashboard_data.xlsx",
        "LocationFilter");
}

@Test(dataProvider = "locationFilterData")
public void testLocationFilter_DDT(Object[] row) {
    String location = cell(row, 1);
    // ... test runs once per row in Excel
}
```

Add a row in Excel → get a new scenario for free.

---

## Prerequisites

- **Java 23** (JDK)
- **Maven 3.9+**
- **Chrome browser** (latest — Selenium Manager auto-downloads the matching ChromeDriver)
- **IntelliJ IDEA** (recommended — has TestNG integration built in)
- **Allure Commandline 2.29+** (for viewing HTML reports)

### Application stack under test
- **Angular frontend** running on `http://localhost:4200` (dev server) OR `http://localhost` (Docker)
- **Spring Boot backend** running on `http://localhost:8080`
- **MySQL 8** with database `iot_db`

---

## Environment Setup

### 1. Clone this repo
```bash
git clone https://github.com/habiba-226/s2-selenium.git
cd s2-selenium
```

### 2. Install Allure (optional but strongly recommended)
**WSL / Ubuntu:**
```bash
sudo apt install -y default-jre
curl -L -o allure.tgz https://github.com/allure-framework/allure2/releases/download/2.29.0/allure-2.29.0.tgz
sudo tar -zxvf allure.tgz -C /opt/
sudo ln -s /opt/allure-2.29.0/bin/allure /usr/bin/allure
allure --version
```

**Windows (Scoop):**
```powershell
scoop install allure
```

### 3. Start the app stack
Any of these work, as long as the frontend is reachable at the URL set in `config.properties`.

**Option A — Docker (fastest, uses the deployed image):**
```bash
cd /path/to/iot_devops
docker compose up -d
```
Then set `base.url=http://localhost` in `config.properties`.

**Option B — Local dev server (needed when testing the newest features before a Docker release):**
```bash
# Backend: open DXC_Juniors in IntelliJ, run SmartMonitoringApplication
# Frontend:
cd /path/to/DXC_Front
npm install
npm start
```
Then set `base.url=http://localhost:4200` in `config.properties`.

### 4. Set up the test user
The suite uses a single dedicated test account: **`valid@test.com` / `Pass123#`**.
Sign up at the frontend once to create it. First name / last name should match `config.properties`.

### 5. Notification thresholds (automatic — no action needed)
Notification tests need low thresholds in Settings so every sensor reading breaches them.
This is now seeded automatically: `NotificationTestsBase` (extended by `NotificationTests`,
`StreetLightNotificationTests`, and `AirPollutionNotificationTests`) sets these once per
suite run via a `@BeforeSuite` hook, so the notification tests are hermetic and don't
depend on `SettingsTests` running first or on this being done by hand:
- Traffic Density → **1** (vehicles/hr)
- Brightness Level → **99** (%)
- Power Consumption → **1** (W)
- CO Level → **1** (ppm)
- Ozone → **0.001** (ppm)

---

## Configuration

`config.properties` (project root):
```properties
implicit.wait=10
base.url=http://localhost:4200
test.email=valid@test.com
test.password=Pass123#
test.firstName=John
test.lastName=Doe
```

`test.firstName` and `test.lastName` **must match the values you signed up with** — some tests (e.g., Home welcome-title check) compare against these.

---

## Running Tests

Always run through **IntelliJ** (recommended) or **Maven** — never call individual test classes without a TestNG XML.

### Full suite (~30–45 min)
Right-click `testng.xml` → **Run 'testng.xml'**

### Regression only (Sprints 1-3) (~15–20 min)
Right-click `regression.xml` → **Run 'regression.xml'**

### Sanity smoke test (~5 min)
Right-click `sanity.xml` → **Run 'sanity.xml'**

### A single test class
Right-click the `.java` file (e.g., `StreetLightDashboardTests.java`) → **Run**

### A single test method
Click the green ▶ next to the method name.

### Via Maven CLI
```bash
mvn test                              # runs testng.xml by default
mvn test -DsuiteXmlFile=sanity.xml    # runs sanity only
```

---

## Generating the Allure Report

Test runs produce raw JSON in `allure-results/`. To view a human-readable report:

### Serve a temporary interactive report
```bash
allure serve -h 0.0.0.0 allure-results
```
Opens in your browser at the URL it prints.

### Generate a persistent static report
```bash
allure generate allure-results --clean -o allure-report
allure open allure-report
```
The `allure-report` folder is a fully static HTML site — zip and share:
```bash
zip -r allure-report.zip allure-report
```

> **Note:** `allure-results` accumulates results across runs by default. For a clean report reflecting only the latest run, delete the folder first:
> ```bash
> rm -rf allure-results
> ```
> Then run the suite, then generate the report.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| All tests fail waiting for `/home` redirect | Test account got locked or password drifted — recreate the user (see [Test-account reset](#test-account-reset)) |
| Login shows "Invalid email or password" for the correct password | ProfileTests changed the password and never reverted — recreate the user |
| Chrome doesn't launch | Update Chrome to the latest stable, or clear the Selenium Manager cache: `rm -rf ~/.cache/selenium` |
| Tests fail with `Aspect weaver cannot determine any valid method` | AspectJ version too old for your Java. Ensure `pom.xml` uses `aspectjweaver 1.9.24+` |
| Pagination click times out (`element not clickable`) | The button was below the viewport. Use `scrollIntoView` in the Page Object before clicking — already done for known cases |
| Notification tests skip with "no notifications found" | `NotificationTestsBase`'s `@BeforeSuite` threshold seeding failed or didn't run — check it fired (see [Environment Setup](#environment-setup) step 5) and that the Settings save succeeded |
| Test data locations don't match your DB | Update Excel rows in `src/test/resources/testdata/` |

### Test-account reset
```bash
docker exec -it iot-db mysql -uroot -p
```
Enter root password, then:
```sql
USE iot_db;
DELETE FROM users WHERE email='valid@test.com';
EXIT;
```
Then re-sign up on the frontend with the credentials in `config.properties`.

---

## Extending the Framework

Adding coverage for a new feature is a repeatable pattern:

1. **Create a Page Object** in `src/main/java/com/dxc/iot/pages/` with private locators + public actions.
2. **Create a Test class** in `src/test/java/com/dxc/iot/tests/` that uses that Page Object.
3. **Add Excel test data** in `src/test/resources/testdata/` if the tests are data-driven.
4. **Register the class** in `testng.xml` (and `regression.xml` when the feature is no longer under active development).

The Sprint 4 & 5 classes (`StreetLightDashboardTests`, `AirPollutionDashboardTests`, etc.) all follow this pattern — copy one as a template.

---

## Bugs Found — Sprint 4 & 5

Documented in the accompanying **Sprint 4_5_QA_Report.xlsx** (Bug Report tab). Summary:

| Bug ID | Area | Description | Status |
|--------|------|-------------|--------|
| BUG-001 | Street Light Pagination | Next button did not change table data | Fixed |
| BUG-002 | Street Light Pagination | Previous button did not return to page 1 | Fixed |
| BUG-003 | Street Light Filtering | Pagination did not reset to page 1 after filter | Fixed |
| BUG-004 | Air Pollution Pagination | Next button did not change table data | Fixed |
| BUG-005 | Air Pollution Pagination | Previous button did not return to page 1 | Fixed |
| BUG-006 | Air Pollution Filtering | Pagination did not reset to page 1 after filter | Fixed |
| BUG-007 | Login / Regression | Account was not locked after 3 failed login attempts | Fixed |
| BUG-008 | Profile / Regression | Success/error banner rendered `[object Object]` | Fixed |

---