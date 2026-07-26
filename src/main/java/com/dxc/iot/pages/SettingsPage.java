package com.dxc.iot.pages;

import com.dxc.iot.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class SettingsPage extends BasePage {

  public static final String PATH = "/settings";

  private final By sidebarDashboardLink = By.cssSelector("a.nav-item[routerLink='/home']");
  private final By logoutBtn = By.cssSelector("button.logout-btn");

  //  Section titles 
  public static final String SECTION_TRAFFIC = "Traffic Sensors";
  public static final String SECTION_AIR_POLLUTION = "Air Pollution Sensors";
  public static final String SECTION_STREET_LIGHTS = "Street Lights Sensors";

  //  Section labels 
  public static final String LABEL_TRAFFIC = "Traffic";
  public static final String LABEL_AIR_POLLUTION = "Air Pollution";
  public static final String LABEL_STREET_LIGHTS = "Street Lights";

  // Metric labels 
  public static final String METRIC_TRAFFIC_DENSITY = "Traffic Density";
  public static final String METRIC_AVERAGE_SPEED = "Average Speed";
  public static final String METRIC_CO_LEVEL = "CO Level";
  public static final String METRIC_OZONE = "Ozone";
  public static final String METRIC_BRIGHTNESS_LEVEL = "Brightness Level";
  public static final String METRIC_POWER_CONSUMPTION = "Power Consumption";

  private static final String SAVE_BTN_PREFIX = "Save ";

  public SettingsPage(WebDriver driver) {
    super(driver);
  }

  public SettingsPage open(String baseUrl) {
    driver.get(baseUrl + PATH);
    waitVisible(By.xpath("//h2[contains(@class,'card-title')]"));
    return this;
  }

  // Locator builders 

  private By thresholdInput(String sectionTitle, String metricLabel, int position) {
    return By.xpath(
        "//div[contains(@class,'settings-card') and .//h2[normalize-space()='" + sectionTitle + "']]" +
            "//div[contains(@class,'metric-row') and .//span[contains(@class,'metric-name') and normalize-space()='"
            + metricLabel + "']]" +
            "//div[contains(@class,'metric-input-wrap')][" + position + "]//input");
  }

  private By saveButtonFor(String sectionLabel) {
    return By.xpath("//button[contains(@class,'btn-save') and contains(.,'" + SAVE_BTN_PREFIX + sectionLabel + "')]");
  }

  private By sectionSuccessBanner(String sectionTitle) {
    return By.xpath(
        "//div[contains(@class,'settings-card') and .//h2[normalize-space()='" + sectionTitle + "']]" +
            "//div[contains(@class,'feedback-banner') and contains(@class,'success-banner')]");
  }

  private By sectionErrorBanner(String sectionTitle) {
    return By.xpath(
        "//div[contains(@class,'settings-card') and .//h2[normalize-space()='" + sectionTitle + "']]" +
            "//div[contains(@class,'feedback-banner') and contains(@class,'error-banner')]");
  }

  private By metricFieldError(String sectionTitle, String metricLabel) {
    return By.xpath(
        "//div[contains(@class,'settings-card') and .//h2[normalize-space()='" + sectionTitle + "']]" +
            "//div[contains(@class,'metric-row') and .//span[contains(@class,'metric-name') and normalize-space()='"
            + metricLabel + "']]" +
            "//span[contains(@class,'field-error')]");
  }

  private By sectionInputs(String sectionTitle) {
    return By.xpath(
        "//div[contains(@class,'settings-card') and .//h2[normalize-space()='" + sectionTitle + "']]//input");
  }

  private By sectionCardTitle(String sectionTitle) {
    return By.xpath("//h2[contains(@class,'card-title') and normalize-space()='" + sectionTitle + "']");
  }

  private final By infoBanner = By.cssSelector(".info-banner");

  //  Actions

  public SettingsPage setLowerThreshold(String sectionTitle, String metricLabel, String value) {
    type(thresholdInput(sectionTitle, metricLabel, 1), value);
    return this;
  }

  public SettingsPage setUpperThreshold(String sectionTitle, String metricLabel, String value) {
    type(thresholdInput(sectionTitle, metricLabel, 2), value);
    return this;
  }

  public SettingsPage clearLowerThreshold(String sectionTitle, String metricLabel) {
    driver.findElement(thresholdInput(sectionTitle, metricLabel, 1)).clear();
    return this;
  }

  public SettingsPage clearUpperThreshold(String sectionTitle, String metricLabel) {
    driver.findElement(thresholdInput(sectionTitle, metricLabel, 2)).clear();
    return this;
  }

  public void saveSection(String sectionLabel) {
    click(saveButtonFor(sectionLabel));
  }

  /**
   * Clears every input in a section via Ctrl+A/Backspace (and Cmd+A for macOS)
   * so Angular recognizes the form as truly empty.
   */
  public void clearAllInputsInSection(String sectionTitle) {
    List<WebElement> inputs = driver.findElements(sectionInputs(sectionTitle));
    for (WebElement input : inputs) {
      input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
      input.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
    }
  }

  public boolean isSectionCardPresent(String sectionTitle) {
    return driver.findElements(sectionCardTitle(sectionTitle)).size() > 0;
  }

  public boolean isSaveButtonPresentFor(String sectionLabel) {
    return driver.findElements(saveButtonFor(sectionLabel)).size() > 0;
  }

  public boolean isInfoBannerPresent() {
    return driver.findElements(infoBanner).size() > 0;
  }

  // Assertions

  public boolean isSuccessShownFor(String sectionTitle) {
    return isDisplayed(sectionSuccessBanner(sectionTitle));
  }

  public boolean isErrorShownFor(String sectionTitle) {
    return isDisplayed(sectionErrorBanner(sectionTitle));
  }

  public String getSuccessText(String sectionTitle) {
    return getText(sectionSuccessBanner(sectionTitle));
  }

  public String getErrorText(String sectionTitle) {
    return getText(sectionErrorBanner(sectionTitle));
  }

  public String getMetricFieldError(String sectionTitle, String metricLabel) {
    return getText(metricFieldError(sectionTitle, metricLabel));
  }

  // Cross-field range error 
  private By metricRowError(String sectionTitle, String metricLabel) {
    return By.xpath(
        "//div[contains(@class,'settings-card') and .//h2[normalize-space()='" + sectionTitle + "']]" +
        "//div[contains(@class,'metric-row') and .//span[contains(@class,'metric-name') and normalize-space()='"
        + metricLabel + "']]" +
        "//div[contains(@class,'row-error')]");
  }

  public String getMetricRowError(String sectionTitle, String metricLabel) {
    return getText(metricRowError(sectionTitle, metricLabel));
  }

  // Informational metric chips 

  private By infoMetricChip(String label) {
    return By.xpath(
        "//div[contains(@class,'info-metric-chip')]" +
        "[.//span[contains(@class,'chip-label') and normalize-space()='" + label + "']]");
  }

  public boolean isInfoMetricChipPresent(String label) {
    return isDisplayed(infoMetricChip(label));
  }

  // Input value reader

  public String getLowerThresholdValue(String sectionTitle, String metricLabel) {
    return driver.findElement(thresholdInput(sectionTitle, metricLabel, 1)).getDomProperty("value");
  }

  // Sidebar

  public void clickSidebarDashboard() {
    click(sidebarDashboardLink);
  }

  public void clickLogout() {
    click(logoutBtn);
  }
}