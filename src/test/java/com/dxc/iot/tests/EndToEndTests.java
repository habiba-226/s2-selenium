package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.HomePage;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.NotificationPage;
import com.dxc.iot.pages.TrafficDashboardPage;
import com.dxc.iot.utils.ConfigReader;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class EndToEndTests extends BaseTest {

  @Test
  public void testLoginToTrafficDashboardToNotifications_E2E001() {
    System.out.println("Running: E2E-001 — Login -> Traffic Dashboard -> Sort -> Home -> Notifications");

    String email = ConfigReader.get("test.email") != null ? ConfigReader.get("test.email") : "valid@test.com";
    String password = ConfigReader.get("test.password") != null ? ConfigReader.get("test.password") : "Pass123#";

    // 1. Login
    LoginPage loginPage = new LoginPage(driver);
    loginPage.open(ConfigReader.get("base.url"));
    loginPage.signIn(email, password);

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    wait.until(ExpectedConditions.urlContains("/home"));
    Assert.assertTrue(driver.getCurrentUrl().contains("/home"),
        "E2E-001: expected redirect to /home after login but was: " + driver.getCurrentUrl());

    // 2. Navigate to Traffic Dashboard
    TrafficDashboardPage trafficPage = new TrafficDashboardPage(driver).open(ConfigReader.get("base.url"));
    wait.until(ExpectedConditions.urlContains("/traffic"));
    Assert.assertTrue(trafficPage.isTrafficTableDisplayed(),
        "E2E-001: traffic table did not load");

    // 3. Apply a sort (Traffic Density header)
    String densityHeaderBefore = trafficPage.getTrafficDensityHeaderText();
    trafficPage.clickTrafficDensityHeader();
    try { Thread.sleep(1000); } catch (Exception ignored) {}
    Assert.assertNotEquals(trafficPage.getTrafficDensityHeaderText(), densityHeaderBefore,
        "E2E-001: sort indicator did not change after clicking Traffic Density header");

    // 4. Navigate back to Home (notifications panel is only accessible from the homepage)
    new HomePage(driver).open(ConfigReader.get("base.url"));
    wait.until(ExpectedConditions.urlContains("/home"));

    // 5. Open Notifications
    NotificationPage notificationPage = new NotificationPage(driver);
    notificationPage.openNotifications();
    Assert.assertTrue(notificationPage.waitForNotificationsToAppear(),
        "E2E-001: no notifications appeared within timeout");

    // 6. Confirm at least one notification is visible
    Assert.assertTrue(notificationPage.isNotificationPanelVisible(),
        "E2E-001: notification panel not visible");
    Assert.assertTrue(notificationPage.getNotificationCount() > 0,
        "E2E-001: expected at least one notification but found 0");
  }
}
