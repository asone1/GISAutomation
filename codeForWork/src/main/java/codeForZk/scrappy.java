package codeForZk;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.input.Input;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import scrappy.copySheet;

public class scrappy {

//	private static String[] court = { "臺中", "新竹" };
	private static String[] court = { "花蓮", "台東" };

	public void visitJudical(){
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\User\\Desktop\\chromedriver.exe");

		WebDriver driver = new ChromeDriver();
//	    JavascriptExecutor js = (JavascriptExecutor)driver;
//	    driver.get("http://www.google.com/");
		// https://aomp.judicial.gov.tw/abbs/wkw/WHD2A00.jsp
		driver.get("https://aomp.judicial.gov.tw/abbs/wkw/WHD2A00.jsp");
		Select court = new Select(driver.findElement(By.name("court")));
		List<WebElement> options = court.getOptions();
		for (String s : scrappy.court) {
			for (WebElement option : options) {
				if (option.getText().contains(s)) {
					court.selectByVisibleText(option.getText());
					option.click();
					driver.findElement(By.tagName("input")).submit();

					driver.findElement(By.xpath("//input[@value='C51']")).click();
					driver.findElement(By.xpath("//input[@class='small']")).submit();

					driver.findElement(By.xpath("//input[@value='確定']")).click();
//					driver.findElement(By.name("downloadExcel")).submit();
					
					driver.findElement(By.tagName(""));
				}
			}
		}
//		Thread.sleep(5000); // Let the user actually see something!
//		driver.quit();

	}
	static String location[];
	static String setLocationElementId[] = {"city","area_office","section"};
	
	public static String ConvertTai(String input) {
		return input.replace("台", "臺");
	}
	
	public static void clickPopUP(WebDriver driver) {
		for(WebElement e:driver.findElements(By.tagName("button"))) {
			if(e.isDisplayed()) {
				Actions act =  new Actions(driver);
				act.moveToElement(e).click().perform();
			}
		}
	}
	
	public static void main(String[] arg) throws InterruptedException {
		// Optional. If not specified, WebDriver searches the PATH for chromedriver.
		//
		location =new String[4];
		location[0]="台東縣";
		location[1]="卑南鄉";
		location[2]="鎮樂段";
		location[3]="148號";
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\User\\Desktop\\chromedriver.exe");

		WebDriver driver = new ChromeDriver();
		driver.get("https://maps.nlsc.gov.tw/T09/mapshow.action?In_type=web");
		//按確認按紐
		for(int i=0; i<2;++i) {
			clickPopUP(driver);
		}
			
		//click定位查詢
			//onclick="folder('adg','CollapsiblePanel1');toggleControl('none');chk_allpos(false);"
		    JavascriptExecutor js = (JavascriptExecutor)driver;
			js.executeScript("folder('adg','CollapsiblePanel1');toggleControl('none');chk_allpos(false);");
//		id=submenu_po
		WebElement submenu = driver.findElement(By.id("submenu_pos"));
		int countEleId=0;
		for(String eleId : setLocationElementId) {
			for (WebElement option : (new Select(submenu.findElement(By.id(eleId)))).getOptions()) {
				if (option.getText().contains(ConvertTai(location[countEleId]))) {
					option.click();++countEleId;
				}
			}
		}
		//"landcode"
		driver.findElement(By.id("landcode")).sendKeys(location[3].replace("號", ""));
		driver.findElement(By.id("div_cross_query")).click();
		
		clickPopUP(driver);
		Thread.sleep(5000000);
		driver.quit();
		//https://maps.nlsc.gov.tw/T09/mapshow.action?In_type=web
		
	}

}
