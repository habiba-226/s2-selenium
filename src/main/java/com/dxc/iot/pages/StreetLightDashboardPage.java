package com.dxc.iot.pages;

import com.dxc.iot.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class StreetLightDashboardPage extends BasePage {

    public static final String PATH = "/street-light";

    public StreetLightDashboardPage(WebDriver driver) {
        super(driver);
    }

    public StreetLightDashboardPage open(String baseUrl) {
        driver.get(baseUrl + PATH);
        return this;
    }

    // ===== TABLE =====
    // Expected column order (verify against actual HTML once available):
    // td[1] = Location
    // td[2] = Timestamp
    // td[3] = Brightness Level (e.g. "85 %")
    // td[4] = Power Consumption (e.g. "120 W")
    // td[5] = Status (ON / OFF)

    private final By streetLightTable =
            By.cssSelector("table");

    private final By streetLightRows =
            By.cssSelector("tbody tr");

    private final By locationColumnCells =
            By.xpath("//tbody/tr/td[1]");

    private final By brightnessColumnCells =
            By.xpath("//tbody/tr/td[3]");

    private final By powerColumnCells =
            By.xpath("//tbody/tr/td[4]");

    private final By statusColumnCells =
            By.xpath("//tbody/tr/td[5]");

    // ===== PAGINATION =====

    private final By nextPageBtn =
            By.xpath("//div[contains(@class,'pagination')]//button[last()]");

    private final By previousPageBtn =
            By.xpath("//div[contains(@class,'pagination')]//button[1]");

    // ===== SORT HEADERS =====

    private final By brightnessHeader =
            By.xpath("//th[contains(text(),'Brightness')]");

    private final By powerHeader =
            By.xpath("//th[contains(text(),'Power')]");

    private final By timestampHeader =
            By.xpath("//th[contains(text(),'Timestamp') or contains(text(),'Time')]");

    private final By locationHeader =
            By.xpath("//th[contains(text(),'Location')]");

    // ===== FILTERS =====

    private final By locationFilter =
            By.xpath("(//div[contains(@class,'filter-selector')])[1]");

    private final By statusFilter =
            By.xpath("(//div[contains(@class,'filter-selector')])[2]");

    private final By locationDisplay =
            By.cssSelector(".filter-display");

    private final By startDateInput =
            By.xpath("(//input[@type='datetime-local'])[1]");

    private final By endDateInput =
            By.xpath("(//input[@type='datetime-local'])[2]");

    // ===== MESSAGES =====

    private final By noDataMessage =
            By.xpath("//*[contains(text(),'No street light data available')]");

    // ===== TOAST =====

    private final By toastNotification =
            By.cssSelector(".toast-item");

    private final By toastMessage =
            By.cssSelector(".toast-message");

    // ===== CHARTS =====

    private final By brightnessChartTitle = By.xpath(
            "//h3[contains(@class,'chart-title') and contains(.,'Brightness')]");
    private final By powerChartTitle = By.xpath(
            "//h3[contains(@class,'chart-title') and contains(.,'Power')]");
    private final By chartCanvases = By.cssSelector("canvas");
    private final By chartHints = By.cssSelector(".chart-hint");
    private final By noDataChartMessage = By.xpath(
            "//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data to display')]");
    private final By peakStatLabel = By.cssSelector(".stat-label");
    private final By expandedModalCard = By.cssSelector(".modal-card");
    private final By expandedModalCloseBtn = By.cssSelector(".modal-close-btn");

    // ===== TABLE ACTIONS =====

    public boolean isStreetLightTableDisplayed() {
        return isDisplayed(streetLightTable);
    }

    public List<String> getColumnHeaders() {
        return driver.findElements(By.cssSelector("thead th"))
                .stream()
                .map(el -> el.getText().trim())
                .filter(t -> !t.isEmpty())
                .toList();
    }

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

    public int getStreetLightRowCount() {
        return driver.findElements(streetLightRows).size();
    }

    public String getTableSnapshot() {
        StringBuilder sb = new StringBuilder();
        for (WebElement row : driver.findElements(streetLightRows)) {
            sb.append(row.getText()).append("\n");
        }
        return sb.toString();
    }

    // ===== PAGINATION =====

    public void clickNextPage() {
        WebElement btn = driver.findElement(nextPageBtn);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);
        try { Thread.sleep(300); } catch (Exception ignored) {}
        btn.click();
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

    public String getCurrentPageNumber() {
        return driver.findElement(By.cssSelector(".page-btn.active"))
                .getText().trim();
    }

    // ===== SORT =====

    public void clickBrightnessHeader() {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();",
                driver.findElement(brightnessHeader));
    }

    public void clickPowerHeader() {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();",
                driver.findElement(powerHeader));
    }

    public void clickTimestampHeader() {
        click(timestampHeader);
    }

    public void clickLocationHeader() {
        click(locationHeader);
    }

    /**
     * Parses brightness values from td[3].
     * Cell text expected: "85 %" — strips unit before parsing.
     */
    public List<Integer> getVisibleBrightnessValues() {
        return driver.findElements(brightnessColumnCells).stream()
                .map(el -> {
                    try {
                        String raw = el.getText().trim()
                                .replace("%", "")
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
     * Parses power values from td[4].
     * Cell text expected: "120 W" or "120 Watts" — strips unit.
     */
    public List<Double> getVisiblePowerValues() {
        return driver.findElements(powerColumnCells).stream()
                .map(el -> {
                    try {
                        String raw = el.getText().trim()
                                .replace("Watts", "")
                                .replace("W", "")
                                .trim();
                        return Double.parseDouble(raw);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(v -> v != null)
                .toList();
    }

    public boolean areAllDisplayedLocationsMatching(String expectedLocation) {
        List<WebElement> cells = driver.findElements(locationColumnCells);
        if (cells.isEmpty()) return false;
        for (WebElement cell : cells) {
            if (!cell.getText().trim().equals(expectedLocation)) return false;
        }
        return true;
    }

    public boolean areAllDisplayedStatusesMatching(String expectedStatus) {
        List<WebElement> cells = driver.findElements(statusColumnCells);
        if (cells.isEmpty()) return false;
        for (WebElement cell : cells) {
            if (!cell.getText().trim().equalsIgnoreCase(expectedStatus)) return false;
        }
        return true;
    }

    public static boolean isAscending(List<? extends Comparable> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (((Comparable) list.get(i)).compareTo(list.get(i + 1)) > 0) return false;
        }
        return true;
    }

    public static boolean isDescending(List<? extends Comparable> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (((Comparable) list.get(i)).compareTo(list.get(i + 1)) < 0) return false;
        }
        return true;
    }

    // ===== LOCATION FILTER =====

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
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
    }

    // ===== STATUS FILTER =====

    public void openStatusFilter() {
        click(statusFilter);
    }

    public void selectStatusOption(String status) {
        openStatusFilter();
        try { Thread.sleep(500); } catch (Exception ignored) {}
        WebElement option = driver.findElement(
                By.xpath("//div[contains(@class,'filter-option') and contains(text(),'" + status + "')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
    }

    // ===== DATE FILTER =====

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

    // ===== TOAST =====

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

    // ===== CHARTS =====

    public boolean isBrightnessChartVisible() {
        return isDisplayed(brightnessChartTitle);
    }

    public boolean isPowerChartVisible() {
        return isDisplayed(powerChartTitle);
    }

    public boolean areChartCanvasesRendered() {
        return driver.findElements(chartCanvases).size() >= 2;
    }

    public void clickExpandBrightnessChart() {
        driver.findElements(chartHints).get(0).click();
    }

    public boolean isExpandedViewVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> driver.findElements(expandedModalCard).size() > 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNoDataChartMessageVisible() {
        return driver.findElements(noDataChartMessage).size() > 0;
    }

    public boolean isPeakStatVisible() {
        return driver.findElements(peakStatLabel).size() >= 3;
    }

    public void closeExpandedChart() {
        try {
            driver.findElement(expandedModalCloseBtn).click();
        } catch (Exception e) {
            try {
                driver.findElement(By.cssSelector("body"))
                        .sendKeys(Keys.ESCAPE);
            } catch (Exception ignored) {}
        }
    }

    public boolean isExpandedModalClosed() {
        return driver.findElements(expandedModalCard).isEmpty();
    }
}
