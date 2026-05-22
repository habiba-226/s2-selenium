package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.SettingsPage;
import com.dxc.iot.utils.ConfigReader;
import com.dxc.iot.utils.ExcelUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class SettingsTests extends BaseTest {

  private static final String DATA_FILE = "src/test/resources/testdata/SettingsData.xlsx";

  @DataProvider(name = "settingsData")
  public Object[][] settingsData() {
    return ExcelUtils.getSheetData(DATA_FILE, "settings");
  }

  private SettingsPage loginAndOpenSettings() {
    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));

    String email = ConfigReader.get("test.email");
    String pass = ConfigReader.get("test.password");
    loginPage.enterEmail(email != null ? email : "valid@test.com");
    loginPage.enterPassword(pass != null ? pass : "Pass@123");
    loginPage.clickSignIn();

    new WebDriverWait(driver, Duration.ofSeconds(10))
        .until(ExpectedConditions.urlContains("/home"));

    SettingsPage settings = new SettingsPage(driver);
    settings.open(ConfigReader.get("base.url"));
    return settings;
  }

  @Test(dataProvider = "settingsData")
  public void testSettings(Object[] row) {
    String testCaseId = cell(row, 0); // col A
    String scenario = cell(row, 1); // col B
    String sectionTitle = cell(row, 2); // col C — e.g. "Traffic Sensors"
    String metricLabel = cell(row, 3); // col D — e.g. "Traffic Density"
    String lowerStr = cell(row, 4); // col E — may be empty
    String upperStr = cell(row, 5); // col F — may be empty
    String expectedOutcome = cell(row, 6); // col G
    String expectedError = cell(row, 7); // col H

    System.out.println("Running: " + testCaseId + " — " + scenario);

    SettingsPage settings = loginAndOpenSettings();
    String saveLabel = sectionTitle.replace(" Sensors", " Settings");

    // Use Ctrl+A and Backspace so Angular recognizes the form is truly empty
    List<WebElement> sectionInputs = driver.findElements(By.xpath(
        "//div[contains(@class,'settings-card') and .//h2[normalize-space()='" + sectionTitle + "']]//input"));
    for (WebElement input : sectionInputs) {
      input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE); 
      input.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE); 
    }

    if (!lowerStr.isEmpty()) {
      settings.setLowerThreshold(sectionTitle, metricLabel, lowerStr);
    }
    if (!upperStr.isEmpty()) {
      settings.setUpperThreshold(sectionTitle, metricLabel, upperStr);
    }
    settings.saveSection(saveLabel);

    switch (expectedOutcome.trim().toLowerCase()) {

      case "success":
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        boolean bannerFound = false;
        try {
          wait.until(d -> settings.isSuccessShownFor(sectionTitle));
          bannerFound = true;
        } catch (Exception e) {
          bannerFound = false;
        }
        Assert.assertTrue(bannerFound,
            testCaseId + ": expected success banner in '" + sectionTitle + "' section but none appeared");

        if (!expectedError.isEmpty()) {
          String bannerText = settings.getSuccessText(sectionTitle).toLowerCase();
          Assert.assertTrue(
              bannerText.contains(expectedError.toLowerCase()),
              testCaseId + ": banner text was '" + bannerText + "' but expected to contain '" + expectedError + "'");
        }
        break;

      case "fielderror":
        // Angular validation fires after Save 
        try {
          Thread.sleep(500);
        } catch (Exception ignored) {
        }

        String fieldError = "";
        try {
          fieldError = settings.getMetricFieldError(sectionTitle, metricLabel);
        } catch (Exception e) {
          fieldError = "";
        }
        if (fieldError.isEmpty()) {
          try {
            fieldError = settings.getMetricRowError(sectionTitle, metricLabel);
          } catch (Exception e) {
            fieldError = "";
          }
        }
        Assert.assertFalse(fieldError.isEmpty(),
            testCaseId + ": expected field error on '" + metricLabel + "' in '" + sectionTitle + "' but none appeared");

        if (!expectedError.isEmpty()) {
          Assert.assertTrue(
              fieldError.toLowerCase().contains(expectedError.toLowerCase()),
              testCaseId + ": field error was '" + fieldError + "' but expected to contain '" + expectedError + "'");
        }

        Assert.assertFalse(
            settings.isSuccessShownFor(sectionTitle),
            testCaseId + ": form had validation errors but success banner appeared anyway");
        break;

      default:
        Assert.fail(testCaseId + ": unknown expectedOutcome '" + expectedOutcome + "'");
    }
  }

  // Saved thresholds pre-populate on page reload 

  @Test
  public void testSettingsPrePopulate_A051() {
    System.out.println("Running: TC-FE-A051 — Settings Pre-Populate on Reload");

    SettingsPage settings = loginAndOpenSettings();

    // Clear all Traffic Sensors inputs first
    List<WebElement> trafficInputs = driver.findElements(By.xpath(
        "//div[contains(@class,'settings-card') and .//h2[normalize-space()='Traffic Sensors']]//input"));
    for (WebElement input : trafficInputs) {
      input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
    }

    settings.setLowerThreshold("Traffic Sensors", "Traffic Density", "350");
    settings.saveSection("Traffic Settings");

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    try {
      wait.until(d -> settings.isSuccessShownFor("Traffic Sensors"));
    } catch (Exception e) {
      Assert.fail("TC-FE-A051: Could not save initial threshold value to backend");
    }

    // Navigate away then back — triggers ngOnInit and loadExistingSettings()
    driver.get(ConfigReader.get("base.url") + "/home");
    new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.urlContains("/home"));
    settings.open(ConfigReader.get("base.url"));

    // Allow Angular time to prefill from backend
    try { Thread.sleep(1500); } catch (Exception ignored) {}

    String actual = settings.getLowerThresholdValue("Traffic Sensors", "Traffic Density");
    Assert.assertEquals(actual, "350",
        "TC-FE-A051: Expected pre-populated value '350' but got '" + actual + "'");
  }

  // Air Pollution informational metric chips are visible 

  @Test
  public void testInfoMetricChips_A055() {
    System.out.println("Running: TC-FE-A055 — Air Pollution Informational Metric Chips Visible");

    SettingsPage settings = loginAndOpenSettings();

    String[] chips = { "PM2.5", "PM10", "NO2", "SO2" };
    for (String chip : chips) {
      Assert.assertTrue(
          settings.isInfoMetricChipPresent(chip),
          "TC-FE-A055: Informational chip '" + chip + "' not found on settings page");
    }
  }

  @Test
  public void testSettingsPageStructure_A024() {
    System.out.println("Running: TC-FE-A024 — Settings Page Visual Structure");

    loginAndOpenSettings();

    Assert.assertTrue(
        driver.findElements(By.xpath("//h2[contains(@class,'card-title') and normalize-space()='Traffic Sensors']"))
            .size() > 0,
        "TC-FE-A024: Traffic Sensors card not found");
    Assert.assertTrue(driver
        .findElements(By.xpath("//h2[contains(@class,'card-title') and normalize-space()='Air Pollution Sensors']"))
        .size() > 0, "TC-FE-A024: Air Pollution Sensors card not found");
    Assert.assertTrue(driver
        .findElements(By.xpath("//h2[contains(@class,'card-title') and normalize-space()='Street Lights Sensors']"))
        .size() > 0, "TC-FE-A024: Street Lights Sensors card not found");

    Assert.assertTrue(
        driver.findElements(By.xpath("//button[contains(@class,'btn-save') and contains(.,'Save Traffic Settings')]"))
            .size() > 0,
        "TC-FE-A024: Save Traffic Settings button not found");
    Assert.assertTrue(driver
        .findElements(By.xpath("//button[contains(@class,'btn-save') and contains(.,'Save Air Pollution Settings')]"))
        .size() > 0, "TC-FE-A024: Save Air Pollution Settings button not found");
    Assert.assertTrue(driver
        .findElements(By.xpath("//button[contains(@class,'btn-save') and contains(.,'Save Street Lights Settings')]"))
        .size() > 0, "TC-FE-A024: Save Street Lights Settings button not found");

    Assert.assertTrue(driver.findElements(By.cssSelector(".info-banner")).size() > 0,
        "TC-FE-A024: Info banner not found on page");
  }
}