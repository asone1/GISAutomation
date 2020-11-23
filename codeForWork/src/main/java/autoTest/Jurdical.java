package autoTest;

import java.util.List;
import static autoTest.seleniumCommon.exeJs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import static autoTest.scrappy.defaultPath;
import static autoTest.commonMethod.ConvertTai;
import static autoTest.commonMethod.newLine;;

public class Jurdical {
	// private static String[] court = { "臺中", "新竹" };
	private static String[] court = {  "台東" };

	public static void main(String... args) throws InterruptedException {
		visitJurdical();
	}

	public static String AddressBuilder(String county, String address) {
		StringBuilder completeAddress = new StringBuilder();
		county = ConvertTai(county.strip());
		address = ConvertTai(address.strip()).substring(0,
				(!address.contains("號") ? address.strip().length() : address.strip().lastIndexOf("號") + 1));

		// e.g.臺東縣
		// 成功鎮
		String[] countyArr = county.split(newLine);

		for (String s : countyArr) {
			if (!address.contains(s)) {
				completeAddress.append(s);
			}
		}
		completeAddress.append(address);
		return completeAddress.toString();
	}

	public static String searchById(WebDriver driver,String id, String address) throws InterruptedException {
		
		Select courtSelect = new Select(driver.findElement(By.name("court")));
		List<WebElement> options = courtSelect.getOptions();

		for (WebElement option : options) {
			if (option.getText().contains("臺南")) {
				option.click();break;
			}
		}

		Thread.sleep(200);
		driver.findElement(By.tagName("input")).submit();
		// 土地
		driver.findElement(By.xpath("//input[@value='C51']")).click();
		driver.findElement(By.xpath("//input[@class='small']")).submit();

		driver.findElement(By.id("crmno")).sendKeys(id);
		driver.findElement(By.xpath("//input[@value='確定']")).submit();

		List<WebElement> links = driver.findElements(By.tagName("a"));
		for (WebElement link : links) {
			if (link.getText().contains(address.substring(address.lastIndexOf("段") + 1, address.indexOf("號")))) {
				
				return link.getAttribute("href");
			}
		}
		
		return "";
	}

	public static WebDriver startJurdical() throws InterruptedException {
		System.setProperty("webdriver.chrome.driver", defaultPath + "/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.get("https://aomp.judicial.gov.tw/abbs/wkw/WHD2A00.jsp");
		return driver;
	}

	public static void visitJurdical() throws InterruptedException {
		System.setProperty("webdriver.chrome.driver", defaultPath + "/chromedriver.exe");

//	    JavascriptExecutor js = (JavascriptExecutor)driver;
//	    driver.get("http://www.google.com/");
		// https://aomp.judicial.gov.tw/abbs/wkw/WHD2A00.jsp
		for (String s : court) {
			s = ConvertTai(s);
			outer: for (int i = 0; i < 3; ++i) {
				WebDriver driver = new ChromeDriver();
				driver.get("https://aomp.judicial.gov.tw/abbs/wkw/WHD2A00.jsp");
				Select courtSelect = new Select(driver.findElement(By.name("court")));
				List<WebElement> options = courtSelect.getOptions();

				for (WebElement option : options) {
					if (option.getText().contains(s)) {
						courtSelect.selectByVisibleText(option.getText());
						option.click();
						driver.findElement(By.tagName("input")).submit();

						driver.findElement(By.xpath("//input[@value='C51']")).click();
						if (i % 2 == 0)
							driver.findElement(By.xpath("//input[@value='1']")).click();
						else
							driver.findElement(By.xpath("//input[@value='4']")).click();

						driver.findElement(By.xpath("//input[@class='small']")).submit();
						driver.findElement(By.xpath("//input[@value='確定']")).submit();
//						exeJs(driver, "javascript:document.form.submit();");
//						exeJs(driver, "javascript:final2();");

						exeJs(driver, "doExcel();");
						Thread.sleep(500);
						driver.quit();
						continue outer;
						// driver.findElement(By.name("downloadExcel")).submit();

//							driver.findElement(By.tagName(""));
					}
				}
				driver.quit();
			}

		}

		// Thread.sleep(5000); // Let the user actually see something!

	}
}
