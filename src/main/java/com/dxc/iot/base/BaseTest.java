package com.dxc.iot.base;

import org.testng.ITestResult;
import com.dxc.iot.utils.ScreenshotUtils;
import com.dxc.iot.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public class BaseTest {
  protected WebDriver driver;

  @BeforeMethod
  public void setUp() {
    ChromeOptions options = new ChromeOptions();
    if (isHeadless()) {
      options.addArguments("--headless=new");
      options.addArguments("--no-sandbox");
      options.addArguments("--disable-dev-shm-usage");
      options.addArguments("--window-size=1920,1080");
    }
    driver = new ChromeDriver(options);
    if (!isHeadless()) {
      driver.manage().window().maximize();
    }
    driver.manage().timeouts().implicitlyWait(
        Duration.ofSeconds(Long.parseLong(ConfigReader.get("implicit.wait"))));
    driver.get(ConfigReader.get("base.url"));
  }

  /*
   * Headless mode defaults to off so local runs keep using a normal windowed
   * browser. CI enables it with -Dheadless=true, or a headless=true entry in
   * config.properties.
   */
  private boolean isHeadless() {
    String value = System.getProperty("headless", ConfigReader.get("headless"));
    return "true".equalsIgnoreCase(value);
  }

  @AfterMethod
  public void tearDown(ITestResult result) {
    if (driver != null) {
      // If the test case failed, snap a picture automatically
      if (result.getStatus() == ITestResult.FAILURE) {
        System.out.println("Test Failed! Snapping a screenshot...");
        ScreenshotUtils.capture(driver, result.getName() + "_FAILURE");
      }
      driver.quit();
    }
  }

  /*
   * Safely reads one cell from an Excel data row. Returns empty string if null.
   */
  protected String cell(Object[] row, int index) {
    if (index >= row.length || row[index] == null)
      return "";
    return row[index].toString().trim();
  }
}