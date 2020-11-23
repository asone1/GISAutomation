package autoTest;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class seleniumCommon {
	public static void takeSnapShot(WebDriver webdriver, String fileWithPath) throws Exception {

		// Convert web driver object to TakeScreenshot
		TakesScreenshot scrShot = ((TakesScreenshot) webdriver);

		// Call getScreenshotAs method to create image file
		File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);

		// Move image file to new destination
		File DestFile = new File(fileWithPath);

		// Copy file at destination
		FileUtils.copyFile(SrcFile, DestFile);
	}

	public static void clickPopUP(WebDriver driver) {
		for (WebElement e : driver.findElements(By.tagName("button"))) {
			if (e.isDisplayed()) {
				Actions act = new Actions(driver);
				act.moveToElement(e).click().perform();
			}
		}
	}

	/*
	 * onclick=
	 * "folder('adg','CollapsiblePanel1');toggleControl('none');chk_allpos(false);"
	 * 則輸入 js.executeScript(
	 * "folder('adg','CollapsiblePanel1');toggleControl('none');chk_allpos(false);")
	 * ;
	 */
	public static void exeJs(WebDriver driver, String jsCode) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript(jsCode);
	}

	@SuppressWarnings("deprecation")
	public static void waitAndClick(WebDriver driver, By by) {
		try {
			new WebDriverWait(driver, 30).until(ExpectedConditions.elementToBeClickable(by)).click();
		}catch(ElementClickInterceptedException e) {
			clickPopUP(driver);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static void jsClick(WebDriver driver, By by) {
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(by));
	}

	public static boolean mustDo(WebDriver driver, By by) {
		boolean result = false;
		int attempts = 0;
		while (attempts < 3) {
			try {
				waitAndClick(driver, by);
				result = true;
				break;
			} catch (StaleElementReferenceException | TimeoutException e) {
				e.printStackTrace();
			}
			attempts++;
		}
		return result;
	}
}
