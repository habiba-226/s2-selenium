package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.EntryPointPage;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.utils.ConfigReader;
import io.qameta.allure.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

@Epic("Sprint 3 — Traffic Monitoring")
@Feature("F#6 — Entry Point / Navigation Page")
public class EntryPointTests extends BaseTest {

    private EntryPointPage loginAndOpenHome() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.get("base.url"));
        String email = ConfigReader.get("test.email");
        String pass  = ConfigReader.get("test.password");
        loginPage.enterEmail(email != null ? email : "valid@test.com");
        loginPage.enterPassword(pass  != null ? pass  : "Pass@123");
        loginPage.clickSignIn();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/home"));
        return new EntryPointPage(driver);
    }

    @Test
    @Story("Three dashboard cards visible on /home")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Asserts Traffic, Air Pollution, and Street Light cards all visible after login.")
    public void testHomePageShowsThreeDashboardCards_EP001() {
        System.out.println("Running: EP-001");
        EntryPointPage home = loginAndOpenHome();
        Assert.assertTrue(home.isTrafficCardDisplayed(),
                "EP-001: Traffic card not displayed");
        Assert.assertTrue(home.isAirPollutionCardDisplayed(),
                "EP-001: Air Pollution card not displayed");
        Assert.assertTrue(home.isStreetLightCardDisplayed(),
                "EP-001: Street Light card not displayed");
    }

    @Test
    @Story("Traffic card navigates to /traffic")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Clicks the Traffic card and asserts URL changes to /traffic.")
    public void testTrafficCardNavigatesToDashboard_EP002() {
        System.out.println("Running: EP-002");
        EntryPointPage home = loginAndOpenHome();
        home.clickTrafficCard();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/traffic"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/traffic"),
                "EP-002: URL did not contain /traffic. Got: " + driver.getCurrentUrl());
    }

    @Test
    @Story("Air Pollution card opens modal overlay")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Clicks the Air Pollution card and asserts a modal appears. URL stays /home.")
    public void testAirPollutionCardOpensModal_EP003() {
        System.out.println("Running: EP-003");
        EntryPointPage home = loginAndOpenHome();
        home.clickAirPollutionCard();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        Assert.assertTrue(home.isModalVisible(),
                "EP-003: modal did not appear after clicking Air Pollution card");
        Assert.assertTrue(driver.getCurrentUrl().contains("/home"),
                "EP-003: URL changed unexpectedly. Got: " + driver.getCurrentUrl());
    }

    @Test
    @Story("Street Light card opens modal overlay")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Clicks the Street Light card and asserts a modal appears. URL stays /home.")
    public void testStreetLightCardOpensModal_EP004() {
        System.out.println("Running: EP-004");
        EntryPointPage home = loginAndOpenHome();
        home.clickStreetLightCard();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        Assert.assertTrue(home.isModalVisible(),
                "EP-004: modal did not appear after clicking Street Light card");
        Assert.assertTrue(driver.getCurrentUrl().contains("/home"),
                "EP-004: URL changed unexpectedly. Got: " + driver.getCurrentUrl());
    }

    @Test
    @Story("Back navigation returns to /home with cards intact")
    @Severity(SeverityLevel.NORMAL)
    @Description("Clicks Traffic card, browser Back, asserts all 3 cards still present.")
    public void testBackNavigationReturnsToHome_EP005() {
        System.out.println("Running: EP-005");
        EntryPointPage home = loginAndOpenHome();
        home.clickTrafficCard();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/traffic"));
        driver.navigate().back();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/home"));
        EntryPointPage homeAgain = new EntryPointPage(driver);
        Assert.assertTrue(homeAgain.isTrafficCardDisplayed(),
                "EP-005: Traffic card missing after back nav");
        Assert.assertTrue(homeAgain.isAirPollutionCardDisplayed(),
                "EP-005: Air Pollution card missing after back nav");
        Assert.assertTrue(homeAgain.isStreetLightCardDisplayed(),
                "EP-005: Street Light card missing after back nav");
    }

    @Test
    @Story("Modal closes when X button clicked")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Opens Air Pollution modal then clicks the close button. Asserts modal disappears.")
    public void testModalClosesOnXButton_EP006() {
        System.out.println("Running: EP-006");
        EntryPointPage home = loginAndOpenHome();
        home.clickAirPollutionCard();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        Assert.assertTrue(home.isModalVisible(),
                "EP-006: modal did not open");
        home.clickModalCloseButton();
        try { Thread.sleep(700); } catch (Exception ignored) {}
        Assert.assertFalse(home.isModalVisible(),
                "EP-006: modal still visible after clicking X");
    }

    @Test
    @Story("Modal closes when clicking outside it")
    @Severity(SeverityLevel.NORMAL)
    @Description("Opens Street Light modal then clicks outside it. Asserts modal disappears.")
    public void testModalClosesOnOutsideClick_EP007() {
        System.out.println("Running: EP-007");
        EntryPointPage home = loginAndOpenHome();
        home.clickStreetLightCard();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        Assert.assertTrue(home.isModalVisible(),
                "EP-007: modal did not open");
        home.clickOutsideModal();
        try { Thread.sleep(700); } catch (Exception ignored) {}
        Assert.assertFalse(home.isModalVisible(),
                "EP-007: modal still visible after outside click");
    }
}