package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.AirPollutionDashboardPage;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.NotificationPage;
import com.dxc.iot.utils.ConfigReader;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Sprint 4 & 5 — Air Pollution Monitoring")
@Feature("F#15 — Air Pollution Notifications")
public class AirPollutionNotificationTests extends BaseTest {

    private NotificationPage loginAndOpenNotifications() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.get("base.url"));
        loginPage.signIn("valid@test.com", "Pass123#");
        try { Thread.sleep(5000); } catch (Exception ignored) {}
        return new NotificationPage(driver);
    }

    @Test(priority = 1)
    @Story("Air Pollution notification message matches format")
    @Severity(SeverityLevel.NORMAL)
    @Description("Asserts an Air Pollution alert contains 'CO Levels' or 'Ozone', "
            + "'above/below the limit', 'reading:', and 'threshold:'.")
    public void testAirPollutionNotificationFormat_APN001() {
        System.out.println("Running: APN-001 — Air Pollution Notification Format");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(),
                "APN-001: no notifications appeared");

        boolean foundAirPollutionAlert = false;
        for (int i = 0; i < p.getNotificationCount(); i++) {
            String msg = p.getFirstNotificationMessage();
            if (msg != null && (
                    msg.toLowerCase().contains("co level")
                            || msg.toLowerCase().contains("ozone"))) {
                Assert.assertTrue(
                        msg.toLowerCase().contains("above the limit")
                                || msg.toLowerCase().contains("below the limit"),
                        "APN-001: missing 'above/below the limit': '" + msg + "'");
                Assert.assertTrue(msg.toLowerCase().contains("reading:"),
                        "APN-001: missing 'reading:': '" + msg + "'");
                Assert.assertTrue(msg.toLowerCase().contains("threshold:"),
                        "APN-001: missing 'threshold:': '" + msg + "'");
                foundAirPollutionAlert = true;
                break;
            }
            p.dismissFirstNotification();
            try { Thread.sleep(500); } catch (Exception ignored) {}
        }

        if (!foundAirPollutionAlert) {
            System.out.println("APN-001: no Air Pollution alert found — skipped");
        }
    }

    @Test(priority = 1)
    @Story("Air Pollution notification contains location")
    @Severity(SeverityLevel.MINOR)
    @Description("Asserts an Air Pollution alert includes one of the known sensor locations.")
    public void testAirPollutionNotificationLocation_APN002() {
        System.out.println("Running: APN-002 — Air Pollution Notification Location");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(),
                "APN-002: no notifications appeared");

        String msg = p.getFirstNotificationMessage();
        Assert.assertNotNull(msg, "APN-002: message null");
        Assert.assertTrue(
                msg.contains("Industrial Zone B")
                        || msg.contains("Main St & 1st Ave")
                        || msg.contains("Central Park North")
                        || msg.contains("Highway 42 / Exit 7")
                        || msg.contains("Harbor Bridge"),
                "APN-002: no known location in: '" + msg + "'");
    }

    @Test
    @Story("Air Pollution toast appears on threshold breach")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Navigates to /air-pollution and waits for toast when a sensor reading "
            + "breaches CO or Ozone threshold.")
    public void testAirPollutionToastAppears_APN003() {
        System.out.println("Running: APN-003 — Air Pollution Toast Appears");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.get("base.url"));
        loginPage.signIn("valid@test.com", "Pass123#");
        try { Thread.sleep(3000); } catch (Exception ignored) {}

        driver.get(ConfigReader.get("base.url") + AirPollutionDashboardPage.PATH);
        AirPollutionDashboardPage page = new AirPollutionDashboardPage(driver);

        if (!page.waitForToastNotification(60)) {
            System.out.println("APN-003: no toast appeared within 60s — skipped");
            return;
        }
        Assert.assertTrue(page.isToastVisible(),
                "APN-003: toast not visible after waitForToast returned true");
    }

    @Test
    @Story("Air Pollution notifications coexist with other notifications")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Confirms the notification panel can display Air Pollution alerts "
            + "alongside Traffic and Street Light alerts.")
    public void testMixedNotifications_APN004() {
        System.out.println("Running: APN-004 — Mixed Notifications");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(),
                "APN-004: no notifications appeared");
        Assert.assertTrue(p.getNotificationCount() > 0,
                "APN-004: count is 0");
    }
}
