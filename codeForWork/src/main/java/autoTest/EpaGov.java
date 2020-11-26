package autoTest;

import static autoTest.scrappy.defaultPath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import excel.Excel;
import excel.Excel.ExcelType;

public class EpaGov {
	
	public static List<WebElement> findElementsBy (By by, WebElement parent) {
		int attempts = 0;
		List<WebElement> trs= new ArrayList<>();
		while (attempts < 20) {
			try {
				trs = parent.findElements(by);
				return trs;
			} catch (StaleElementReferenceException e1) {
				e1.printStackTrace();
			}
			attempts++;
		}
		return trs;
	}

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		System.setProperty("webdriver.chrome.driver", defaultPath + "/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.get(
				"https://erdb.epa.gov.tw/DataRepository/EnvMonitor/AtmMdWeatherMonthly.aspx?topic1=%u5927%u6c23&topic2=%u74b0%u5883%u53ca%u751f%u614b%u76e3%u6e2c&subject=%u5929%u6c23");

		String[] include = { "日照時數", "最低氣溫", "最高氣溫", "相對溼度", "降水量", "雲量", "風速" };
		String[] county = { "臺中", "臺南", "花蓮", "臺東", "新竹", "宜蘭", "玉山", "阿里山" };
		// ctl00_ContentPlaceHolder1_ucSearchCondition_ddlItem
		Select courtSelect = new Select(
				driver.findElement(By.id("ctl00_ContentPlaceHolder1_ucSearchCondition_ddlItem")));
		Excel file = Excel.createExcel(ExcelType.EXCEL_XLSX);
		List<WebElement> options = courtSelect.getOptions();
		//ctl00_ContentPlaceHolder1_gvResult
		WebElement div = driver.findElement(By.id("ctl00_ContentPlaceHolder1_UpdatePanel1")).findElement(By.tagName("table"));
		
		for (String s : include) {
			for (WebElement option : options) {
				String temp = "";
			    int attempts = 0;
				while (attempts < 5) {
					try {
						temp = option.getText();
						break;
					} catch (StaleElementReferenceException e1) {
					}
					attempts++;
				}
				if (temp.equals(s)) {
					option.click();
					file.assignSheet(s);
					List<WebElement> trs = findElementsBy( By.tagName("tr"),div);
					int rowCount = 0;
					for (WebElement tr : trs) {
						List<WebElement> ths = findElementsBy( By.tagName("th"),tr);
//						List<WebElement> ths = tr.findElements(By.tagName("th"));
						file.assignRow(rowCount++);
						int cellCount_row = 0;
						for (WebElement th : ths) {
							file.assignCell(cellCount_row++).getCurCell().setCellValue(th.getText());
						}
						List<WebElement> tds = findElementsBy(By.tagName("td"),tr);
//						List<WebElement> tds = tr.findElements(By.tagName("td"));
						if (tds.size() > 1 && Arrays.asList(county).contains(tds.get(0).getText())) {
							file.assignRow(rowCount++);
							int cellCount = 0;
							for (WebElement td : tds) {
								file.assignCell(cellCount++).getCurCell().setCellValue(td.getText());
							}
						}
					}
					break;
				}
			}
			
		}
		driver.quit();
		try {
			String filePath = defaultPath + "/EPA結果.xls";
			FileOutputStream out = new FileOutputStream(new File(filePath));
			file.getWorkbook().write(out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
