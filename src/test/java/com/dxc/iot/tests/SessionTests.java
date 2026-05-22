package com.dxc.iot.tests;

import com.dxc.iot.base.BaseTest;
import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.ProfilePage;
import com.dxc.iot.pages.SettingsPage;
import com.dxc.iot.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class SessionTests extends BaseTest {

        @Test
        public void testBUG_LocalStoragePersists_A031() {
                System.out.println("--- Running TC-FE-A031: LocalStorage Persistence ---");

                LoginPage loginPage = new LoginPage(driver);
                loginPage.open(ConfigReader.get("base.url"));
                loginPage.enterEmail(ConfigReader.get("test.email"));
                loginPage.enterPassword(ConfigReader.get("test.password"));
                loginPage.clickSignIn();

                new WebDriverWait(driver, Duration.ofSeconds(10))
                                .until(ExpectedConditions.urlContains("/home"));

                JavascriptExecutor js = (JavascriptExecutor) driver;
                String localStorageToken = (String) js.executeScript(
                                "return window.localStorage.getItem('token');");
                System.out.println("LocalStorage Token after login: " + localStorageToken);

                // SIMULATE BROWSER CLOSE:
                //Delete cookies
                driver.manage().deleteAllCookies();

                // Clear sessionStorage (Because Selenium doesn't actually close the physical tab)
                // NOTE: We do NOT clear localStorage here. If the bug still exists, the app will illegally rebuild the session from localStorage
                js.executeScript("window.sessionStorage.clear();");

                // Force Angular to drop memory state
                js.executeScript("window.location.href = 'about:blank';");

                // Now navigate back to the app base URL (mimics a true cold start)
                driver.get(ConfigReader.get("base.url"));

                try {
                        Thread.sleep(2000);
                } catch (Exception ignored) {
                }

                String urlAfterReopen = driver.getCurrentUrl();
                boolean isLoggedOut = urlAfterReopen.contains("/login");

                if (!isLoggedOut) {
                        System.out.println("BUG CONFIRMED: URL after cookie clear = " + urlAfterReopen);
                        System.out.println("Token was still in localStorage: " + localStorageToken);
                }

                Assert.assertTrue(isLoggedOut,
                                "TC-FE-A031 BUG CONFIRMED: After clearing cookies (simulating browser close), " +
                                                "app went to '" + urlAfterReopen + "' instead of /login. " +
                                                "localStorage token persisted. App should use sessionStorage instead.");
        }

        @Test
        public void testUnauthorizedAccessToHome_A032() {
                System.out.println("--- Running TC-FE-A032: AuthGuard on /home ---");

                driver.get(ConfigReader.get("base.url") + "/home");

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                boolean redirectedToLogin = wait.until(ExpectedConditions.urlContains("/login"));

                Assert.assertTrue(redirectedToLogin,
                                "TC-FE-A032 FAILED: Unauthorized user accessed /home without redirect.");
        }

        @Test
        public void testUnauthorizedAccessToProfile_A033() {
                System.out.println("--- Running TC-FE-A033: AuthGuard on /profile ---");

                driver.get(ConfigReader.get("base.url") + "/profile");

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                boolean redirectedToLogin = wait.until(ExpectedConditions.urlContains("/login"));

                Assert.assertTrue(redirectedToLogin,
                                "TC-FE-A033 FAILED: Unauthorized user accessed /profile without redirect.");
        }

        @Test
        public void testLogoutClearsStorage_A034() {
                System.out.println("--- Running TC-FE-A034: Logout Cleanup ---");

                // open login page first
                LoginPage loginPage = new LoginPage(driver);
                loginPage.open(ConfigReader.get("base.url"));
                loginPage.enterEmail(ConfigReader.get("test.email"));
                loginPage.enterPassword(ConfigReader.get("test.password"));
                loginPage.clickSignIn();

                new WebDriverWait(driver, Duration.ofSeconds(10))
                                .until(ExpectedConditions.urlContains("/home"));

                driver.get(ConfigReader.get("base.url") + "/profile");
                new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.urlContains("/profile"));

                driver.findElement(By.cssSelector("button.logout-btn")).click();

                new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.urlContains("/login"));

                JavascriptExecutor js = (JavascriptExecutor) driver;
                String tokenAfterLogout = (String) js.executeScript(
                                "return window.localStorage.getItem('token');");
                String userAfterLogout = (String) js.executeScript(
                                "return window.localStorage.getItem('user');");

                Assert.assertNull(tokenAfterLogout,
                                "TC-FE-A034 FAILED: 'token' still in localStorage after logout.");
                Assert.assertNull(userAfterLogout,
                                "TC-FE-A034 FAILED: 'user' still in localStorage after logout.");
        }

        // Sidebar nav links work from Profile page 
        @Test
        public void testSidebarNavFromProfile_A064() {
                System.out.println("--- Running TC-FE-A064: Sidebar Navigation from /profile ---");

                LoginPage loginPage = new LoginPage(driver);
                loginPage.open(ConfigReader.get("base.url"));
                loginPage.enterEmail(ConfigReader.get("test.email"));
                loginPage.enterPassword(ConfigReader.get("test.password"));
                loginPage.clickSignIn();

                new WebDriverWait(driver, Duration.ofSeconds(10))
                                .until(ExpectedConditions.urlContains("/home"));

                ProfilePage profilePage = new ProfilePage(driver);
                profilePage.open(ConfigReader.get("base.url"));

                // Dashboard link
                profilePage.clickSidebarDashboard();
                new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.urlContains("/home"));
                Assert.assertTrue(driver.getCurrentUrl().contains("/home"),
                                "TC-FE-A064: Dashboard link should navigate to /home");

                // Back to profile, then Settings link
                profilePage.open(ConfigReader.get("base.url"));
                profilePage.clickSidebarSettings();
                new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.urlContains("/settings"));
                Assert.assertTrue(driver.getCurrentUrl().contains("/settings"),
                                "TC-FE-A064: Settings link should navigate to /settings");
        }

        // Sidebar logout from Settings clears session
        @Test
        public void testSidebarLogoutFromSettings_A065() {
                System.out.println("--- Running TC-FE-A065: Logout from Settings Sidebar ---");

                LoginPage loginPage = new LoginPage(driver);
                loginPage.open(ConfigReader.get("base.url"));
                loginPage.enterEmail(ConfigReader.get("test.email"));
                loginPage.enterPassword(ConfigReader.get("test.password"));
                loginPage.clickSignIn();

                new WebDriverWait(driver, Duration.ofSeconds(10))
                                .until(ExpectedConditions.urlContains("/home"));

                SettingsPage settings = new SettingsPage(driver);
                settings.open(ConfigReader.get("base.url"));

                settings.clickLogout();

                new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.urlContains("/login"));

                JavascriptExecutor js = (JavascriptExecutor) driver;
                Assert.assertNull(js.executeScript("return window.localStorage.getItem('token');"),
                                "TC-FE-A065: 'token' should be cleared from localStorage after logout");
                Assert.assertNull(js.executeScript("return window.localStorage.getItem('user');"),
                                "TC-FE-A065: 'user' should be cleared from localStorage after logout");
        }

        // AuthGuard blocks unauthenticated access to /settings
        @Test
        public void testUnauthorizedAccessToSettings_A066() {
                System.out.println("--- Running TC-FE-A066: AuthGuard on /settings ---");

                driver.get(ConfigReader.get("base.url") + "/settings");

                boolean redirected = new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.urlContains("/login"));

                Assert.assertTrue(redirected,
                                "TC-FE-A066 FAILED: Unauthorized user accessed /settings without redirect.");
        }

        // noAuthGuard blocks logged-in user from /login 
        @Test
        public void testAuthenticatedUserBlockedFromLogin_A067() {
                System.out.println("--- Running TC-FE-A067: noAuthGuard on /login ---");

                LoginPage loginPage = new LoginPage(driver);
                loginPage.open(ConfigReader.get("base.url"));
                loginPage.enterEmail(ConfigReader.get("test.email"));
                loginPage.enterPassword(ConfigReader.get("test.password"));
                loginPage.clickSignIn();

                new WebDriverWait(driver, Duration.ofSeconds(10))
                                .until(ExpectedConditions.urlContains("/home"));

                driver.get(ConfigReader.get("base.url") + "/login");

                new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.urlContains("/home"));

                Assert.assertTrue(driver.getCurrentUrl().contains("/home"),
                                "TC-FE-A067 FAILED: Logged-in user should be redirected away from /login to /home");
        }

        // noAuthGuard blocks logged-in user from /signup

        @Test
        public void testAuthenticatedUserBlockedFromSignup_A068() {
                System.out.println("--- Running TC-FE-A068: noAuthGuard on /signup ---");

                LoginPage loginPage = new LoginPage(driver);
                loginPage.open(ConfigReader.get("base.url"));
                loginPage.enterEmail(ConfigReader.get("test.email"));
                loginPage.enterPassword(ConfigReader.get("test.password"));
                loginPage.clickSignIn();

                new WebDriverWait(driver, Duration.ofSeconds(10))
                                .until(ExpectedConditions.urlContains("/home"));

                driver.get(ConfigReader.get("base.url") + "/signup");

                new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.urlContains("/home"));

                Assert.assertTrue(driver.getCurrentUrl().contains("/home"),
                                "TC-FE-A068 FAILED: Logged-in user should be redirected away from /signup to /home");
        }
}