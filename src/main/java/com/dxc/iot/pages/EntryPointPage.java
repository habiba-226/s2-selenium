package com.dxc.iot.pages;

import com.dxc.iot.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EntryPointPage extends BasePage {

    public static final String PATH = "/home";

    public EntryPointPage(WebDriver driver) {
        super(driver);
    }

    public EntryPointPage open(String baseUrl) {
        driver.get(baseUrl + PATH);
        return this;
    }

    // ===== DASHBOARD CARDS =====

    private final By allDashboardCards =
            By.cssSelector(".sensor-card, .dashboard-card, [class*='card']");

    private final By trafficCard =
            By.xpath("//*[contains(@class,'card') and .//*[contains(text(),'Traffic')]]");

    private final By airPollutionCard =
            By.xpath("//*[contains(@class,'card') and .//*[contains(text(),'Air Pollution')]]");

    private final By streetLightCard =
            By.xpath("//*[contains(@class,'card') and .//*[contains(text(),'Street Light')]]");

    // ===== ACTIONS =====

    public int getDashboardCardCount() {
        return driver.findElements(allDashboardCards).size();
    }

    public boolean isTrafficCardDisplayed() {
        return isDisplayed(trafficCard);
    }

    public boolean isAirPollutionCardDisplayed() {
        return isDisplayed(airPollutionCard);
    }

    public boolean isStreetLightCardDisplayed() {
        return isDisplayed(streetLightCard);
    }

    public void clickTrafficCard() {
        click(trafficCard);
    }

    public void clickAirPollutionCard() {
        click(airPollutionCard);
    }

    public void clickStreetLightCard() {
        click(streetLightCard);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    // ===== MODALS =====

    private final By airPollutionModal =
            By.cssSelector(".modal-overlay");

    private final By modalCloseButton =
            By.cssSelector(".modal-close, .close-btn, .popup-close, [class*='close']");

    public boolean isModalVisible() {
        try {
            return driver.findElements(airPollutionModal)
                    .stream()
                    .anyMatch(el -> el.isDisplayed());
        } catch (Exception e) {
            return false;
        }
    }

    public void closeModal() {
        try {
            driver.findElements(modalCloseButton)
                    .stream()
                    .filter(el -> el.isDisplayed())
                    .findFirst()
                    .ifPresent(el -> el.click());
        } catch (Exception e) {}
    }

    public void clickModalCloseButton() {
        try {
            WebElement closeBtn = driver.findElement(
                    By.cssSelector(".modal-close"));
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", closeBtn);
        } catch (Exception e) {
            try {
                // Fallback: try any visible close button
                driver.findElements(By.cssSelector("[class*='close']"))
                        .stream()
                        .filter(org.openqa.selenium.WebElement::isDisplayed)
                        .findFirst()
                        .ifPresent(el -> ((org.openqa.selenium.JavascriptExecutor) driver)
                                .executeScript("arguments[0].click();", el));
            } catch (Exception ignored) {}
        }
    }

    public void clickOutsideModal() {
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript(
                            "document.querySelector('.modal-overlay').click();");
        } catch (Exception ignored) {}
    }
}