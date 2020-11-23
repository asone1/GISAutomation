package codeForZk;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class scrappy2 {
	public static void main(String[] arg) throws InterruptedException {
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\Angel\\Desktop\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.get("https://www.google.com.tw/");
		

		// 無反應??
		WebElement html = driver.findElement(By.tagName("html"));
		html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));


		Thread.sleep(5000);
		driver.quit();
// https://maps.nlsc.gov.tw/T09/mapshow.action?In_type=web

	}

}