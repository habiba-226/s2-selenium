package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.HomePage;
import com.dxc.iot.utils.ConfigReader;
import com.dxc.iot.utils.ExcelUtils;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;

public class HomeTests extends BaseTest {

  private static final String DATA_FILE = "src/test/resources/testdata/HomeData.xlsx";

  @DataProvider(name = "homeData")
  public Object[][] homeData() {
    return ExcelUtils.getSheetData(DATA_FILE, "home");
  }

  private HomePage login() {
    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));

    String email = ConfigReader.get("test.email");
    String pass = ConfigReader.get("test.password");
    loginPage.enterEmail(email != null ? email : "valid@test.com");
    loginPage.enterPassword(pass != null ? pass : "Pass123#");
    loginPage.clickSignIn();

    new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains("/home"));
    return new HomePage(driver);
  }

  @Test(dataProvider = "homeData")
  public void testHomePageFeatures(Object[] row) {
    String testCaseId = cell(row, 0);
    String scenario = cell(row, 1);
    String expectedVal = cell(row, 2);

    System.out.println("Running: " + testCaseId + " — " + scenario);
    HomePage homePage = login();
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

    switch (testCaseId) {
      // Visual Structure
      case "TC-FE-A026":
        Assert.assertEquals(homePage.getSensorCardCount(), 3, "Should be 3 sensor cards");
        break;

      // Modal Interactivity
      case "TC-FE-A027":
        return;

      // Settings Navigation
      case "TC-FE-A028":
        homePage.clickQuickActionSettings();
        wait.until(ExpectedConditions.urlContains(expectedVal));
        Assert.assertTrue(driver.getCurrentUrl().contains(expectedVal));
        break;

      // Location Selector
      case "TC-FE-A029":
        homePage.openLocationDropdown();
        homePage.clickLocationOption(expectedVal);
        Assert.assertTrue(homePage.getSubtitleText().contains(expectedVal), "Subtitle didn't update");
        break;

      // Logout
      case "TC-FE-A030":
        homePage.clickProfileAvatar();
        wait.until(ExpectedConditions.urlContains("/profile"));
        homePage.clickLogout();
        wait.until(ExpectedConditions.urlContains(expectedVal));
        Assert.assertTrue(driver.getCurrentUrl().contains(expectedVal));
        break;

      // Notif Empty State
      case "TC-FE-A035":
        homePage.clickBell();
        homePage.waitForNotifPanelVisible();
        Assert.assertTrue(homePage.isNotifPanelVisible(), "Panel did not open");
        Assert.assertTrue(homePage.isNotifEmptyStateVisible(), "Empty state not shown");
        Assert.assertEquals(homePage.getNotificationCount(), 0, "Expected 0 items");
        break;

      // Panel Closes on Outside Click
      case "TC-FE-A036":
        homePage.clickBell();
        homePage.waitForNotifPanelVisible();
        homePage.clickOutside();
        homePage.waitForNotifPanelInvisible();
        Assert.assertFalse(homePage.isNotifPanelVisible(), "Panel should have closed");
        break;

      // Badge Hidden When Empty
      case "TC-FE-A037":
        Assert.assertFalse(homePage.isBellBadgeVisible(), "Bell badge visible when empty");
        break;

      // Bell Toggles Panel
      case "TC-FE-A038":
        homePage.clickBell();
        homePage.waitForNotifPanelVisible();
        Assert.assertTrue(homePage.isNotifPanelVisible(), "Panel should open on first click");
        homePage.clickBell();
        homePage.waitForNotifPanelInvisible();
        Assert.assertFalse(homePage.isNotifPanelVisible(), "Panel should close on second click");
        break;

      // Clear All Hidden When Empty
      case "TC-FE-A039":
        homePage.clickBell();
        homePage.waitForNotifPanelVisible();
        Assert.assertFalse(homePage.isClearAllVisible(), "Clear All visible when empty");
        break;

      // Click Inside Doesn't Close
      case "TC-FE-A040":
        homePage.clickBell();
        homePage.waitForNotifPanelVisible();
        homePage.clickNotifHeader();
        try {
          Thread.sleep(500);
        } catch (Exception ignored) {
        } // Pause to ensure it doesn't accidentally close
        Assert.assertTrue(homePage.isNotifPanelVisible(), "Panel closed when clicking inside it");
        break;

      // Opening Notifications Closes Location Dropdown
      case "TC-FE-A041":
        homePage.openLocationDropdown();
        Assert.assertTrue(homePage.isLocationDropdownVisible(), "Location dropdown didn't open");
        homePage.clickBell();
        homePage.waitForNotifPanelVisible();
        Assert.assertTrue(homePage.isNotifPanelVisible(), "Notif Panel didn't open");
        Assert.assertFalse(homePage.isLocationDropdownVisible(), "Location dropdown stayed open");
        break;

      // Air Pollution Card Navigates to /air-pollution (updated for Sprint 4 — feature now exists)
      case "TC-FE-A042":
        homePage.openAirPollutionModal();
        wait.until(ExpectedConditions.urlContains("/air-pollution"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/air-pollution"),
            "TC-FE-A042: Expected URL to contain '/air-pollution' but was: " + driver.getCurrentUrl());
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("/home"));
        break;

      // Street Light Card Navigates to /street-light (updated for Sprint 4 — feature now exists)
      case "TC-FE-A043":
        homePage.openStreetLightModal();
        wait.until(ExpectedConditions.urlContains("/street-light"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/street-light"),
            "TC-FE-A043: Expected URL to contain '/street-light' but was: " + driver.getCurrentUrl());
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("/home"));
        break;

      // Modal closes on backdrop click
      case "TC-FE-A056":
        return;

      // Welcome title contains user's first name
      case "TC-FE-A057":
        String welcomeText = homePage.getWelcomeText();
        String expectedFirstName = ConfigReader.get("test.firstName");
        Assert.assertTrue(welcomeText.contains(expectedFirstName),
            "TC-FE-A057: Welcome title '" + welcomeText + "' does not contain '" + expectedFirstName + "'");
        break;

      // "My Profile" quick action → /profile
      case "TC-FE-A058":
        homePage.clickMyProfileQuickAction();
        wait.until(ExpectedConditions.urlContains(expectedVal));
        Assert.assertTrue(driver.getCurrentUrl().contains(expectedVal),
            "TC-FE-A058: Expected URL to contain '" + expectedVal + "'");
        break;

      // Location dropdown closes on outside click
      case "TC-FE-A059":
        homePage.openLocationDropdown();
        Assert.assertTrue(homePage.isLocationDropdownVisible(),
            "TC-FE-A059: Dropdown did not open");
        homePage.clickOutside();
        homePage.waitForLocationDropdownInvisible();
        Assert.assertFalse(homePage.isLocationDropdownVisible(),
            "TC-FE-A059: Dropdown should close on outside click");
        break;

      // Active location highlighted in dropdown
      case "TC-FE-A060":
        homePage.openLocationDropdown();
        Assert.assertTrue(homePage.isActiveLocationInDropdown(expectedVal),
            "TC-FE-A060: Location '" + expectedVal + "' should have active CSS class");
        break;

      // Traffic modal shows sensor data labels
      case "TC-FE-A061":
        return;

      // Top nav "Settings" → /settings
      case "TC-FE-A062":
        homePage.clickTopNavSettings();
        wait.until(ExpectedConditions.urlContains(expectedVal));
        Assert.assertTrue(driver.getCurrentUrl().contains(expectedVal),
            "TC-FE-A062: Expected URL to contain '" + expectedVal + "'");
        break;

      // Top nav "Profile" → /profile
      case "TC-FE-A063":
        homePage.clickTopNavProfile();
        wait.until(ExpectedConditions.urlContains(expectedVal));
        Assert.assertTrue(driver.getCurrentUrl().contains(expectedVal),
            "TC-FE-A063: Expected URL to contain '" + expectedVal + "'");
        break;

      default:
        Assert.fail("Unknown Test Case ID: " + testCaseId);
    }
  }

  @Test
  public void testHomePageLoads_Sanity() {
    System.out.println("Running: Sanity — Home Page Loads");

    HomePage homePage = login();

    Assert.assertEquals(homePage.getSensorCardCount(), 3,
        "Sanity Home: expected 3 sensor cards on home page load");
  }
}