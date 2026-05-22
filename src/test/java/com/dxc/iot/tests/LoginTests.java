package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.utils.ConfigReader;
import com.dxc.iot.utils.ExcelUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginTests extends BaseTest {

  private static final String DATA_FILE = "src/test/resources/testdata/SignInData.xlsx";

  @DataProvider(name = "loginData")
  public Object[][] loginData() {
    return ExcelUtils.getSheetData(DATA_FILE, "signin");
  }

  @Test(dataProvider = "loginData", priority = 1)
  public void testLogin(Object[] row) {

    String testCaseId = cell(row, 0);
    String scenario = cell(row, 1);
    String email = cell(row, 2);
    String password = cell(row, 3);
    String expectedOutcome = cell(row, 4);
    String expectedError = cell(row, 5);

    System.out.println("Running: " + testCaseId + " — " + scenario);

    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));

    if (!email.isEmpty())
      loginPage.enterEmail(email);
    if (!password.isEmpty())
      loginPage.enterPassword(password);
    loginPage.clickSignIn();

    switch (expectedOutcome.trim().toLowerCase()) {

      case "success":
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
          wait.until(ExpectedConditions.urlContains("/home"));
        } catch (Exception e) {
          Assert.fail(testCaseId + ": Timed out waiting for redirect to /home. Current URL: " + driver.getCurrentUrl());
        }

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(
            currentUrl.contains("/home"),
            testCaseId + ": expected redirect to /home but was: " + currentUrl);
        break;

      case "bannererror":
        Assert.assertTrue(
            loginPage.isErrorBannerDisplayed(),
            testCaseId + ": expected error banner but none appeared");
        if (!expectedError.isEmpty()) {
          String bannerText = loginPage.getErrorBannerText().toLowerCase();
          Assert.assertTrue(
              bannerText.contains(expectedError.toLowerCase()),
              testCaseId + ": banner text was '" + bannerText +
                  "' but expected to contain '" + expectedError + "'");
        }
        break;

      case "fielderror":
        Assert.assertFalse(
            driver.getCurrentUrl().contains("/home"),
            testCaseId + ": form should not have submitted but URL changed to /home");
        break;

      default:
        Assert.fail(testCaseId + ": unknown expectedOutcome '" + expectedOutcome + "'");
    }
  }

  // Password field show/hide toggle
  @Test(priority = 2)
  public void testPasswordToggle_A044() {
    System.out.println("Running: TC-FE-A044 — Password Show/Hide Toggle");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));
    loginPage.enterPassword("anyText");

    Assert.assertEquals(loginPage.getPasswordInputType(), "password",
        "TC-FE-A044: Input should start as 'password'");

    loginPage.clickPasswordToggle();
    Assert.assertEquals(loginPage.getPasswordInputType(), "text",
        "TC-FE-A044: Input should switch to 'text' after first toggle");

    loginPage.clickPasswordToggle();
    Assert.assertEquals(loginPage.getPasswordInputType(), "password",
        "TC-FE-A044: Input should revert to 'password' after second toggle");
  }

  // "Create one" link navigates to /signup
  @Test(priority = 3)
  public void testCreateOneLinkNavigation_A045() {
    System.out.println("Running: TC-FE-A045 — 'Create one' Link → /signup");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));
    loginPage.clickGoToSignup();

    new WebDriverWait(driver, Duration.ofSeconds(5))
        .until(ExpectedConditions.urlContains("/signup"));

    Assert.assertTrue(driver.getCurrentUrl().contains("/signup"),
        "TC-FE-A045: Expected URL to contain '/signup' but was: " + driver.getCurrentUrl());
  }

  // Account locked banner after 3 failed attempts
  @Test(priority = 4)
  public void testAccountLocked_A046() {
    System.out.println("Running: TC-FE-A046 — Account Locked on 3 Failed Attempts");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));

    String email = "lockout_sandbox_user@test.com";

    for (int attempt = 1; attempt <= 3; attempt++) {

      driver.findElement(By.cssSelector("input[type='email']"))
          .sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
      driver.findElement(By.cssSelector("input[id='password']")) // ← was formControlName
          .sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);

      loginPage.enterEmail(email);
      loginPage.enterPassword("WrongPass123!");
      loginPage.clickSignIn();

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
      try {
        wait.until(d -> loginPage.isErrorBannerDisplayed());
      } catch (Exception ignored) {
      }

      if (loginPage.isAccountLocked())
        break;
    }

    Assert.assertTrue(loginPage.isAccountLocked(),
        "TC-FE-A046: Expected locked-banner after 3 failed attempts");

    WebElement submitBtn = driver.findElement(By.cssSelector("button.btn-primary[type='submit']"));
    Assert.assertFalse(submitBtn.isEnabled(),
        "TC-FE-A046: Submit button should be disabled when account is locked");
    Assert.assertTrue(submitBtn.getText().trim().contains("Account Locked"),
        "TC-FE-A046: Button text should contain 'Account Locked'");
  }
}