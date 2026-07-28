package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.TrafficDashboardPage;
import com.dxc.iot.pages.NotificationPage;
import com.dxc.iot.utils.ConfigReader;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

@Epic("End-to-End Tests")
@Feature("E2E Flows")
public class EndToEndTests extends BaseTest {

    @Test
    @Story("Login, navigate to traffic, sort density, and view notifications")
    @Severity(SeverityLevel.BLOCKER)
    @Description("E2E flow: login -> traffic dashboard -> sort density -> notification verification")
    public void testLoginToTrafficDashboardToNotifications_E2E001() {
        System.out.println("Running E2E-001");
        String baseUrl = ConfigReader.get("base.url");

        // 1. Login
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl);
        loginPage.signIn("valid@test.com", "Pass123#");
        try { Thread.sleep(5000); } catch (Exception ignored) {}

        // 2. Open Traffic Dashboard
        TrafficDashboardPage trafficPage = new TrafficDashboardPage(driver);
        trafficPage.open(baseUrl);
        try { Thread.sleep(2000); } catch (Exception ignored) {}
        Assert.assertTrue(trafficPage.isTrafficTableDisplayed(), "E2E: traffic table not displayed");

        // 3. Click Traffic Density Header to sort
        List<Integer> beforeSort = trafficPage.getVisibleDensityValues();
        trafficPage.clickTrafficDensityHeader();
        try { Thread.sleep(2000); } catch (Exception ignored) {}
        List<Integer> afterSort = trafficPage.getVisibleDensityValues();
        System.out.println("Density values before sort: " + beforeSort);
        System.out.println("Density values after sort: " + afterSort);

        // 4. Verify Notifications are active
        NotificationPage notificationPage = new NotificationPage(driver);
        Assert.assertTrue(notificationPage.isBellVisible(), "E2E: bell icon not visible");
        notificationPage.openNotifications();
        Assert.assertTrue(notificationPage.waitForNotificationsToAppear(), "E2E: notifications did not load");
        int count = notificationPage.getNotificationCount();
        System.out.println("E2E: found " + count + " active alerts/notifications");
        Assert.assertTrue(count >= 0, "E2E: invalid notification count");
    }
}
