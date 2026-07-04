package com.dxc.iot.pages;

import com.dxc.iot.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AirPollutionDashboardPage extends BasePage {

    public static final String PATH = "/air-pollution";

    public AirPollutionDashboardPage(WebDriver driver) {
        super(driver);
    }

    public AirPollutionDashboardPage open(String baseUrl) {
        driver.get(baseUrl + PATH);
        return this;
    }

    // ===== TABLE =====
    // Expected column order (verify against actual HTML once available):
    // td[1] = Location
    // td[2] = Timestamp
    // td[3] = CO Level (e.g. "2.5 ppm")
    // td[4] = Ozone Concentration (e.g. "0.08 ppm")
    // td[5] = Pollution Level (Low / Moderate / High / Hazardous)

    private final By airPollutionTable =
            By.cssSelector("table");

    private final By airPollutionRows =
            By.cssSelector("tbody tr");

    private final By locationColumnCells =
            By.xpath("//tbody/tr/td[1]");

    private final By coColumnCells =
            By.xpath("//tbody/tr/td[3]");

    private final By ozoneColumnCells =
            By.xpath("//tbody/tr/td[4]");

    private final By pollutionLevelColumnCells =
            By.xpath("//tbody/tr/td[5]");

    // ===== PAGINATION =====

    private final By nextPageBtn =
            By.xpath("//div[contains(@class,'pagination')]//button[last()]");

    private final By previousPageBtn =
            By.xpath("//div[contains(@class,'pagination')]//button[1]");

    // ===== SORT HEADERS =====

    private final By coHeader =
            By.xpath("//th[contains(text(),'CO')]");

    private final By ozoneHeader =
            By.xpath("//th[contains(text(),'Ozone')]");

    private final By timestampHeader =
            By.xpath("//th[contains(text(),'Timestamp') or contains(text(),'Time')]");

    private final By locationHeader =
            By.xpath("//th[contains(text(),'Location')]");

    // ===== FILTERS =====

    private final By locationFilter =
            By.xpath("(//div[contains(@class,'filter-selector')])[1]");

    private final By pollutionLevelFilter =
            By.xpath("(//div[contains(@class,'filter-selector')])[2]");

    private final By locationDisplay =
            By.cssSelector(".filter-display");

    private final By startDateInput =
            By.xpath("(//input[@type='datetime-local'])[1]");

    private final By endDateInput =
            By.xpath("(//input[@type='datetime-local'])[2]");

    // ===== MESSAGES =====

    private final By noDataMessage =
            By.xpath("//*[contains(text(),'No air pollution data available')]");

    // ===== TOAST =====

    private final By toastNotification =
            By.cssSelector(".toast-item");

    private final By toastMessage =
            By.cssSelector(".toast-message");

    // ===== CHARTS =====

    private final By coChartTitle = By.xpath(
            "//h3[contains(@class,'chart-title') and contains(.,'CO')]");
    private final By ozoneChartTitle = By.xpath(
            "//h3[contains(@class,'chart-title') and contains(.,'Ozone')]");
    private final By chartCanvases = By.cssSelector("canvas");
    private final By chartHints = By.cssSelector(".chart-hint");
    private final By noDataChartMessage = By.xpath(
            "//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data to display')]");
    private final By peakStatLabel = By.cssSelector(".stat-label");
    private final By expandedModalCard = By.cssSelector(".modal-card");
    private final By expandedModalCloseBtn = By.cssSelector(".modal-close-btn");

    // ===== TABLE ACTIONS =====

    public boolean isAirPollutionTableDisplayed() {
        return isDisplayed(airPollutionTable);
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

    public int getAirPollutionRowCount() {
        return driver.findElements(airPollutionRows).size();
    }

    public String getTableSnapshot() {
        StringBuilder sb = new StringBuilder();
        for (WebElement row : driver.findElements(airPollutionRows)) {
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

    public void clickCoHeader() {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();",
                driver.findElement(coHeader));
    }

    public void clickOzoneHeader() {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();",
                driver.findElement(ozoneHeader));
    }

    public void clickTimestampHeader() {
        click(timestampHeader);
    }

    public void clickLocationHeader() {
        click(locationHeader);
    }

    /**
     * Parses CO values from td[3].
     * Cell text expected: "2.5 ppm" — strips unit before parsing.
     */
    public List<Double> getVisibleCoValues() {
        return driver.findElements(coColumnCells).stream()
                .map(el -> {
                    try {
                        String raw = el.getText().trim()
                                .replace("ppm", "")
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
     * Parses ozone values from td[4].
     * Cell text expected: "0.08 ppm" — strips unit.
     */
    public List<Double> getVisibleOzoneValues() {
        return driver.findElements(ozoneColumnCells).stream()
                .map(el -> {
                    try {
                        String raw = el.getText().trim()
                                .replace("ppm", "")
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

    public boolean areAllDisplayedPollutionLevelsMatching(String expectedLevel) {
        List<WebElement> cells = driver.findElements(pollutionLevelColumnCells);
        if (cells.isEmpty()) return false;
        for (WebElement cell : cells) {
            if (!cell.getText().trim().equalsIgnoreCase(expectedLevel)) return false;
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

    // ===== POLLUTION LEVEL FILTER =====

    public void openPollutionLevelFilter() {
        click(pollutionLevelFilter);
    }

    public void selectPollutionLevelOption(String level) {
        openPollutionLevelFilter();
        try { Thread.sleep(500); } catch (Exception ignored) {}
        WebElement option = driver.findElement(
                By.xpath("//div[contains(@class,'filter-option') and contains(text(),'" + level + "')]"));
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

    public boolean isCoChartVisible() {
        return isDisplayed(coChartTitle);
    }

    public boolean isOzoneChartVisible() {
        return isDisplayed(ozoneChartTitle);
    }

    public boolean areChartCanvasesRendered() {
        return driver.findElements(chartCanvases).size() >= 2;
    }

    public void clickExpandCoChart() {
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
