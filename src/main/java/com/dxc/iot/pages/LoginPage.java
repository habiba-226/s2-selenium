package com.dxc.iot.pages;

import com.dxc.iot.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

  public static final String PATH = "/login";

  // Locators 
  private final By emailInput = By.id("email");
  private final By passwordInput = By.id("password");
  private final By submitButton = By.cssSelector("button.btn-primary[type='submit']");

  // Field-level errors 
  private final By emailFieldError = By.xpath("//input[@id='email']/ancestor::div[contains(@class,'field-group')]" +
      "//span[contains(@class,'field-error')]");
  private final By passwordFieldError = By
      .xpath("//input[@id='password']/ancestor::div[contains(@class,'field-group')]" +
          "//span[contains(@class,'field-error')]");

  // Banner-level errors 
  private final By errorBanner = By.cssSelector(".error-banner");
  private final By lockedBanner = By.cssSelector(".error-banner.locked-banner");

  // Password visibility toggle
  private final By passwordToggleBtn = By.cssSelector("button.toggle-password");

  // Navigation
  private final By signupLink = By.cssSelector("a[routerLink='/signup']");

  public LoginPage(WebDriver driver) {
    super(driver);
  }

  // Actions 
  public LoginPage open(String baseUrl) {
    driver.get(baseUrl + PATH);
    waitVisible(emailInput);
    return this;
  }

  public LoginPage enterEmail(String email) {
    type(emailInput, email);
    return this;
  }

  public LoginPage enterPassword(String password) {
    type(passwordInput, password);
    return this;
  }

  public void clickSignIn() {
    click(submitButton);
  }


  public void signIn(String email, String password) {
    enterEmail(email);
    enterPassword(password);
    clickSignIn();
  }

  public void clickGoToSignup() {
    click(signupLink);
  }

  public String getPasswordInputType() {
    return driver.findElement(passwordInput).getDomProperty("type");
  }

  public void clickPasswordToggle() {
    click(passwordToggleBtn);
  }

  // Assertions 

  public boolean isErrorBannerDisplayed() {
    return isDisplayed(errorBanner);
  }

  public String getErrorBannerText() {
    return getText(errorBanner);
  }

  public boolean isAccountLocked() {
    return isDisplayed(lockedBanner);
  }

  public String getEmailFieldError() {
    return getText(emailFieldError);
  }

  public String getPasswordFieldError() {
    return getText(passwordFieldError);
  }
}