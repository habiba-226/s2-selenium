package com.dxc.iot.pages;

import com.dxc.iot.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static java.nio.file.Files.find;

public class NotificationPage extends BasePage {

    public NotificationPage(WebDriver driver) {
        super(driver);
    }

    private final By bellButton =
            By.cssSelector(".bell-btn");

    private final By bellBadge =
            By.cssSelector(".bell-badge");

    private final By notificationPanel =
            By.cssSelector(".notif-panel");

    private final By notificationItems =
            By.cssSelector(".notif-item");

    private final By notificationMessages =
            By.cssSelector(".notif-item-msg");

    private final By dismissButtons =
            By.cssSelector(".notif-dismiss");

    private final By clearAllButton =
            By.cssSelector(".notif-clear-all");

    public void openNotifications() {
        click(bellButton);
    }

    public boolean isBellVisible() {
        return isDisplayed(bellButton);
    }

    public boolean isUnreadBadgeVisible() {
        return isDisplayed(bellBadge);
    }

    public String getUnreadBadgeText() {
        return driver.findElement(bellBadge).getText().trim();
    }

    public boolean isNotificationPanelVisible() {
        return isDisplayed(notificationPanel);
    }

    public int getNotificationCount() {
        return driver.findElements(notificationItems).size();
    }

    public String getFirstNotificationMessage() {

        return driver.findElements(notificationMessages)
                .get(0)
                .getText()
                .trim();
    }

    public void dismissFirstNotification() {

        driver.findElements(dismissButtons)
                .get(0)
                .click();
    }

    public void clearAllNotifications() {
        click(clearAllButton);
    }

    public boolean areNotificationsEmpty() {

        return driver.findElements(notificationItems)
                .isEmpty();
    }

    public boolean waitForNotificationsToAppear() {

        try {

            WebDriverWait wait =
                    new WebDriverWait(driver, Duration.ofSeconds(120));

            wait.until(driver ->
                    !driver.findElements(notificationItems).isEmpty());

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    public boolean isBellBadgeVisible() {
        return driver.findElements(
                By.cssSelector(".bell-badge")).size() > 0
                && driver.findElement(
                By.cssSelector(".bell-badge")).isDisplayed();
    }

    public void clickToastCloseButton() {
        try {
            driver.findElement(By.cssSelector(".toast-close")).click();
        } catch (Exception e) {}
    }

    public boolean isToastGone() {
        return driver.findElements(
                By.cssSelector(".toast-item")).isEmpty();
    }

    private final By noNotificationsText =
            By.xpath("//*[contains(text(),'No notifications')]");

    public boolean isNoNotificationsTextVisible() {
        return driver.findElements(noNotificationsText).size() > 0
                && driver.findElement(noNotificationsText).isDisplayed();
    }

    private final By outsideClickTarget =
            By.cssSelector("h1, h2, .welcome-title, body");

    private final By insidePanelClickable =
            By.cssSelector(".notif-panel h3, .notif-panel .notif-title, .notif-panel");

    private final By anyElementInsidePanel =
            By.cssSelector(".notif-panel *");

    public void clickOutsidePanel() {
        try {
            driver.findElement(outsideClickTarget).click();
        } catch (Exception e) {
            new org.openqa.selenium.interactions.Actions(driver)
                    .moveByOffset(0, 0).click().perform();
        }
    }

    public void clickInsidePanel() {
        try {
            driver.findElement(insidePanelClickable).click();
        } catch (Exception e) {
            driver.findElements(anyElementInsidePanel)
                    .stream().findFirst()
                    .ifPresent(el -> { try { el.click(); } catch (Exception ignored) {} });
        }
    }
}