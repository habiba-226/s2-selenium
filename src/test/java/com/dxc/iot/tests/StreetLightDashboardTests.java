package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.StreetLightDashboardPage;
import com.dxc.iot.utils.ConfigReader;
import com.dxc.iot.utils.ExcelUtils;
import io.qameta.allure.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

@Epic("Sprint 4 & 5 — Street Light Monitoring")
public class StreetLightDashboardTests extends BaseTest {

    // ===== DATA PROVIDERS =====

    @DataProvider(name = "streetLightDateData")
    public Object[][] streetLightDateData() {
        return ExcelUtils.getSheetData(
                "src/test/resources/testdata/street_light_dashboard_data.xlsx",
                "StreetLightDashboard");
    }

    @DataProvider(name = "statusFilterData")
    public Object[][] statusFilterData() {
        return ExcelUtils.getSheetData(
                "src/test/resources/testdata/street_light_dashboard_data.xlsx",
                "StatusFilter");
    }

    @DataProvider(name = "locationFilterData")
    public Object[][] locationFilterData() {
        return ExcelUtils.getSheetData(
                "src/test/resources/testdata/street_light_dashboard_data.xlsx",
                "LocationFilter");
    }

    @DataProvider(name = "sortingData")
    public Object[][] sortingData() {
        return ExcelUtils.getSheetData(
                "src/test/resources/testdata/street_light_dashboard_data.xlsx",
                "Sorting");
    }

    @DataProvider(name = "combinedFiltersData")
    public Object[][] combinedFiltersData() {
        return ExcelUtils.getSheetData(
                "src/test/resources/testdata/street_light_dashboard_data.xlsx",
                "CombinedFilters");
    }

    // ===== SETUP HELPER =====

    private StreetLightDashboardPage loginAndOpenStreetLightDashboard() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.get("base.url"));
        String email = ConfigReader.get("test.email");
        String pass  = ConfigReader.get("test.password");
        loginPage.enterEmail(email != null ? email : "valid@test.com");
        loginPage.enterPassword(pass  != null ? pass  : "Pass@123");
        loginPage.clickSignIn();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/home"));
        StreetLightDashboardPage page = new StreetLightDashboardPage(driver);
        page.open(ConfigReader.get("base.url"));
        return page;
    }

    // =========================================================
    // F#10 — DASHBOARD TABLE
    // =========================================================

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Table renders on page load")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Navigates to /street-light and asserts the data table is visible with at least one row.")
    public void testStreetLightDashboardLoads_SL001() {
        System.out.println("Running: SL-001");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        Assert.assertTrue(p.isStreetLightTableDisplayed(),
                "SL-001: street light table not displayed");
        Assert.assertTrue(p.getStreetLightRowCount() > 0,
                "SL-001: street light table has no rows");
    }

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("All column headers present")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Asserts the table has all required columns. Case-insensitive check.")
    public void testTableHasAllColumnHeaders_SL006() {
        System.out.println("Running: SL-006");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, 0)");
        try { Thread.sleep(500); } catch (Exception ignored) {}
        List<String> rawHeaders = p.getColumnHeaders();
        List<String> headers = rawHeaders.stream().map(String::toLowerCase).toList();
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("location")),
                "SL-006: Location column missing. Found: " + rawHeaders);
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("timestamp") || h.contains("time")),
                "SL-006: Timestamp column missing. Found: " + rawHeaders);
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("brightness")),
                "SL-006: Brightness column missing. Found: " + rawHeaders);
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("power")),
                "SL-006: Power column missing. Found: " + rawHeaders);
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("status")),
                "SL-006: Status column missing. Found: " + rawHeaders);
    }

    // =========================================================
    // CHARTS
    // =========================================================

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Brightness chart visible on page load")
    @Severity(SeverityLevel.CRITICAL)
    public void testBrightnessChartVisible_SLCG001() {
        System.out.println("Running: SL-CG-001 — Brightness Chart Visible");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        Assert.assertTrue(p.isBrightnessChartVisible(),
                "SL-CG-001: Brightness chart title not visible");
        Assert.assertTrue(p.areChartCanvasesRendered(),
                "SL-CG-001: Chart canvas elements not rendered");
    }

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Power Consumption chart visible on page load")
    @Severity(SeverityLevel.CRITICAL)
    public void testPowerChartVisible_SLCG002() {
        System.out.println("Running: SL-CG-002 — Power Chart Visible");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        Assert.assertTrue(p.isPowerChartVisible(),
                "SL-CG-002: Power chart title not visible");
    }

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Charts update when location filter applied")
    @Severity(SeverityLevel.NORMAL)
    public void testChartsUpdateOnFilter_SLCG003() {
        System.out.println("Running: SL-CG-003 — Charts Update With Filter");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        p.selectLocationOption("Industrial Zone B");
        try { Thread.sleep(1500); } catch (Exception ignored) {}

        Assert.assertTrue(p.isBrightnessChartVisible(),
                "SL-CG-003: Brightness chart disappeared after filter");
        Assert.assertTrue(p.isPowerChartVisible(),
                "SL-CG-003: Power chart disappeared after filter");
    }

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Expand chart shows detailed stats")
    @Severity(SeverityLevel.NORMAL)
    public void testExpandChartShowsStats_SLCG004() {
        System.out.println("Running: SL-CG-004 — Expand Chart Shows Stats");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        p.clickExpandBrightnessChart();
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        Assert.assertTrue(p.isExpandedViewVisible(),
                "SL-CG-004: Expanded chart modal did not open");
        Assert.assertTrue(p.isPeakStatVisible(),
                "SL-CG-004: Peak stat not visible in expanded chart view");

        p.closeExpandedChart();
    }

    // =========================================================
    // F#11 — FILTERING & SORTING — PAGINATION
    // =========================================================

    @Test
    @Feature("F#11 — Street Light Filtering")
    @Story("Pagination controls visible")
    @Severity(SeverityLevel.CRITICAL)
    public void testPaginationControlsDisplayed_SL002() {
        System.out.println("Running: SL-002");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        Assert.assertTrue(p.isNextPageButtonDisplayed(),     "SL-002: Next button missing");
        Assert.assertTrue(p.isPreviousPageButtonDisplayed(), "SL-002: Previous button missing");
    }

    @Test
    @Feature("F#11 — Street Light Filtering")
    @Story("Next page navigation")
    @Severity(SeverityLevel.CRITICAL)
    public void testNextPaginationChangesData_SL003() {
        System.out.println("Running: SL-003");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        String page1 = p.getTableSnapshot();
        p.clickNextPage();
        try { Thread.sleep(2000); } catch (Exception ignored) {}
        Assert.assertNotEquals(p.getTableSnapshot(), page1,
                "SL-003: Next page shows identical data to page 1");
    }

    // =========================================================
    // FILTERING - LOCATION (data-driven)
    // =========================================================

    @Test(dataProvider = "locationFilterData")
    @Feature("F#11 — Street Light Filtering")
    @Story("Location filter shows only that location")
    @Severity(SeverityLevel.CRITICAL)
    public void testLocationFilter_DDT(Object[] row) {
        String testCaseId = cell(row, 0);
        String location = cell(row, 1);
        System.out.println("Running: " + testCaseId + " — Location filter: " + location);

        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        p.selectLocationOption(location);
        try { Thread.sleep(1500); } catch (Exception ignored) {}

        Assert.assertTrue(p.areAllDisplayedLocationsMatching(location),
                testCaseId + ": rows displayed do not all match location '" + location + "'");
    }

    // =========================================================
    // FILTERING - STATUS (data-driven)
    // =========================================================

    @Test(dataProvider = "statusFilterData")
    @Feature("F#11 — Street Light Filtering")
    @Story("Status filter shows only that status (ON/OFF)")
    @Severity(SeverityLevel.CRITICAL)
    public void testStatusFilter_DDT(Object[] row) {
        String testCaseId = cell(row, 0);
        String status = cell(row, 1);
        System.out.println("Running: " + testCaseId + " — Status filter: " + status);

        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        p.selectStatusOption(status);
        try { Thread.sleep(1500); } catch (Exception ignored) {}

        Assert.assertTrue(p.areAllDisplayedStatusesMatching(status),
                testCaseId + ": rows displayed do not all match status '" + status + "'");
    }

    // =========================================================
    // FILTERING - DATE (data-driven)
    // =========================================================

    @Test(dataProvider = "streetLightDateData")
    @Feature("F#11 — Street Light Filtering")
    @Story("Date range filter — past returns data, future returns empty")
    @Severity(SeverityLevel.NORMAL)
    public void testDateFilter_DDT(Object[] row) {
        String testCaseId = cell(row, 0);
        String startDate = cell(row, 1);
        String endDate = cell(row, 2);
        String expectedOutcome = cell(row, 3);
        System.out.println("Running: " + testCaseId + " — Date range filter");

        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        p.setStartDate(startDate);
        p.setEndDate(endDate);
        try { Thread.sleep(2000); } catch (Exception ignored) {}

        switch (expectedOutcome.trim().toLowerCase()) {
            case "hasdata":
                Assert.assertTrue(p.getStreetLightRowCount() > 0,
                        testCaseId + ": expected data but table is empty");
                break;
            case "nodata":
                Assert.assertTrue(p.getStreetLightRowCount() == 0 || p.isNoDataMessageDisplayed(),
                        testCaseId + ": expected no data but table has rows");
                break;
            default:
                Assert.fail(testCaseId + ": unknown expected outcome '" + expectedOutcome + "'");
        }
    }

    // =========================================================
    // SORTING (data-driven)
    // =========================================================

    @Test(dataProvider = "sortingData")
    @Feature("F#11 — Street Light Filtering")
    @Story("Sorting by column")
    @Severity(SeverityLevel.NORMAL)
    public void testSorting_DDT(Object[] row) {
        String testCaseId = cell(row, 0);
        String column = cell(row, 1);
        String direction = cell(row, 2);
        System.out.println("Running: " + testCaseId + " — Sort " + column + " " + direction);

        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        switch (column.trim().toLowerCase()) {
            case "brightness":
                p.clickBrightnessHeader();
                try { Thread.sleep(1000); } catch (Exception ignored) {}
                List<Integer> brightnessVals = p.getVisibleBrightnessValues();
                boolean brightnessNeedsReverse = direction.equalsIgnoreCase("asc")
                        ? !StreetLightDashboardPage.isAscending(brightnessVals)
                        : !StreetLightDashboardPage.isDescending(brightnessVals);
                if (brightnessNeedsReverse) {
                    p.clickBrightnessHeader();
                    try { Thread.sleep(1000); } catch (Exception ignored) {}
                    brightnessVals = p.getVisibleBrightnessValues();
                }
                if (direction.equalsIgnoreCase("asc")) {
                    Assert.assertTrue(StreetLightDashboardPage.isAscending(brightnessVals),
                            testCaseId + ": brightness not ascending: " + brightnessVals);
                } else {
                    Assert.assertTrue(StreetLightDashboardPage.isDescending(brightnessVals),
                            testCaseId + ": brightness not descending: " + brightnessVals);
                }
                break;
            case "power":
                p.clickPowerHeader();
                try { Thread.sleep(1000); } catch (Exception ignored) {}
                List<Double> powerVals = p.getVisiblePowerValues();
                boolean powerNeedsReverse = direction.equalsIgnoreCase("asc")
                        ? !StreetLightDashboardPage.isAscending(powerVals)
                        : !StreetLightDashboardPage.isDescending(powerVals);
                if (powerNeedsReverse) {
                    p.clickPowerHeader();
                    try { Thread.sleep(1000); } catch (Exception ignored) {}
                    powerVals = p.getVisiblePowerValues();
                }
                if (direction.equalsIgnoreCase("asc")) {
                    Assert.assertTrue(StreetLightDashboardPage.isAscending(powerVals),
                            testCaseId + ": power not ascending: " + powerVals);
                } else {
                    Assert.assertTrue(StreetLightDashboardPage.isDescending(powerVals),
                            testCaseId + ": power not descending: " + powerVals);
                }
                break;
            default:
                Assert.fail(testCaseId + ": unknown column '" + column + "'");
        }
    }

    // =========================================================
    // COMBINED FILTERS (data-driven)
    // =========================================================

    @Test(dataProvider = "combinedFiltersData")
    @Feature("F#11 — Street Light Filtering")
    @Story("Combined filters (location + status)")
    @Severity(SeverityLevel.NORMAL)
    public void testCombinedFilters_DDT(Object[] row) {
        String testCaseId = cell(row, 0);
        String location = cell(row, 1);
        String status = cell(row, 2);
        System.out.println("Running: " + testCaseId + " — Combined: " + location + " + " + status);

        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        p.selectLocationOption(location);
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        p.selectStatusOption(status);
        try { Thread.sleep(1500); } catch (Exception ignored) {}

        if (p.getStreetLightRowCount() > 0) {
            Assert.assertTrue(p.areAllDisplayedLocationsMatching(location),
                    testCaseId + ": location mismatch after combined filter");
            Assert.assertTrue(p.areAllDisplayedStatusesMatching(status),
                    testCaseId + ": status mismatch after combined filter");
        }
    }

    // =========================================================
    // ADDITIONAL CHART TESTS (mirroring Traffic CG-005 to CG-010)
    // =========================================================

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Charts show empty state when no data")
    @Severity(SeverityLevel.NORMAL)
    public void testChartsEmptyState_SLCG005() {
        System.out.println("Running: SL-CG-005 — Charts Empty State");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        p.setStartDate("2024-04-01T00:00");
        p.setEndDate("2024-04-01T23:59");
        try { Thread.sleep(2000); } catch (Exception ignored) {}

        boolean pageStillLoaded = driver.getCurrentUrl().contains("/street-light");
        Assert.assertTrue(pageStillLoaded,
                "SL-CG-005: Page crashed when charts had no data");
    }

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Chart titles correct")
    @Severity(SeverityLevel.NORMAL)
    public void testChartTitlesCorrect_SLCG006() {
        System.out.println("Running: SL-CG-006 — Chart Titles Correct");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        Assert.assertTrue(p.isBrightnessChartVisible(), "SL-CG-006: Brightness chart title not found");
        Assert.assertTrue(p.isPowerChartVisible(),      "SL-CG-006: Power chart title not found");
    }

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Expanded chart closes on X button click")
    @Severity(SeverityLevel.NORMAL)
    public void testExpandedChartCloses_SLCG007() {
        System.out.println("Running: SL-CG-007 — Expanded Chart Closes");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        p.clickExpandBrightnessChart();
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        Assert.assertTrue(p.isExpandedViewVisible(),
                "SL-CG-007: Expanded chart modal did not open");

        p.closeExpandedChart();
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        Assert.assertTrue(p.isExpandedModalClosed(),
                "SL-CG-007: Expanded modal did not close after X");
    }

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Charts remain visible after pagination")
    @Severity(SeverityLevel.NORMAL)
    public void testChartsVisibleAfterPagination_SLCG008() {
        System.out.println("Running: SL-CG-008 — Charts Visible After Pagination");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        p.clickNextPage();
        try { Thread.sleep(1500); } catch (Exception ignored) {}

        Assert.assertTrue(p.isBrightnessChartVisible(),
                "SL-CG-008: Brightness chart disappeared after pagination");
        Assert.assertTrue(p.isPowerChartVisible(),
                "SL-CG-008: Power chart disappeared after pagination");
    }

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Expand Power chart shows stats")
    @Severity(SeverityLevel.NORMAL)
    public void testExpandPowerChartShowsStats_SLCG009() {
        System.out.println("Running: SL-CG-009 — Expand Power Chart");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        driver.findElements(
                org.openqa.selenium.By.cssSelector(".chart-hint")).get(1).click();
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        Assert.assertTrue(p.isExpandedViewVisible(),
                "SL-CG-009: Power expanded modal did not open");
        Assert.assertTrue(p.isPeakStatVisible(),
                "SL-CG-009: Stat cards not visible in expanded power chart");
    }

    @Test
    @Feature("F#10 — Street Light Dashboard")
    @Story("Expanded Power chart closes on X button")
    @Severity(SeverityLevel.NORMAL)
    public void testExpandedPowerChartCloses_SLCG010() {
        System.out.println("Running: SL-CG-010 — Expanded Power Chart Closes");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        driver.findElements(
                org.openqa.selenium.By.cssSelector(".chart-hint")).get(1).click();
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        Assert.assertTrue(p.isExpandedViewVisible(),
                "SL-CG-010: Power chart modal did not open");

        p.closeExpandedChart();
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        Assert.assertTrue(p.isExpandedModalClosed(),
                "SL-CG-010: Power chart modal did not close after X");
    }

    // =========================================================
    // ADDITIONAL PAGINATION & SORT TESTS (mirroring TD-004, TD-013, TD-014)
    // =========================================================

    @Test
    @Feature("F#11 — Street Light Filtering")
    @Story("Brightness sort indicator changes on click")
    @Severity(SeverityLevel.NORMAL)
    public void testBrightnessSortHeaderChanges_SL004() {
        System.out.println("Running: SL-004");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        String before = driver.findElement(
                org.openqa.selenium.By.xpath("//th[contains(text(),'Brightness')]")).getText();
        p.clickBrightnessHeader();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        String after = driver.findElement(
                org.openqa.selenium.By.xpath("//th[contains(text(),'Brightness')]")).getText();
        Assert.assertNotEquals(after, before, "SL-004: sort indicator did not change");
    }

    @Test
    @Feature("F#11 — Street Light Filtering")
    @Story("Previous page navigation")
    @Severity(SeverityLevel.CRITICAL)
    public void testPreviousPageNavigation_SL013() {
        System.out.println("Running: SL-013");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        String page1 = p.getTableSnapshot();
        p.clickNextPage();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        String page2 = p.getTableSnapshot();

        Assert.assertNotEquals(page1, page2, "SL-013: page 1 and page 2 identical");

        p.clickPreviousPage();
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        String currentPage = p.getCurrentPageNumber();
        Assert.assertEquals(currentPage, "1", "SL-013: Previous did not return to page 1");
    }

    @Test
    @Feature("F#11 — Street Light Filtering")
    @Story("Previous on first page is no-op")
    @Severity(SeverityLevel.NORMAL)
    public void testPreviousPageOnFirstPageIsNoOp_SL014() {
        System.out.println("Running: SL-014");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        String before = p.getTableSnapshot();
        p.clickPreviousPage();
        try { Thread.sleep(1500); } catch (Exception ignored) {}
        Assert.assertEquals(p.getTableSnapshot(), before,
                "SL-014: Previous on first page changed the data");
    }

    @Test
    @Feature("F#11 — Street Light Filtering")
    @Story("Location filter dropdown accessible")
    @Severity(SeverityLevel.NORMAL)
    public void testLocationFilterAccessible_SL005() {
        System.out.println("Running: SL-005");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();
        p.openLocationFilter();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        String text = p.getLocationFilterText();
        Assert.assertNotNull(text, "SL-005: filter text null");
        Assert.assertFalse(text.isEmpty(), "SL-005: filter text empty");
    }

    // =========================================================
    // ADDITIONAL DATE/FILTER TESTS (mirroring TD-021, TD-022, TD-023, TD-024)
    // =========================================================

    @Test
    @Feature("F#11 — Street Light Filtering")
    @Story("Clearing date filter restores all rows")
    @Severity(SeverityLevel.CRITICAL)
    public void testClearDateFilterRestoresRows_SL021() {
        System.out.println("Running: SL-021");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        p.setStartDate("2099-01-01T00:00");
        p.setEndDate("2099-12-31T23:59");
        try { Thread.sleep(2000); } catch (Exception ignored) {}
        Assert.assertTrue(p.isNoDataMessageDisplayed(),
                "SL-021: expected no-data after future filter");

        p.clearDateFilters();
        try { Thread.sleep(2000); } catch (Exception ignored) {}

        Assert.assertTrue(p.getStreetLightRowCount() > 0,
                "SL-021: table did not recover after clearing date filter");
    }

    @Test
    @Feature("F#11 — Street Light Filtering")
    @Story("Pagination resets to page 1 after filter applied")
    @Severity(SeverityLevel.NORMAL)
    public void testPaginationResetsAfterFilter_SL022() {
        System.out.println("Running: SL-022");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        p.clickNextPage();
        try { Thread.sleep(2000); } catch (Exception ignored) {}

        p.selectLocationOption("Main St & 1st Ave");
        try { Thread.sleep(2000); } catch (Exception ignored) {}

        String currentPage = p.getCurrentPageNumber();
        Assert.assertEquals(currentPage, "1",
                "SL-022: pagination did not reset to page 1 after filter applied");
    }

    @Test
    @Feature("F#11 — Street Light Filtering")
    @Story("Same day date range boundary")
    @Severity(SeverityLevel.NORMAL)
    public void testSameDayDateRange_SL023() {
        System.out.println("Running: SL-023");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        String today = java.time.LocalDate.now().toString();
        p.setStartDate(today + "T00:00");
        p.setEndDate(today + "T23:59");
        try { Thread.sleep(2000); } catch (Exception ignored) {}

        boolean hasRows = p.getStreetLightRowCount() > 0;
        boolean noData  = p.isNoDataMessageDisplayed();

        Assert.assertTrue(hasRows || noData,
                "SL-023: Neither rows nor no-data shown for same-day date range");
    }

    @Test
    @Feature("F#11 — Street Light Filtering")
    @Story("Combined filter with guaranteed zero results")
    @Severity(SeverityLevel.NORMAL)
    public void testCombinedFilterZeroResults_SL024() {
        System.out.println("Running: SL-024");
        StreetLightDashboardPage p = loginAndOpenStreetLightDashboard();

        p.setStartDate("2024-04-01T00:00");
        p.setEndDate("2024-04-01T23:59");
        try { Thread.sleep(2000); } catch (Exception ignored) {}

        Assert.assertTrue(p.isNoDataMessageDisplayed(),
                "SL-024: Expected no-data for April 2024 date range");
    }
}
