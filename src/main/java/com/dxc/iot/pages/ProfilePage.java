package com.dxc.iot.pages;

import com.dxc.iot.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProfilePage extends BasePage {

  public static final String PATH = "/profile";

  // Avatar card
  private final By avatarImg = By.cssSelector(".avatar-img");
  private final By avatarInitials = By.cssSelector(".avatar-initials");
  private final By userFullName = By.cssSelector(".user-full-name");
  private final By userEmail = By.cssSelector(".user-email-display");
  private final By changeAvatarBtn = By.cssSelector("label.change-avatar-btn");
  private final By avatarUploadInput = By.id("avatar-upload");
  private final By avatarSuccessBanner = By
      .xpath("//div[contains(@class,'avatar-card')]//div[contains(@class,'success-banner')]");
  private final By avatarErrorBanner = By
      .xpath("//div[contains(@class,'avatar-card')]//div[contains(@class,'error-banner')]");

  // Account details card (read-only display)
  private By infoValueFor(String label) {
    return By.xpath("//div[contains(@class,'info-item')]" +
        "[.//span[contains(@class,'info-label') and normalize-space()='" + label + "']]" +
        "//span[contains(@class,'info-value')]");
  }

  // Change Password card 
  private final By passwordToggleBtn = By.cssSelector(".toggle-btn");
  private final By currentPasswordInput = By.cssSelector("input[formcontrolname='currentPassword']");
  private final By newPasswordInput = By.id("newPassword");
  private final By updatePasswordBtn = By
      .xpath("//button[contains(@class,'btn-primary') and contains(.,'Update Password')]");
  private final By passwordSuccessBanner = By
      .xpath("//div[contains(@class,'success-banner')][not(ancestor::div[contains(@class,'avatar-card')])]");
  private final By passwordErrorBanner = By
      .xpath("//form[.//input[@id='newPassword']]//div[contains(@class,'error-banner')]");

  // Cross-field error: "New password cannot be the same as current password."
  private final By samePasswordError = By
      .xpath("//span[contains(@class,'field-error') and contains(.,'cannot be the same')]");

  // Generic field-errors near currentPassword / newPassword
  private final By currentPwdFieldError = By
      .xpath("//input[@formcontrolname='currentPassword']/ancestor::div[contains(@class,'field-group')]" +
          "//span[contains(@class,'field-error')]");
  private final By newPwdFieldError = By
      .xpath("//input[@id='newPassword']/ancestor::div[contains(@class,'field-group')]" +
          "//span[contains(@class,'field-error')]");

  // Sidebar
  private final By logoutBtn = By.cssSelector(".logout-btn");
  private final By sidebarDashboardLink = By.cssSelector("a.nav-item[routerLink='/home']");
  private final By sidebarSettingsLink  = By.cssSelector("a.nav-item[routerLink='/settings']");

  public ProfilePage(WebDriver driver) {
    super(driver);
  }

  // Navigation

  public ProfilePage open(String baseUrl) {
    driver.get(baseUrl + PATH);
    waitVisible(userFullName);
    return this;
  }

  // Avatar actions 

  public ProfilePage uploadNewAvatar(String absoluteFilePath) {
    WebElement input = driver.findElement(avatarUploadInput);
    ((JavascriptExecutor) driver)
        .executeScript("arguments[0].removeAttribute('hidden');", input);
    input.sendKeys(absoluteFilePath);
    return this;
  }

  public boolean isAvatarSuccessShown() {
    return isDisplayed(avatarSuccessBanner);
  }

  public boolean isAvatarErrorShown() {
    return isDisplayed(avatarErrorBanner);
  }

  // Account details (read-only assertions)

  public String getDisplayedFullName() {
    return getText(userFullName);
  }

  public String getDisplayedEmail() {
    return getText(userEmail);
  }

  public String getFirstNameValue() {
    return getText(infoValueFor("First name"));
  }

  public String getLastNameValue() {
    return getText(infoValueFor("Last name"));
  }

  public String getEmailValue() {
    return getText(infoValueFor("Email address"));
  }

  // Password change flow 

  public ProfilePage openChangePasswordForm() {
    if (!isPasswordFormVisible()) {
      click(passwordToggleBtn);
    }
    waitVisible(currentPasswordInput);
    return this;
  }

  public ProfilePage enterCurrentPassword(String value) {
    type(currentPasswordInput, value);
    return this;
  }

  public ProfilePage enterNewPassword(String value) {
    type(newPasswordInput, value);
    return this;
  }

  public void clickUpdatePassword() {
    click(updatePasswordBtn);
  }

  public void changePassword(String currentPwd, String newPwd) {
    openChangePasswordForm();
    enterCurrentPassword(currentPwd);
    enterNewPassword(newPwd);
    clickUpdatePassword();
  }

  public boolean isPasswordSuccessShown() {
    try {
      new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8))
          .until(org.openqa.selenium.support.ui.ExpectedConditions
              .presenceOfElementLocated(passwordSuccessBanner));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String getPasswordSuccessText() {
    return getText(passwordSuccessBanner);
  }

  public boolean isPasswordErrorShown() {
    return isDisplayed(passwordErrorBanner);
  }

  public String getPasswordErrorText() {
    return getText(passwordErrorBanner);
  }

  public boolean isSamePasswordErrorShown() {
    return isDisplayed(samePasswordError);
  }

  public String getCurrentPwdFieldError() {
    return getText(currentPwdFieldError);
  }

  public String getNewPwdFieldError() {
    return getText(newPwdFieldError);
  }

  // Password form visibility 
  public void clickCancelPasswordForm() {
    click(passwordToggleBtn);
  }

  /** True when the change-password form is currently expanded in the DOM. */
  public boolean isPasswordFormVisible() {
    return !driver.findElements(currentPasswordInput).isEmpty();
  }

  // Sidebar

  public void clickSignOut() {
    click(logoutBtn);
  }

  public void clickSidebarDashboard() {
    click(sidebarDashboardLink);
  }

  public void clickSidebarSettings() {
    click(sidebarSettingsLink);
  }

  // Avatar display assertions

  /** True when the user has an uploaded profile picture (img element renders). */
  public boolean isAvatarImageDisplayed() {
    return isDisplayed(avatarImg);
  }

  /** True when initials fallback is shown (no profile picture set). */
  public boolean isAvatarInitialsDisplayed() {
    return isDisplayed(avatarInitials);
  }

  /** Clicks the "Change photo" label. Equivalent to clicking the hidden input. */
  public void clickChangeAvatarButton() {
    click(changeAvatarBtn);
  }
}