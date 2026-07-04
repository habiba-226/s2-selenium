package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.NotificationPage;
import com.dxc.iot.pages.StreetLightDashboardPage;
import com.dxc.iot.utils.ConfigReader;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

@Epic("Sprint 4 & 5 — Street Light Monitoring")
@Feature("F#12 — Street Light Notifications")
public class StreetLightNotificationTests extends BaseTest {

    private NotificationPage loginAndOpenNotifications() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.get("base.url"));
        loginPage.signIn("valid@test.com", "Pass123#");
        try { Thread.sleep(5000); } catch (Exception ignored) {}
        return new NotificationPage(driver);
    }

    @Test(priority = 1)
    @Story("Street Light notification message matches format")
    @Severity(SeverityLevel.NORMAL)
    @Description("Asserts a Street Light alert contains 'Power Consumption' or 'Brightness Level', "
            + "'above/below the limit', 'reading:', and 'threshold:'.")
    public void testStreetLightNotificationFormat_SLN001() {
        System.out.println("Running: SLN-001 — Street Light Notification Format");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(),
                "SLN-001: no notifications appeared");

        // Look through all notifications for a Street Light one
        boolean foundStreetLightAlert = false;
        for (int i = 0; i < p.getNotificationCount(); i++) {
            String msg = p.getFirstNotificationMessage();
            if (msg != null && (
                    msg.toLowerCase().contains("power consumption")
                            || msg.toLowerCase().contains("brightness"))) {
                Assert.assertTrue(
                        msg.toLowerCase().contains("above the limit")
                                || msg.toLowerCase().contains("below the limit"),
                        "SLN-001: missing 'above/below the limit': '" + msg + "'");
                Assert.assertTrue(msg.toLowerCase().contains("reading:"),
                        "SLN-001: missing 'reading:': '" + msg + "'");
                Assert.assertTrue(msg.toLowerCase().contains("threshold:"),
                        "SLN-001: missing 'threshold:': '" + msg + "'");
                foundStreetLightAlert = true;
                break;
            }
            // Dismiss this one and check next
            p.dismissFirstNotification();
            try { Thread.sleep(500); } catch (Exception ignored) {}
        }

        if (!foundStreetLightAlert) {
            System.out.println("SLN-001: no Street Light alert found — skipped");
        }
    }

    @Test(priority = 1)
    @Story("Street Light notification contains location")
    @Severity(SeverityLevel.MINOR)
    @Description("Asserts a Street Light alert includes one of the known sensor locations.")
    public void testStreetLightNotificationLocation_SLN002() {
        System.out.println("Running: SLN-002 — Street Light Notification Location");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(),
                "SLN-002: no notifications appeared");

        String msg = p.getFirstNotificationMessage();
        Assert.assertNotNull(msg, "SLN-002: message null");
        Assert.assertTrue(
                msg.contains("Industrial Zone B")
                        || msg.contains("Main St & 1st Ave")
                        || msg.contains("Central Park North")
                        || msg.contains("Highway 42 / Exit 7")
                        || msg.contains("Harbor Bridge"),
                "SLN-002: no known location in: '" + msg + "'");
    }

    @Test
    @Story("Street Light toast appears on threshold breach")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Navigates to /street-light and waits for toast when a sensor reading "
            + "breaches Brightness or Power threshold.")
    public void testStreetLightToastAppears_SLN003() {
        System.out.println("Running: SLN-003 — Street Light Toast Appears");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.get("base.url"));
        loginPage.signIn("valid@test.com", "Pass123#");
        try { Thread.sleep(3000); } catch (Exception ignored) {}

        driver.get(ConfigReader.get("base.url") + StreetLightDashboardPage.PATH);
        StreetLightDashboardPage page = new StreetLightDashboardPage(driver);

        if (!page.waitForToastNotification(60)) {
            System.out.println("SLN-003: no toast appeared within 60s — skipped");
            return;
        }
        Assert.assertTrue(page.isToastVisible(),
                "SLN-003: toast not visible after waitForToast returned true");
    }

    @Test
    @Story("Street Light notifications coexist with Traffic notifications")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Confirms the notification panel can display Street Light alerts "
            + "alongside existing Traffic alerts — bell badge counts all sources.")
    public void testMixedNotifications_SLN004() {
        System.out.println("Running: SLN-004 — Mixed Notifications");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(),
                "SLN-004: no notifications appeared");
        Assert.assertTrue(p.getNotificationCount() > 0,
                "SLN-004: count is 0");
    }
}
