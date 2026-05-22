package com.dxc.iot.base;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {
  protected WebDriver driver;
  protected WebDriverWait wait;

  public BasePage(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
  }

  protected WebElement waitVisible(By locator) {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  protected void type(By locator, String text) {
    WebElement el = waitVisible(locator);
    el.clear();
    el.sendKeys(text);
  }

  protected void click(By locator) {
    wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
  }

  protected String getText(By locator) {
    return waitVisible(locator).getText();
  }

  protected boolean isDisplayed(By locator) {
    try {
      return waitVisible(locator).isDisplayed();
    } catch (Exception e) {
      return false;
    }
  }
}