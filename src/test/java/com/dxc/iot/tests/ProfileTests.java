package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.ProfilePage;
import com.dxc.iot.utils.ConfigReader;
import com.dxc.iot.utils.ExcelUtils;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;

public class ProfileTests extends BaseTest {

  private static final String DATA_FILE = "src/test/resources/testdata/ProfileData.xlsx";

  @DataProvider(name = "profileData")
  public Object[][] profileData() {
    return ExcelUtils.getSheetData(DATA_FILE, "profile");
  }

  @Test(dataProvider = "profileData")
  public void testChangePassword(Object[] row) {

    // Pull values out of the Excel row
    String testCaseId = cell(row, 0);
    String scenario = cell(row, 1);
    String currentPassword = cell(row, 2);
    String newPassword = cell(row, 3);
    String expectedOutcome = cell(row, 4);
    String expectedError = cell(row, 5);
    String email = ConfigReader.get("test.email");

    System.out.println("Running: " + testCaseId + " — " + scenario);

    // Log in first
    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));

    loginPage.enterEmail(email != null ? email : "valid@test.com");
    if (!currentPassword.isEmpty())
      loginPage.enterPassword(currentPassword);
    loginPage.clickSignIn();

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    try {
      wait.until(ExpectedConditions.urlContains("/home"));
    } catch (Exception e) {
      Assert.fail(testCaseId + ": Setup Failed - Could not log in. Check if currentPassword is correct in Excel!");
    }

    // Navigate to Profile and change password
    ProfilePage profilePage = new ProfilePage(driver);
    profilePage.open(ConfigReader.get("base.url"));

    profilePage.changePassword(currentPassword, newPassword);

    switch (expectedOutcome.trim().toLowerCase()) {

      case "success":
        // Wait up to 8 seconds for the success banner to appear
        WebDriverWait successWait = new WebDriverWait(driver, Duration.ofSeconds(8));
        boolean bannerAppeared = false;
        try {
          successWait.until(d -> profilePage.isPasswordSuccessShown());
          bannerAppeared = true;
        } catch (Exception e) {
          bannerAppeared = false;
        }
        Assert.assertTrue(bannerAppeared,
            testCaseId + ": expected success banner but none appeared");

        // TEARDOWN — revert password back to original
        profilePage.open(ConfigReader.get("base.url"));
        profilePage.changePassword(newPassword, currentPassword);

        WebDriverWait teardownWait = new WebDriverWait(driver, Duration.ofSeconds(8));
        boolean reverted = false;
        try {
          teardownWait.until(d -> profilePage.isPasswordSuccessShown());
          reverted = true;
        } catch (Exception e) {
          reverted = false;
        }
        Assert.assertTrue(reverted,
            testCaseId + ": TEARDOWN FAILED — password stuck as '" + newPassword +
                "'. Delete user from DB and re-register before next run.");
        break;

      case "bannererror":
        Assert.assertTrue(
            profilePage.isPasswordErrorShown() || profilePage.isSamePasswordErrorShown(),
            testCaseId + ": expected error banner but none appeared");

        if (!expectedError.isEmpty()) {
          String pageSource = driver.getPageSource().toLowerCase();
          Assert.assertTrue(
              pageSource.contains(expectedError.toLowerCase()),
              testCaseId + ": expected banner text containing '" + expectedError + "' was not found.");
        }
        break;

      case "fielderror":
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/profile"),
            testCaseId + ": form should not have submitted.");

        if (!expectedError.isEmpty()) {
          String pageSource = driver.getPageSource().toLowerCase();
          Assert.assertTrue(
              pageSource.contains(expectedError.toLowerCase()),
              testCaseId + ": expected field error containing '" + expectedError + "' was not found.");
        }
        break;

      default:
        Assert.fail(testCaseId + ": unknown expectedOutcome '" + expectedOutcome + "'");
    }
  }

  //Static Display & Navigation Tests 
  @Test
  public void testProfileNavigation_A010() {
    System.out.println("Running: TC-FE-A010 — Clicking Profile Picture Navigates to Profile");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));
    loginPage.enterEmail("valid@test.com");
    loginPage.enterPassword(ConfigReader.get("test.password"));
    loginPage.clickSignIn();

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.urlContains("/home"));

    driver.findElement(org.openqa.selenium.By.cssSelector(".avatar-circle")).click();

    wait.until(ExpectedConditions.urlContains("/profile"));
    Assert.assertTrue(driver.getCurrentUrl().contains("/profile"), "URL did not change to /profile");

    ProfilePage profilePage = new ProfilePage(driver);
    Assert.assertTrue(profilePage.getDisplayedEmail().length() > 0, "Profile fields are not visible");
  }

  // Cancel collapses the change-password form 
  @Test
  public void testCancelCollapsesPasswordForm_A049() {
    System.out.println("Running: TC-FE-A049 — Cancel Collapses Password Form");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));
    loginPage.enterEmail(ConfigReader.get("test.email"));
    loginPage.enterPassword(ConfigReader.get("test.password"));
    loginPage.clickSignIn();

    new WebDriverWait(driver, Duration.ofSeconds(10))
        .until(ExpectedConditions.urlContains("/home"));

    ProfilePage profilePage = new ProfilePage(driver);
    profilePage.open(ConfigReader.get("base.url"));

    if (profilePage.isPasswordFormVisible()) {
      profilePage.clickCancelPasswordForm();
      try { Thread.sleep(500); } catch (Exception ignored) {}
    }

    // Open the form
    profilePage.openChangePasswordForm();
    Assert.assertTrue(profilePage.isPasswordFormVisible(),
        "TC-FE-A049: Password form should be visible after clicking 'Change'");

    // Cancel it — allow more time for the form to collapse
    profilePage.clickCancelPasswordForm();
    try { Thread.sleep(1500); } catch (Exception ignored) {}
    Assert.assertFalse(profilePage.isPasswordFormVisible(),
        "TC-FE-A049: Password form should be hidden after clicking 'Cancel'");
  }

  // Upload valid avatar on profile page 
  @Test
  public void testAvatarUpload_A050() {
    System.out.println("Running: TC-FE-A050 — Profile Avatar Upload Success");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));
    loginPage.enterEmail(ConfigReader.get("test.email"));
    loginPage.enterPassword(ConfigReader.get("test.password"));
    loginPage.clickSignIn();

    new WebDriverWait(driver, Duration.ofSeconds(10))
        .until(ExpectedConditions.urlContains("/home"));

    ProfilePage profilePage = new ProfilePage(driver);
    profilePage.open(ConfigReader.get("base.url"));

    String avatarPath = new File("src/test/resources/testdata/avatars/valid.png").getAbsolutePath();
    profilePage.uploadNewAvatar(avatarPath);

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    boolean successShown = false;
    try {
      wait.until(d -> profilePage.isAvatarSuccessShown());
      successShown = true;
    } catch (Exception e) {
      successShown = false;
    }

    Assert.assertTrue(successShown,
        "TC-FE-A050: Expected avatar success banner after upload");
  }

  @Test
  public void testProfileDataDisplay_A011() {
    System.out.println("Running: TC-FE-A011 — Profile Page Displays Correct User Information");

    String expectedEmail = ConfigReader.get("test.email");
    String expectedFirstName = ConfigReader.get("test.firstName");
    String expectedLastName = ConfigReader.get("test.lastName");
    String password = ConfigReader.get("test.password");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));
    loginPage.enterEmail(expectedEmail);
    loginPage.enterPassword(password);
    loginPage.clickSignIn();

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.urlContains("/home"));

    ProfilePage profilePage = new ProfilePage(driver);
    profilePage.open(ConfigReader.get("base.url"));

    Assert.assertEquals(profilePage.getDisplayedEmail(), expectedEmail, "Header email mismatch");
    Assert.assertEquals(profilePage.getEmailValue(), expectedEmail, "Details card email mismatch");
    Assert.assertEquals(profilePage.getFirstNameValue(), expectedFirstName, "First name mismatch");
    Assert.assertEquals(profilePage.getLastNameValue(), expectedLastName, "Last name mismatch");
  }
}