package com.dxc.iot.pages;

import com.dxc.iot.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class TrafficDashboardPage extends BasePage {

    public static final String PATH = "/traffic";

    public TrafficDashboardPage(WebDriver driver) {
        super(driver);
    }

    public TrafficDashboardPage open(String baseUrl) {
        driver.get(baseUrl + PATH);
        return this;
    }

    // ===== TABLE =====
    // Confirmed column order from inspected HTML:
    // td[1] = Location
    // td[2] = Timestamp
    // td[3] = Traffic Density  e.g. "306 vehicles/hr"
    // td[4] = Avg Speed        e.g. "113.01 km/h"
    // td[5] = Congestion Level e.g. " High " (class="level-high")

    private final By trafficTable =
            By.cssSelector("table");

    private final By trafficRows =
            By.cssSelector("tbody tr");

    private final By locationColumnCells =
            By.xpath("//tbody/tr/td[1]");

    private final By densityColumnCells =
            By.xpath("//tbody/tr/td[3]");

    private final By avgSpeedColumnCells =
            By.xpath("//tbody/tr/td[4]");

    private final By congestionColumnCells =
            By.xpath("//tbody/tr/td[5]");

    // ===== PAGINATION =====

    private final By nextPageBtn =
            By.xpath("//div[contains(@class,'pagination')]//button[last()]");

    private final By previousPageBtn =
            By.xpath("//div[contains(@class,'pagination')]//button[1]");

    // ===== SORT HEADERS =====

    private final By trafficDensityHeader = By.xpath("//th[contains(text(),'Density')]");


    private final By avgSpeedHeader = By.xpath("//th[contains(text(),'Speed')]");

    private final By timestampHeader =
            By.xpath("//th[contains(text(),'Timestamp') or contains(text(),'Time')]");

    private final By locationHeader =
            By.xpath("//th[contains(text(),'Location')]");

    // ===== FILTERS =====

    private final By locationFilter =
            By.xpath("(//div[contains(@class,'filter-selector')])[1]");

    private final By congestionFilter =
            By.xpath("(//div[contains(@class,'filter-selector')])[2]");

    private final By locationDisplay =
            By.cssSelector(".filter-display");

    private final By startDateInput =
            By.xpath("(//input[@type='datetime-local'])[1]");

    private final By endDateInput =
            By.xpath("(//input[@type='datetime-local'])[2]");

    // ===== MESSAGES =====

    private final By noDataMessage =
            By.xpath("//*[contains(text(),'No traffic data available')]");

    // ===== TOAST =====
    // !! UPDATE this selector after inspecting the actual toast on /traffic !!

    private final By toastNotification =
            By.cssSelector(".toast-item");

    private final By toastMessage =
            By.cssSelector(".toast-message");

    // ===== TABLE ACTIONS =====

    public boolean isTrafficTableDisplayed() {
        return isDisplayed(trafficTable);
    }

    /**
     * Returns list of all visible column header texts.
     * Confirmed headers: Location, Timestamp, Traffic Density, Avg Speed, Congestion Level
     */
    public List<String> getColumnHeaders() {
        return driver.findElements(By.cssSelector("thead th"))
                .stream()
                .map(el -> el.getText().trim())
                .filter(t -> !t.isEmpty())
                .toList();
    }

    /**
     * Clears both date inputs by setting them to empty string
     * and firing input + change events so Angular picks up the reset.
     */
    public void clearDateFilters() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement start = driver.findElement(startDateInput);
        WebElement end   = driver.findElement(endDateInput);
        for (WebElement input : List.of(start, end)) {
            js.executeScript("arguments[0].value = '';", input);
            js.executeScript("arguments[0].dispatchEvent(new Event('input'));", input);
            js.executeScript("arguments[0].dispatchEvent(new Event('change'));", input);
        }
    }

    public int getTrafficRowCount() {
        return driver.findElements(trafficRows).size();
    }

    public int getFirstRowCountSnapshot() {
        return getTrafficRowCount();
    }

    public String getTableSnapshot() {
        StringBuilder sb = new StringBuilder();
        for (WebElement row : driver.findElements(trafficRows)) {
            sb.append(row.getText()).append("\n");
        }
        return sb.toString();
    }

    // ===== PAGINATION ACTIONS =====

    public void clickNextPage() {
        click(nextPageBtn);
    }

    public void clickPreviousPage() {
        try {
            WebElement btn = driver.findElement(previousPageBtn);
            if (btn.isEnabled()) {
                btn.click();
            }
        } catch (Exception ignored) {}
    }

    public boolean isNextPageButtonDisplayed() {
        return driver.findElements(nextPageBtn).size() > 0;
    }

    public boolean isPreviousPageButtonDisplayed() {
        return driver.findElements(previousPageBtn).size() > 0;
    }

    // ===== SORT ACTIONS =====

    public void clickTrafficDensityHeader() {
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();",
                        driver.findElement(trafficDensityHeader));
    }

    public void clickAvgSpeedHeader() {
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();",
                        driver.findElement(avgSpeedHeader));
    }

    public void clickTimestampHeader() {
        click(timestampHeader);
    }

    public void clickLocationHeader() {
        click(locationHeader);
    }

    public String getTrafficDensityHeaderText() {
        return driver.findElement(trafficDensityHeader).getText().trim();
    }

    public String getAvgSpeedHeaderText() {
        return driver.findElement(avgSpeedHeader).getText().trim();
    }

    /**
     * Parses density values from td[3].
     * Cell text: "306 vehicles/hr" — strips unit before parsing.
     */
    public List<Integer> getVisibleDensityValues() {
        return driver.findElements(densityColumnCells).stream()
                .map(el -> {
                    try {
                        String raw = el.getText().trim()
                                .replace("vehicles/hr", "")
                                .replace("vehicles/h", "")
                                .replace("veh/hr", "")
                                .trim();
                        return Integer.parseInt(raw);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(v -> v != null)
                .toList();
    }

    /**
     * Parses avg speed values from td[4].
     * Cell text: "113.01 km/h" — strips unit before parsing.
     */
    public List<Double> getVisibleAvgSpeedValues() {
        return driver.findElements(avgSpeedColumnCells).stream()
                .map(el -> {
                    try {
                        String raw = el.getText().trim()
                                .replace("km/h", "")
                                .trim();
                        return Double.parseDouble(raw);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(v -> v != null)
                .toList();
    }

    /**
     * Returns true if every visible td[1] exactly matches expectedLocation.
     */
    public boolean areAllDisplayedLocationsMatching(String expectedLocation) {
        List<WebElement> cells = driver.findElements(locationColumnCells);
        if (cells.isEmpty()) return false;
        for (WebElement cell : cells) {
            if (!cell.getText().trim().equals(expectedLocation)) return false;
        }
        return true;
    }

    /**
     * Returns true if every visible td[5] matches expectedLevel.
     * Cell has surrounding spaces e.g. " High " — trim() handles this.
     */
    public boolean areAllDisplayedCongestionLevelsMatching(String expectedLevel) {
        List<WebElement> cells = driver.findElements(congestionColumnCells);
        if (cells.isEmpty()) return false;
        for (WebElement cell : cells) {
            if (!cell.getText().trim().equalsIgnoreCase(expectedLevel)) return false;
        }
        return true;
    }

    /** Returns true if list is non-decreasing. */
    public static boolean isAscending(List<? extends Comparable> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (((Comparable) list.get(i)).compareTo(list.get(i + 1)) > 0) return false;
        }
        return true;
    }

    /** Returns true if list is non-increasing. */
    public static boolean isDescending(List<? extends Comparable> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (((Comparable) list.get(i)).compareTo(list.get(i + 1)) < 0) return false;
        }
        return true;
    }

    // ===== LOCATION FILTER ACTIONS =====

    public void openLocationFilter() {
        click(locationFilter);
    }

    public String getLocationFilterText() {
        return driver.findElement(locationDisplay).getText().trim();
    }

    public void selectLocationOption(String location) {
        openLocationFilter();
        try { Thread.sleep(500); } catch (Exception ignored) {}
        WebElement option = driver.findElement(
                By.xpath("//div[contains(@class,'filter-option') and contains(text(),'" + location + "')]"));
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", option);
    }

    // ===== CONGESTION FILTER ACTIONS =====

    public void openCongestionFilter() {
        click(congestionFilter);
    }

    public void selectCongestionOption(String level) {
        openCongestionFilter();
        try { Thread.sleep(500); } catch (Exception ignored) {}
        WebElement option = driver.findElement(
                By.xpath("//div[contains(@class,'filter-option') and contains(text(),'" + level + "')]"));
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", option);
    }

    // ===== DATE FILTER ACTIONS =====

    public void setStartDate(String value) {
        setDateInput(startDateInput, value);
    }

    public void setEndDate(String value) {
        setDateInput(endDateInput, value);
    }

    private void setDateInput(By locator, String value) {
        WebElement input = driver.findElement(locator);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = arguments[1];", input, value);
        js.executeScript("arguments[0].dispatchEvent(new Event('input'));", input);
        js.executeScript("arguments[0].dispatchEvent(new Event('change'));", input);
    }

    public boolean isNoDataMessageDisplayed() {
        return driver.findElements(noDataMessage).size() > 0;
    }

    // ===== TOAST ACTIONS =====

    public boolean waitForToastNotification(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(toastNotification));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isToastVisible() {
        try {
            return driver.findElement(toastNotification).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getToastMessageText() {
        try {
            return driver.findElement(toastMessage).getText().trim();
        } catch (NoSuchElementException e) {
            try {
                return driver.findElement(toastNotification).getText().trim();
            } catch (NoSuchElementException e2) {
                return "";
            }
        }
    }

    public boolean waitForToastToAppearAndDisappear(int appearTimeout, int disappearTimeout) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(appearTimeout))
                    .until(ExpectedConditions.visibilityOfElementLocated(toastNotification));
            new WebDriverWait(driver, Duration.ofSeconds(disappearTimeout))
                    .until(ExpectedConditions.invisibilityOfElementLocated(toastNotification));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Charts
    private final By densityChartTitle = By.xpath(
            "//h3[contains(@class,'chart-title') and contains(.,'Traffic Density')]");
    private final By speedChartTitle = By.xpath(
            "//h3[contains(@class,'chart-title') and contains(.,'Average Speed')]");
    private final By chartCanvases = By.cssSelector("canvas");
    private final By chartHints = By.cssSelector(".chart-hint");
    private final By noDataChartMessage = By.xpath(
            "//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data to display')]");
    private final By peakDensityLabel = By.cssSelector(".stat-label");
    private final By expandedModalCard = By.cssSelector(".modal-card");
    private final By expandedModalCloseBtn = By.cssSelector(".modal-close-btn");


    public boolean isDensityChartVisible() {
        return isDisplayed(densityChartTitle);
    }

    public boolean isSpeedChartVisible() {
        return isDisplayed(speedChartTitle);
    }

    public boolean areChartCanvasesRendered() {
        return driver.findElements(chartCanvases).size() >= 2;
    }

    public void clickExpandDensityChart() {
        driver.findElements(chartHints).get(0).click();
    }

    public boolean isExpandedViewVisible() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .until(d -> driver.findElements(expandedModalCard).size() > 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNoDataChartMessageVisible() {
        return driver.findElements(noDataChartMessage).size() > 0;
    }

    public boolean isPeakDensityStatVisible() {
        return driver.findElements(peakDensityLabel).size() >= 3; // Peak, Average, Min
    }

    public void closeExpandedChart() {
        try {
            driver.findElement(expandedModalCloseBtn).click();
        } catch (Exception e) {
            try {
                driver.findElement(By.cssSelector("body"))
                        .sendKeys(org.openqa.selenium.Keys.ESCAPE);
            } catch (Exception ignored) {}
        }
    }

    private final By densityChartTitleExact = By.xpath(
            "//h3[contains(@class,'chart-title') and contains(.,'Traffic Density Over Time')]");
    private final By speedChartTitleExact = By.xpath(
            "//h3[contains(@class,'chart-title') and contains(.,'Average Speed Distribution')]");

    public boolean isExpandedModalClosed() {
        return driver.findElements(expandedModalCard).isEmpty();
    }
    public String getCurrentPageNumber() {
        return driver.findElement(By.cssSelector(".page-btn.active"))
                .getText().trim();
    }
}