package OnlineLandSearch;

import static autoTest.scrappy.defaultPath;
import static CommonAPI.ChineseAddressHandler.ConvertTai;
import static CommonAPI.ChineseAddressHandler.newLine;
import static CommonAPI.ChineseAddressHandler.takeLastNum;
import static CommonAPI.seleniumCommon.*;
import static OnlineLandSearch.findLandOnline.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import dataStructure.ExcelValue;
import dataStructure.ExcelValue.Row;
import dataStructure.ExcelValue.Row.Item;
import io.github.bonigarcia.wdm.WebDriverManager;;

public class Jurdical {
	// private static String[] court = { "臺中", "新竹" };
	private static String[] court = { "嘉義" };
	/*
	 * page1
	 */
	// 房屋
	public static By house = By.xpath("//input[@value='C52']");
	// 土地
	public static By land = By.xpath("//input[@value='C51']");
	public static By confirm2 = By.xpath("//input[@class='small']");
	/*
	 * page2
	 */
	// 一般
	public static By goBid = By.xpath("//input[@value='1']");
	// 應買
	public static By goBuy = By.xpath("//input[@value='4']");
	/*
	 * page3
	 */
	public static By bidId = By.id("crmno");
	public static By confirm = By.xpath("//input[@value='確定']");
//	public static By fifthCell = By.xpath("//html/body/form[1]/table[1]/tbody/tr[4]/td[2]/table/tbody/tr/td[6]");
	public static final String trsXpath = "//html/body/form[1]/table[1]/tbody/tr[4]/td[2]/table/tbody/tr";
//	public static final String tdsXpath = trsXpath + "/td";

//	public static void main(String... args) throws InterruptedException, UnsupportedEncodingException {
//		downloadJurdical(new ExcelValue());
//	}

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

	public static String searchById(WebDriver driver, String id, String address, boolean landType)
			throws InterruptedException {

		setCourt(driver, address);

		// 土地
		driver.findElement(land).click();

		if (landType) {
			driver.findElement(goBuy).click();
		}
		driver.findElement(confirm2).submit();
		driver.findElement(bidId).sendKeys(id);
		driver.findElement(confirm).submit();

//		findPDFUrl(driver);

		return "";
	}

	public static String findPDFUrl(WebElement tr) {
		List<WebElement> links = tr.findElements(By.tagName("a"));
		for (WebElement link : links) {
			if (link.getText().contains("段") || link.getText().contains("縣") || link.getText().contains("號")) {
				return link.getAttribute("href");
			}
		}
		return "";
	}

	public static void toPage(WebDriver driver, int pageNum) throws InterruptedException {
		exeJs(driver, "doChangeAction();form.pageNow.value=" + pageNum + ";form.submit();");
		Thread.sleep(500);
	}

	public static void setCourt(WebDriver driver, String countyName) throws InterruptedException {
		Select courtSelect = new Select(driver.findElement(By.name("court")));
		List<WebElement> options = courtSelect.getOptions();

		for (WebElement option : options) {
			if (option.getText().contains(countyName.substring(0, 2))) {
				option.click();
				break;
			}
		}

		Thread.sleep(200);
		driver.findElement(By.tagName("input")).submit();
	}

	public static void addJurdicalPdfInfo(Row row, String pdfContent) {
//		String county = row.getItem(COUNTY).getItemValue().split(newLine)[0];
		String location_spec = row.getItem(ADDRESS).getItemValue();
		String locateLandObject = takeLastNum(location_spec);
		// 可能很多物件一起賣，利用地號找到該物件位置
		int thisIndex = pdfContent.indexOf(locateLandObject);
		// 從該物件的備考開始，加入到item資料
		String section = pdfContent.substring(thisIndex, pdfContent.length());
		// 若有其他標別，資料會太多
		int lastIndex = section.indexOf("備註");
		if (lastIndex == -1)
			lastIndex = section.length();
		try {
			row.addNewItem(JURDI_INFO, section.substring(section.indexOf("使用情形"), lastIndex));
		} catch (StringIndexOutOfBoundsException e) {
			row.addNewItem(JURDI_INFO, section.substring(section.indexOf("使用情形"), section.length()));
		}
	

	}

	public static WebDriver startJurdical() throws InterruptedException {
		WebDriver driver = startDriver();
		driver.get("https://aomp.judicial.gov.tw/abbs/wkw/WHD2A00.jsp");
		return driver;
	}

	public static Row FifthCellHandler(String FifthCell, ExcelValue excelValue) {

		if (FifthCell.contains("全部")) {
			String arr[] = FifthCell.split("\n");
			Row row = excelValue.new Row();
			for (int arrIndex = 0; arrIndex < arr.length; arrIndex++) {
				switch (arrIndex) {
				case 0:
					row.addNewItem(ADDRESS, arr[arrIndex].trim().replace(" ", ""));
					break;
				case 1:
					row.addNewItem(SQUARE, arr[arrIndex].trim().replace(" ", "").split("坪")[0]);
					;
					break;
				case 2:
					String Price = arr[arrIndex].substring(11).trim().replace(" ", "").replace("元", "").replace(",", "")
							.trim();
					try {
						// 萬為單位
						row.addNewItem(PRICE, String.valueOf(Integer.parseInt(Price) / 10000));
						row.addNewItem(UNIT_PRICE,
								new DecimalFormat("#.##").format(Double.parseDouble(row.getItem(PRICE).getItemValue())
										/ Double.parseDouble(row.getItem(SQUARE).getItemValue())));

					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}

			}
			return row;
		}
		return null;
	}

	public static void downloadJurdical(ExcelValue excelValue)
			throws InterruptedException, UnsupportedEncodingException {

		for (String courtName : court) {
			courtName = ConvertTai(courtName);
			outer: for (int landtype = 0; landtype < 2; ++landtype) {
				WebDriver driver = startJurdical();
				visitJurdical(excelValue, driver, courtName, landtype);
				exeJs(driver, "doExcel();");
				driver.quit();
				continue outer;
			}

		}
	}

	public static int findLastPage(WebDriver driver) {
		int pageNum = 0;
		try {
			return Integer.parseInt(driver.findElement(By.xpath("/html/body/form[1]/table[2]/tbody/tr[2]/td/nobr[15]"))
					.getAttribute("onclick").toString().replaceAll("[^0-9]+", ""));

		}catch (NoSuchElementException e) {
			List<WebElement> pageElements = driver.findElements(By.tagName("nobr"));
			return pageElements.size();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return pageNum;
	}

	public static void visitJurdical(ExcelValue excelValue, WebDriver driver, String courtName, int landtype)
			throws InterruptedException, UnsupportedEncodingException {

		Select courtSelect = new Select(driver.findElement(By.name("court")));
		List<WebElement> options = courtSelect.getOptions();

		for (WebElement option : options) {
			if (option.getText().contains(courtName)) {
				courtSelect.selectByVisibleText(option.getText());
				option.click();
				driver.findElement(By.tagName("input")).submit();

				driver.findElement(By.xpath("//input[@value='C51']")).click();
				if (landtype % 2 == 0)
					driver.findElement(goBuy).click();
				else
					driver.findElement(goBid).click();
				driver.findElement(By.xpath("//input[@class='small']")).submit();
				driver.findElement(By.xpath("//input[@value='確定']")).submit();
				Thread.sleep(500);
				return;
			}
		}
	}

}
