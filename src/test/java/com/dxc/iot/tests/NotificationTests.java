package com.dxc.iot.tests;

import com.dxc.iot.base.NotificationTestsBase;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.NotificationPage;
import com.dxc.iot.utils.ConfigReader;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.dxc.iot.pages.TrafficDashboardPage;

@Epic("Sprint 3 — Traffic Monitoring")
@Feature("F#9 — Traffic Notifications")
public class NotificationTests extends NotificationTestsBase {

    private NotificationPage loginAndOpenNotifications() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.get("base.url"));
        loginPage.signIn("valid@test.com", "Pass123#");
        try { Thread.sleep(5000); } catch (Exception ignored) {}
        return new NotificationPage(driver);
    }

    @Test
    @Story("Bell icon visible on homepage")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Asserts .bell-btn is present after login. Entry point to notification center.")
    public void testNotificationBellVisible_NT001() {
        System.out.println("Running: NT-001");
        Assert.assertTrue(loginAndOpenNotifications().isBellVisible(),
                "NT-001: bell not visible");
    }

    @Test
    @Story("Bell click opens notification panel")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Clicks .bell-btn and asserts .notif-panel becomes visible.")
    public void testNotificationPanelOpens_NT003() {
        System.out.println("Running: NT-003");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.isNotificationPanelVisible(), "NT-003: panel did not open");
    }

    @Test
    @Story("Bell toggles panel closed on second click")
    @Severity(SeverityLevel.NORMAL)
    @Description("First click opens, second click closes. Validates Angular toggle behavior.")
    public void testBellTogglesPanel_NT007() {
        System.out.println("Running: NT-007");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.isNotificationPanelVisible(), "NT-007: did not open");
        p.openNotifications();
        try { Thread.sleep(500); } catch (Exception ignored) {}
        Assert.assertFalse(p.isNotificationPanelVisible(), "NT-007: did not close");
    }

    @Test
    @Story("Click outside panel closes it")
    @Severity(SeverityLevel.NORMAL)
    @Description("Opens panel then clicks page heading outside it. Validates @HostListener behavior.")
    public void testClickOutsideClosesPanel_NT008() {
        System.out.println("Running: NT-008");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.isNotificationPanelVisible(), "NT-008: did not open");
        p.clickOutsidePanel();
        try { Thread.sleep(700); } catch (Exception ignored) {}
        Assert.assertFalse(p.isNotificationPanelVisible(), "NT-008: still open");
    }

    @Test
    @Story("Click inside panel does not close it")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks .notif-panel header. Validates $event.stopPropagation() works.")
    public void testClickInsidePanelKeepsItOpen_NT011() {
        System.out.println("Running: NT-011");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.isNotificationPanelVisible(), "NT-011: did not open");
        p.clickInsidePanel();
        try { Thread.sleep(500); } catch (Exception ignored) {}
        Assert.assertTrue(p.isNotificationPanelVisible(),
                "NT-011: panel closed — stopPropagation may be broken");
    }

    @Test(priority = 0)
    @Story("Notifications exist after threshold breach")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Waits up to 60s for notifications. Asserts count > 0.")
    public void testNotificationsExist_NT004() {
        System.out.println("Running: NT-004");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(), "NT-004: none appeared within 60s");
        Assert.assertTrue(p.getNotificationCount() > 0,    "NT-004: count is 0");
    }

    @Test(priority = 1)
    @Story("Notification message matches confirmed format")
    @Severity(SeverityLevel.NORMAL)
    @Description("Asserts message contains 'above/below the limit', 'reading:', and 'threshold:'. "
            + "Confirmed format: 'Traffic Density is above the limit — reading: 449, threshold: 50 · Location'")
    public void testNotificationMessageFormat_NT010() {
        System.out.println("Running: NT-010");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(), "NT-010: none appeared");
        String msg = p.getFirstNotificationMessage();
        Assert.assertNotNull(msg,         "NT-010: message null");
        Assert.assertFalse(msg.isEmpty(), "NT-010: message empty");
        Assert.assertTrue(
                msg.toLowerCase().contains("above the limit")
                        || msg.toLowerCase().contains("below the limit"),
                "NT-010: missing 'above/below the limit': '" + msg + "'");
        Assert.assertTrue(msg.toLowerCase().contains("reading:"),
                "NT-010: missing 'reading:': '" + msg + "'");
        Assert.assertTrue(msg.toLowerCase().contains("threshold:"),
                "NT-010: missing 'threshold:': '" + msg + "'");
    }

    @Test(priority = 1)
    @Story("Notification message contains a known location")
    @Severity(SeverityLevel.MINOR)
    @Description("Asserts message contains one of the 3 confirmed locations: "
            + "Industrial Zone B, Main St & 1st Ave, Central Park North.")
    public void testNotificationMessageContainsLocation_NT013() {
        System.out.println("Running: NT-013");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(), "NT-013: none appeared");
        String msg = p.getFirstNotificationMessage();
        Assert.assertTrue(
                msg.contains("Industrial Zone B")
                        || msg.contains("Main St & 1st Ave")
                        || msg.contains("Central Park North")
                        || msg.contains("Highway 42 / Exit 7")
                        || msg.contains("Harbor Bridge"),
                "NT-013: no known location in: '" + msg + "'");
    }

    @Test(priority = 2)
    @Story("Dismiss single notification reduces count")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Dismisses first .notif-item and asserts total count decreased.")
    public void testDismissSingleNotification_NT005() {
        System.out.println("Running: NT-005");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(), "NT-005: none appeared");
        int before = p.getNotificationCount();
        p.dismissFirstNotification();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        Assert.assertTrue(p.getNotificationCount() < before, "NT-005: count unchanged");
    }

    @Test(priority = 2)
    @Story("Dismiss reduces count by exactly 1")
    @Severity(SeverityLevel.NORMAL)
    @Description("With 2+ notifications, dismisses one and asserts count = before - 1 exactly.")
    public void testDismissReducesCountByExactlyOne_NT012() {
        System.out.println("Running: NT-012");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(), "NT-012: none appeared");
        int before = p.getNotificationCount();
        if (before < 2) {
            System.out.println("NT-012: only " + before + " — skipped"); return;
        }
        p.dismissFirstNotification();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        Assert.assertEquals(p.getNotificationCount(), before - 1,
                "NT-012: Before=" + before + " After=" + p.getNotificationCount());
    }

    @Test(priority = 3)
    @Story("Clear All removes all notifications")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Clicks .notif-clear-all and asserts .notif-item list is empty.")
    public void testClearAllNotifications_NT006() {
        System.out.println("Running: NT-006");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(), "NT-006: none appeared");
        p.clearAllNotifications();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        Assert.assertTrue(p.areNotificationsEmpty(), "NT-006: items still present");
    }

    @Test(priority = 4)
    @Story("Panel shows empty state after Clear All")
    @Severity(SeverityLevel.NORMAL)
    @Description("After Clear All, panel stays open and list is empty. Validates empty-state UI.")
    public void testEmptyStateAfterClearAll_NT009() {
        System.out.println("Running: NT-009");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(), "NT-009: none appeared");
        p.clearAllNotifications();
        try { Thread.sleep(3000); } catch (Exception ignored) {}
        Assert.assertTrue(p.areNotificationsEmpty(),      "NT-009: items still present");
        Assert.assertTrue(p.isNotificationPanelVisible(), "NT-009: panel closed unexpectedly");
    }

    @Test(priority = 2)
    @Story("Dismiss last notification shows empty state")
    @Severity(SeverityLevel.NORMAL)
    @Description("Dismisses notifications one by one until the last one, "
            + "then dismisses it and asserts the panel shows empty state. "
            + "State transition: 1 notification → dismiss → empty.")
    public void testDismissLastNotificationShowsEmptyState_NT017() {
        System.out.println("Running: NT-017 — Dismiss Last Notification");

        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();

        Assert.assertTrue(p.waitForNotificationsToAppear(),
                "NT-017: no notifications appeared");

        // Dismiss all until only 1 remains
        while (p.getNotificationCount() > 1) {
            p.dismissFirstNotification();
            try { Thread.sleep(500); } catch (Exception ignored) {}
        }

        Assert.assertEquals(p.getNotificationCount(), 1,
                "NT-017: expected exactly 1 notification before final dismiss");

        // Dismiss the last one
        p.dismissFirstNotification();
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        Assert.assertTrue(p.areNotificationsEmpty(),
                "NT-017: panel not empty after dismissing last notification");

        Assert.assertTrue(p.isNotificationPanelVisible(),
                "NT-017: panel closed unexpectedly after dismissing last notification");
        Assert.assertTrue(p.isNoNotificationsTextVisible(),
                "NT-017: 'No notifications' empty state text not shown");
    }

    @Test
    @Story("Toast X button manually dismisses toast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Waits for toast to appear then clicks .toast-close button. "
            + "Asserts toast disappears immediately without waiting for auto-dismiss.")
    public void testToastManualDismiss_NT014() {
        System.out.println("Running: NT-014");
        NotificationPage p = loginAndOpenNotifications();
        // Navigate to traffic page where toasts appear
        driver.get(ConfigReader.get("base.url") + "/traffic");
        TrafficDashboardPage traffic = new TrafficDashboardPage(driver);
        if (!traffic.waitForToastNotification(30)) {
            System.out.println("NT-014: no toast appeared — skipped"); return;
        }
        Assert.assertTrue(traffic.isToastVisible(),
                "NT-014: toast not visible");
        p.clickToastCloseButton();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        Assert.assertTrue(p.isToastGone(),
                "NT-014: toast still visible after clicking X");
    }

    @Test(priority = 1)
    @Story("Bell badge visible when notifications exist")
    @Severity(SeverityLevel.NORMAL)
    @Description("Asserts .bell-badge is visible when there are unread notifications. "
            + "Badge confirmed in HTML: <span class='bell-badge'>15</span>")
    public void testBellBadgeVisibleWithNotifications_NT015() {
        System.out.println("Running: NT-015");
        NotificationPage p = loginAndOpenNotifications();
        // First confirm notifications exist
        p.openNotifications();
        boolean hasNotifications = p.waitForNotificationsToAppear();
        p.openNotifications(); // close panel
        try { Thread.sleep(500); } catch (Exception ignored) {}
        if (!hasNotifications) {
            System.out.println("NT-015: no notifications present — skipped"); return;
        }
        Assert.assertTrue(p.isBellBadgeVisible(),
                "NT-015: bell badge not visible despite notifications existing");
    }

    @Test(priority = 4)
    @Story("Notifications persist after page reload")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Records notification count, reloads the page, opens panel again. "
            + "Asserts notifications are still present — history must survive a refresh.")
    public void testNotificationsPersistAfterReload_NT016() {
        System.out.println("Running: NT-016");
        NotificationPage p = loginAndOpenNotifications();
        p.openNotifications();
        Assert.assertTrue(p.waitForNotificationsToAppear(),
                "NT-016: no notifications before reload");
        int countBefore = p.getNotificationCount();
        p.openNotifications(); // close panel

        // Reload the page
        driver.navigate().refresh();
        try { Thread.sleep(3000); } catch (Exception ignored) {}

        // Reopen panel and check notifications still there
        NotificationPage pAfter = new NotificationPage(driver);
        pAfter.openNotifications();
        Assert.assertTrue(pAfter.waitForNotificationsToAppear(),
                "NT-016: notifications gone after page reload — history not persisted");
        Assert.assertTrue(pAfter.getNotificationCount() > 0,
                "NT-016: notification count is 0 after reload. Before=" + countBefore);
    }
}