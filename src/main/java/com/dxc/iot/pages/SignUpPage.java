package com.dxc.iot.pages;

import com.dxc.iot.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SignUpPage extends BasePage {

  public static final String PATH = "/signup";

  // Locators
  private final By avatarInput = By.id("avatar-input");
  private final By firstNameInput = By.id("firstName");
  private final By lastNameInput = By.id("lastName");
  private final By emailInput = By.id("email");
  private final By passwordInput = By.id("password");
  private final By confirmPwdInput = By.id("confirmPassword");
  private final By submitButton = By.cssSelector("button.btn-primary[type='submit']");
  private final By avatarError = By.xpath("//span[contains(@class,'field-error') and contains(.,'profile picture')]");

  // Field errors next to each input
  private By fieldErrorFor(String inputId) {
    return By.xpath("//input[@id='" + inputId + "']/ancestor::div[contains(@class,'field-group')]" +
        "//span[contains(@class,'field-error')]");
  }

  private final By errorBanner = By.cssSelector(".error-banner");
  private final By successBanner = By.cssSelector(".success-banner");
  private final By loginLink = By.cssSelector("a[routerLink='/login']");
  private final By removeAvatarButton = By.cssSelector("button.remove-avatar-icon");
  private final By avatarPlaceholder = By.cssSelector(".avatar-placeholder");
  private final By avatarPreviewImg = By.cssSelector(".avatar-preview img");

  public SignUpPage(WebDriver driver) {
    super(driver);
  }

  // Actions

  public SignUpPage open(String baseUrl) {
    driver.get(baseUrl + PATH);
    waitVisible(firstNameInput);
    return this;
  }

  public SignUpPage uploadAvatar(String absoluteFilePath) {
    WebElement input = driver.findElement(avatarInput);
    ((JavascriptExecutor) driver)
        .executeScript("arguments[0].removeAttribute('hidden');", input);
    input.sendKeys(absoluteFilePath);
    return this;
  }

  public SignUpPage enterFirstName(String value) {
    type(firstNameInput, value);
    return this;
  }

  public SignUpPage enterLastName(String value) {
    type(lastNameInput, value);
    return this;
  }

  public SignUpPage enterEmail(String value) {
    type(emailInput, value);
    return this;
  }

  public SignUpPage enterPassword(String value) {
    type(passwordInput, value);
    return this;
  }

  public SignUpPage enterConfirmPassword(String value) {
    type(confirmPwdInput, value);
    return this;
  }

  public void clickCreateAccount() {
    click(submitButton);
  }

  public void signUp(String avatarPath, String firstName, String lastName,
      String email, String password, String confirmPassword) {
    if (avatarPath != null && !avatarPath.isEmpty())
      uploadAvatar(avatarPath);
    if (firstName != null)
      enterFirstName(firstName);
    if (lastName != null)
      enterLastName(lastName);
    if (email != null)
      enterEmail(email);
    if (password != null)
      enterPassword(password);
    if (confirmPassword != null)
      enterConfirmPassword(confirmPassword);
    clickCreateAccount();
  }

  // Assertions

  public boolean isSuccessBannerDisplayed() {
    return isDisplayed(successBanner);
  }

  public String getSuccessBannerText() {
    return getText(successBanner);
  }

  public boolean isErrorBannerDisplayed() {
    return isDisplayed(errorBanner);
  }

  public String getErrorBannerText() {
    return getText(errorBanner);
  }

  public String getAvatarError() {
    return getText(avatarError);
  }

  public String getFirstNameError() {
    return getText(fieldErrorFor("firstName"));
  }

  public String getLastNameError() {
    return getText(fieldErrorFor("lastName"));
  }

  public String getEmailError() {
    return getText(fieldErrorFor("email"));
  }

  public String getPasswordError() {
    return getText(fieldErrorFor("password"));
  }

  public String getConfirmPasswordError() {
    return getText(fieldErrorFor("confirmPassword"));
  }

  public void clickGoToLogin() {
    click(loginLink);
  }

  public void clickRemoveAvatar() {
    click(removeAvatarButton);
  }

  /** True when a preview image is shown (user has uploaded a file). */
  public boolean isAvatarPreviewImageDisplayed() {
    return isDisplayed(avatarPreviewImg);
  }

  /** True when the default placeholder is shown (no file selected). */
  public boolean isAvatarPlaceholderDisplayed() {
    return isDisplayed(avatarPlaceholder);
  }
}