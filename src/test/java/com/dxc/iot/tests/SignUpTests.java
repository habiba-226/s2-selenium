package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.SignUpPage;
import com.dxc.iot.utils.ConfigReader;
import com.dxc.iot.utils.ExcelUtils;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;

public class SignUpTests extends BaseTest {

  private static final String DATA_FILE = "src/test/resources/testdata/SignUpData.xlsx";

  @DataProvider(name = "signupData")
  public Object[][] signupData() {
    return ExcelUtils.getSheetData(DATA_FILE, "signup");
  }

  @Test(dataProvider = "signupData", priority = 1)
  public void testSignUp(Object[] row) {

    // Pull values out of the Excel row by column position
    String testCaseId = cell(row, 0); // col A — testCaseId
    String scenario = cell(row, 1); // col B — scenario
    String avatarPath = cell(row, 2); // col C — avatarPath
    String firstName = cell(row, 3); // col D — firstName
    String lastName = cell(row, 4); // col E — lastName
    String email = cell(row, 5); // col F — email
    String password = cell(row, 6); // col G — password
    String confirmPassword = cell(row, 7); // col H — confirmPassword
    String expectedOutcome = cell(row, 8); // col I — expectedOutcome
    String expectedError = cell(row, 9); // col J — expectedErrorContains

    // Only swap in a dynamic email for the valid sign-up case
    if (testCaseId.equals("TC-FE-A001")) {
      email = "qa_user_" + System.currentTimeMillis() + "@test.com";
    }

    System.out.println("Running: " + testCaseId + " — " + scenario);

    SignUpPage signUpPage = new SignUpPage(driver);
    signUpPage.open(ConfigReader.get("base.url"));
    signUpPage.signUp(avatarPath, firstName, lastName, email, password, confirmPassword);

    switch (expectedOutcome.trim().toLowerCase()) {

      case "success":
        // Wait for either the success banner to appear OR the URL to navigate away from
        // /signup
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
          wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/signup")));
        } catch (Exception e) {
          // If URL didn't change, check if at least the success banner appeared
          if (!signUpPage.isSuccessBannerDisplayed()) {
            Assert.fail(testCaseId + ": Timed out waiting for redirect or success banner. Current URL: "
                + driver.getCurrentUrl());
          }
        }
        break;

      case "bannererror":
        // Wait briefly for backend to return the 400/409 error banner
        Assert.assertTrue(
            signUpPage.isErrorBannerDisplayed(),
            testCaseId + ": expected error banner but none appeared");

        if (!expectedError.isEmpty()) {
          String bannerText = signUpPage.getErrorBannerText().toLowerCase();
          Assert.assertTrue(
              bannerText.contains(expectedError.toLowerCase()),
              testCaseId + ": banner text was '" + bannerText +
                  "' but expected to contain '" + expectedError + "'");
        }
        break;

      case "fielderror":
        // Field-level validationform should NOT have submitted, URL should still be
        // /signup
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/signup"),
            testCaseId + ": form should not have submitted but URL changed to " + driver.getCurrentUrl());

        if (!expectedError.isEmpty()) {
          String pageSource = driver.getPageSource().toLowerCase();
          Assert.assertTrue(
              pageSource.contains(expectedError.toLowerCase()),
              testCaseId + ": expected field error containing '" + expectedError + "' was not found on the page.");
        }
        break;

      default:
        Assert.fail(testCaseId + ": unknown expectedOutcome '" + expectedOutcome + "'");
    }
  }

  // Remove avatar resets layout to placeholder
  @Test(priority = 2)
  public void testRemoveAvatar_A047() {
    System.out.println("Running: TC-FE-A047 — Remove Avatar Resets to Placeholder");

    String avatarPath = new File("src/test/resources/testdata/avatars/valid.png").getAbsolutePath();

    SignUpPage signUpPage = new SignUpPage(driver);
    signUpPage.open(ConfigReader.get("base.url"));
    signUpPage.uploadAvatar(avatarPath);

    Assert.assertTrue(signUpPage.isAvatarPreviewImageDisplayed(),
        "TC-FE-A047: Preview image should be visible after upload");

    signUpPage.clickRemoveAvatar();

    Assert.assertTrue(signUpPage.isAvatarPlaceholderDisplayed(),
        "TC-FE-A047: Placeholder should reappear after removing avatar");
    Assert.assertFalse(signUpPage.isAvatarPreviewImageDisplayed(),
        "TC-FE-A047: Preview image should be gone after removing avatar");
  }

  // "Log in" link navigates to /login

  @Test(priority = 3)
  public void testLoginLinkNavigation_A048() {
    System.out.println("Running: TC-FE-A048 — 'Log in' Link → /login");

    SignUpPage signUpPage = new SignUpPage(driver);
    signUpPage.open(ConfigReader.get("base.url"));
    signUpPage.clickGoToLogin();

    new WebDriverWait(driver, Duration.ofSeconds(5))
        .until(ExpectedConditions.urlContains("/login"));

    Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
        "TC-FE-A048: Expected URL to contain '/login' but was: " + driver.getCurrentUrl());
  }
}