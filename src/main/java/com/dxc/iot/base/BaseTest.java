package com.dxc.iot.base;

import org.testng.ITestResult;
import com.dxc.iot.utils.ScreenshotUtils;
import com.dxc.iot.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public class BaseTest {
  protected WebDriver driver;

  @BeforeMethod
  public void setUp() {
    driver = new ChromeDriver();
    driver.manage().window().maximize();
    driver.manage().timeouts().implicitlyWait(
        Duration.ofSeconds(Long.parseLong(ConfigReader.get("implicit.wait"))));
    driver.get(ConfigReader.get("base.url"));
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