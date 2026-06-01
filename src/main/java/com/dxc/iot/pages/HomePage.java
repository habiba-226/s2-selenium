package com.dxc.iot.pages;

import com.dxc.iot.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

  public static final String PATH = "/home";

  // Top bar
  private final By avatarCircle = By.cssSelector(".avatar-circle");
  private final By welcomeTitle = By.cssSelector(".welcome-title");

  // Location Dropdown
  private final By locationSelectorBox = By.cssSelector(".location-selector");
  private final By locationDropdown = By.cssSelector(".location-dropdown");
  private final By welcomeSubtitle = By.cssSelector(".welcome-subtitle");

  // Notification Bell
  private final By bellButton = By.cssSelector("button.bell-btn");
  private final By bellBadge = By.cssSelector(".bell-badge");
  private final By notifPanel = By.cssSelector(".notif-panel");
  private final By notifEmptyState = By.cssSelector(".notif-empty");
  private final By notifClearAll = By.cssSelector(".notif-clear-all");
  private final By notifItems = By.cssSelector(".notif-item");
  private final By notifHeader = By.cssSelector(".notif-header");
  private final By documentBody = By.cssSelector(".welcome-title");

  // Sensor cards (3)
  private final By sensorCards = By.cssSelector(".sensor-card");
  private final By trafficCard =
          By.cssSelector(".sensor-card:nth-child(1)");

  private final By airCard =
          By.cssSelector(".sensor-card:nth-child(2)");

  private final By streetLightCard =
          By.cssSelector(".sensor-card:nth-child(3)");

  // Quick actions
  private final By quickActionSettings = By
      .xpath("//div[contains(@class,'action-card')][.//span[contains(.,'Alert Settings')]]");
  private final By quickActionProfile = By
      .xpath("//div[contains(@class,'action-card')][.//span[contains(.,'My Profile')]]");

  // Top navigation bar
  private final By topNavSettings = By.cssSelector("a.top-nav-item[routerLink='/settings']");
  private final By topNavProfile = By.cssSelector("a.top-nav-item[routerLink='/profile']");

  // Modal
  private final By modalOverlay = By.cssSelector(".modal-overlay");
  private final By modalClose = By.cssSelector(".modal-close");

  public HomePage(WebDriver driver) {
    super(driver);
  }

  public HomePage open(String baseUrl) {
    driver.get(baseUrl + PATH);
    waitVisible(welcomeTitle);
    return this;
  }

  // Core Methods
  public String getWelcomeText() {
    return getText(welcomeTitle);
  }

  public void clickProfileAvatar() {
    click(avatarCircle);
  }

  public void clickQuickActionSettings() {
    click(quickActionSettings);
  }

  public int getSensorCardCount() {
    return driver.findElements(sensorCards).size();
  }

  // Modal Methods
  public void openTrafficModal() {
    ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("arguments[0].click();",
                    driver.findElements(By.cssSelector(".sensor-card")).get(0));
  }

  public void openAirPollutionModal() {
    ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("arguments[0].click();",
                    driver.findElements(By.cssSelector(".sensor-card")).get(1));
  }

  public void openStreetLightModal() {
    ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("arguments[0].click();",
                    driver.findElements(By.cssSelector(".sensor-card")).get(2));
  }

  public boolean isModalOpen() {
    return isDisplayed(modalOverlay);
  }

  public void closeModal() {
    ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("arguments[0].click();",
                    driver.findElement(modalClose));
  }

  // Location Methods
  public void openLocationDropdown() {
    click(locationSelectorBox);
  }

  public boolean isLocationDropdownVisible() {
    return isDisplayed(locationDropdown);
  }

  public void clickLocationOption(String locationName) {
    click(By.xpath("//div[contains(@class,'location-option') and contains(text(), '" + locationName + "')]"));
  }

  public String getSubtitleText() {
    return getText(welcomeSubtitle);
  }

  // Notification Methods
  public void clickBell() {
    click(bellButton);
  }

  public boolean isNotifPanelVisible() {
    return isDisplayed(notifPanel);
  }

  public boolean isBellBadgeVisible() {
    return isDisplayed(bellBadge);
  }

  public boolean isNotifEmptyStateVisible() {
    return isDisplayed(notifEmptyState);
  }

  public boolean isClearAllVisible() {
    return isDisplayed(notifClearAll);
  }

  public int getNotificationCount() {
    return driver.findElements(notifItems).size();
  }

  public void clickNotifHeader() {
    click(notifHeader);
  }

  public void clickOutside() {
    click(documentBody);
  }

  // Quick Actions
  public void clickMyProfileQuickAction() {
    click(quickActionProfile);
  }

  // Top Navigation

  public void clickTopNavSettings() {
    click(topNavSettings);
  }

  public void clickTopNavProfile() {
    click(topNavProfile);
  }

  public void clickModalOverlay() {
    ((JavascriptExecutor) driver).executeScript(
        "arguments[0].dispatchEvent(new MouseEvent('click', {bubbles:true, cancelable:true}));",
        driver.findElement(modalOverlay));
  }

  // Location dropdown helpers

  public boolean isActiveLocationInDropdown(String locationName) {
    By locator = By.xpath(
        "//div[contains(@class,'location-option') and contains(@class,'active')" +
            " and contains(normalize-space(),'" + locationName + "')]");
    return isDisplayed(locator);
  }

  // Modal data assertions

  public boolean isModalDetailLabelVisible(String labelText) {
    By locator = By.xpath(
        "//span[contains(@class,'detail-label') and normalize-space()='" + labelText + "']");
    return isDisplayed(locator);
  }
}