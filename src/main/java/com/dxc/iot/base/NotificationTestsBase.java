package com.dxc.iot.base;

import com.dxc.iot.pages.LoginPage;
import com.dxc.iot.pages.SettingsPage;
import com.dxc.iot.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeSuite;

import java.time.Duration;

/**
 * Shared base for the notification test classes. Seeds guaranteed-breach
 * thresholds once per suite run (values match README "Environment Setup" step 5),
 * so notification tests no longer depend on SettingsTests running first,
 * suite XML ordering, or a human setting these by hand in the UI.
 *
 * Runs in its own short-lived WebDriver, separate from the per-test-method
 * driver BaseTest creates in @BeforeMethod, since @BeforeSuite fires before
 * any @BeforeMethod and before that field is initialized.
 */
public class NotificationTestsBase extends BaseTest {

  @BeforeSuite(alwaysRun = true)
  public void seedBreachThresholds() {
    ChromeOptions options = new ChromeOptions();
    if (isHeadless()) {
      options.addArguments("--headless=new");
      options.addArguments("--no-sandbox");
      options.addArguments("--disable-dev-shm-usage");
      options.addArguments("--window-size=1920,1080");
    }

    WebDriver seedDriver = new ChromeDriver(options);
    try {
      seedDriver.manage().timeouts().implicitlyWait(
          Duration.ofSeconds(Long.parseLong(ConfigReader.get("implicit.wait"))));

      String baseUrl = ConfigReader.get("base.url");
      String email = ConfigReader.get("test.email");
      String password = ConfigReader.get("test.password");

      // Pre-seed/signup the test accounts via backend API to handle clean/recreated database environments
      try {
          String backendUrl = System.getProperty("backend.url");
          if (backendUrl == null || backendUrl.isEmpty()) {
              if (baseUrl.contains("localhost:4200")) {
                  backendUrl = "http://localhost:8080";
              } else if (baseUrl.contains("localhost")) {
                  backendUrl = "http://localhost:8080";
              } else {
                  backendUrl = baseUrl.replace("frontend-route", "backend-route");
                  if (backendUrl.endsWith("/")) {
                      backendUrl = backendUrl.substring(0, backendUrl.length() - 1);
                  }
              }
          }
          System.out.println("Pre-seeding users at backend: " + backendUrl);
          java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

          // Sign up valid@test.com
          String validUserJson = "{\"email\":\"valid@test.com\",\"password\":\"Pass123#\",\"firstName\":\"John\",\"lastName\":\"Doe\"}";
          java.net.http.HttpRequest req1 = java.net.http.HttpRequest.newBuilder()
                  .uri(java.net.URI.create(backendUrl + "/api/auth/signup"))
                  .header("Content-Type", "application/json")
                  .POST(java.net.http.HttpRequest.BodyPublishers.ofString(validUserJson))
                  .build();
          client.send(req1, java.net.http.HttpResponse.BodyHandlers.discarding());

          // Sign up lockout@test.com
          String lockoutUserJson = "{\"email\":\"lockout@test.com\",\"password\":\"LockoutPass123@\",\"firstName\":\"Lock\",\"lastName\":\"Out\"}";
          java.net.http.HttpRequest req2 = java.net.http.HttpRequest.newBuilder()
                  .uri(java.net.URI.create(backendUrl + "/api/auth/signup"))
                  .header("Content-Type", "application/json")
                  .POST(java.net.http.HttpRequest.BodyPublishers.ofString(lockoutUserJson))
                  .build();
          client.send(req2, java.net.http.HttpResponse.BodyHandlers.discarding());
          System.out.println("Test users registered/verified successfully.");
      } catch (Exception e) {
          System.out.println("Warning: test user seeding API call: " + e.getMessage());
      }

      LoginPage loginPage = new LoginPage(seedDriver);
      loginPage.open(baseUrl);
      loginPage.signIn(email != null ? email : "valid@test.com",
          password != null ? password : "Pass123#");
      new WebDriverWait(seedDriver, Duration.ofSeconds(10))
          .until(ExpectedConditions.urlContains("/home"));

      SettingsPage settings = new SettingsPage(seedDriver);
      settings.open(baseUrl);
      WebDriverWait wait = new WebDriverWait(seedDriver, Duration.ofSeconds(20));

      settings.clearAllInputsInSection(SettingsPage.SECTION_TRAFFIC);
      settings.setUpperThreshold(SettingsPage.SECTION_TRAFFIC,
          SettingsPage.METRIC_TRAFFIC_DENSITY, "1");
      settings.saveSection("Traffic Settings");
      wait.until(d -> settings.isSuccessShownFor(SettingsPage.SECTION_TRAFFIC));

      // Brightness uses the LOWER threshold: readings normally sit well below 99%,
      // so a lower bound of 99 guarantees a "below the limit" breach. Power
      // Consumption uses the UPPER threshold like the other low-value metrics below.
      settings.clearAllInputsInSection(SettingsPage.SECTION_STREET_LIGHTS);
      settings.setLowerThreshold(SettingsPage.SECTION_STREET_LIGHTS,
          SettingsPage.METRIC_BRIGHTNESS_LEVEL, "99");
      settings.setUpperThreshold(SettingsPage.SECTION_STREET_LIGHTS,
          SettingsPage.METRIC_POWER_CONSUMPTION, "1");
      settings.saveSection("Street Lights Settings");
      wait.until(d -> settings.isSuccessShownFor(SettingsPage.SECTION_STREET_LIGHTS));

      settings.clearAllInputsInSection(SettingsPage.SECTION_AIR_POLLUTION);
      settings.setUpperThreshold(SettingsPage.SECTION_AIR_POLLUTION,
          SettingsPage.METRIC_CO_LEVEL, "1");
      settings.setUpperThreshold(SettingsPage.SECTION_AIR_POLLUTION,
          SettingsPage.METRIC_OZONE, "0.001");
      settings.saveSection("Air Pollution Settings");
      wait.until(d -> settings.isSuccessShownFor(SettingsPage.SECTION_AIR_POLLUTION));

    } finally {
      seedDriver.quit();
    }
  }

  private boolean isHeadless() {
    String value = System.getProperty("headless", ConfigReader.get("headless"));
    return "true".equalsIgnoreCase(value);
  }
}
