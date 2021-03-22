package OnlineLandSearch;

import static CommonAPI.ChineseAddressHandler.ConvertTai;
import static CommonAPI.ChineseAddressHandler.newLine;
import static CommonAPI.ChineseAddressHandler.takeLastNum;
import static CommonAPI.seleniumCommon.*;
import static OnlineLandSearch.Jurdical.FifthCellHandler;
import static OnlineLandSearch.Jurdical.trsXpath;
import static OnlineLandSearch.findLandOnline.ADDRESS;
import static OnlineLandSearch.findLandOnline.JURDI_INFO;
import static OnlineLandSearch.findLandOnline.PRICE;
import static OnlineLandSearch.findLandOnline.SQUARE;
import static OnlineLandSearch.findLandOnline.UNIT_PRICE;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import dataStructure.ExcelValue;
import dataStructure.ExcelValue.Row;
import dataStructure.ExcelValue.Row.Item;;

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
//	public static final String trsXpath = "//html/body/form[1]/table[1]/tbody/tr[4]/td[2]/table/tbody/tr";
	public static final String trsXpath = "//html/body/div[1]/form[1]/table[2]/table/tbody/tr";

//	public static final String tdsXpath = trsXpath + "/td";

	public static void main(String... args) throws Exception {
		WebDriver driver = startJurdical();
		visitJurdical(driver, "嘉義", 0);
		int lastPage = findLastPage(driver);

//		toPage(driver,lastPage);
//		findLandOnline.setHtmlHeader(driver);
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("v2"));

		for (int pageNum = 2; pageNum <= lastPage; ++pageNum) {

//			LoopTr: for (WebElement tr : driver.findElements(By.xpath(trsXpath))) {
//				
//			}
			toPage(driver,pageNum);
			Thread.sleep(500);
		}

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

	public static WebElement clickPDFUrl(WebElement tr) {

		mustExist(tr, By.tagName("a")).click();
//		List<WebElement> links = tr.findElements(By.tagName("a"));
//		for (WebElement link : links) {
//			if (link.getText().contains("段") || link.getText().contains("縣") || link.getText().contains("號")) {
//				mustClick(driver,tr.findElements(By.tagName("a")));
//				return link;
//			}
//		}
		return null;
	}

	//要在V2的這個frame
	public static void toPage(WebDriver driver, int pageNum) throws InterruptedException {
//		exeJs(driver, "doChangeAction();form.pageNow.value=" + pageNum + ";form.submit();");
//		Thread.sleep(500);
		WebElement input = driver.findElement(By.name("_pageNum_"));
		System.out.println("to page:"+pageNum);
//		input.clear();
		input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		input.sendKeys(String.valueOf(pageNum));
		input.sendKeys(Keys.RETURN);
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
		driver.get("https://aomp109.judicial.gov.tw/judbp/wkw/WHD1A02.htm");
		return driver;
	}

	public static Row FifthCellHandler(Row row,String FifthCell, ExcelValue excelValue) {

//		if (FifthCell.contains("全部")) {
//		System.out.println(FifthCell);
		String arr[] = FifthCell.split("\n");
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
//		System.out.println(row);
		return row;
//		}
//		return null;
	}

	public static void downloadJurdical(ExcelValue excelValue)
			throws InterruptedException, UnsupportedEncodingException {

		for (String courtName : court) {
			courtName = ConvertTai(courtName);
			outer: for (int landtype = 0; landtype < 2; ++landtype) {
				WebDriver driver = startJurdical();
				visitJurdical(driver, courtName, landtype);
				exeJs(driver, "doExcel();");
				driver.quit();
				continue outer;
			}

		}
	}

	public static int findLastPage(WebDriver driver) throws Exception {
		int pageNum = 0;
//		try {
//			return Integer.parseInt(driver.findElement(By.xpath("/html/body/form[1]/table[2]/tbody/tr[2]/td/nobr[15]"))
//					.getAttribute("onclick").toString().replaceAll("[^0-9]+", ""));
//
//		}catch (NoSuchElementException e) {
//			List<WebElement> pageElements = driver.findElements(By.tagName("nobr"));
//			return pageElements.size();
//		} 
//		catch (Exception e) {
//			e.printStackTrace();
//		}

		// 第 1 / 132 頁
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("v2"));

//		String page = driver.findElement(By.name("_totalPageNum_")).getText();
//		System.out.print("!!!"+page);
		String page[] = driver.findElement(By.id("page_info")).getText().split("/");

		try {
			if (page.length >= 1)
				pageNum = Integer.parseInt(page[1].replace("頁", "").trim());
			System.out.println("司法院找最後一頁" + pageNum);
		} catch (NumberFormatException e) {
			System.out.println("司法院找最後一頁有問題");
			throw new Exception("司法院找最後一頁有問題");
		}
		driver.switchTo().defaultContent();
		return pageNum;

	}

	public static void visitJurdical(WebDriver driver, String courtName, int landtype)
			throws InterruptedException, UnsupportedEncodingException {
		WebDriverWait wait = new WebDriverWait(driver, 10);

		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("v1"));
		WebElement court = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("court")));

		Select courtSelect = new Select(court);
		List<WebElement> options = courtSelect.getOptions();

		for (WebElement option : options) {
//			System.out.println(option.getText());
			if (option.getText().contains(courtName)) {
				courtSelect.selectByVisibleText(option.getText());
//				wait.until(ExpectedConditions.elementToBeClickable(by)); 
				option.click();
				// 持分全部
//				waitAndClick(wait,driver,By.id("rrange_ALL"));
				driver.findElement(By.id("rrange_ALL")).click();
				// 土地
//				waitAndClick(wait,driver,By.id("proptype_c51"));
				driver.findElement(By.id("proptype_c51")).click();

				if (landtype % 2 == 0)// 一般
//					waitAndClick(wait,driver,By.id("saletype_1"));
					driver.findElement(By.id("saletype_1")).click();
				else // 應買
//					waitAndClick(wait,driver,By.id("saletype_4"));
					driver.findElement(By.id("saletype_4")).click();

//				driver.findElement(By.xpath("//input[@class='small']")).submit();
				driver.findElement(By.id("btn_ok")).click();
//				Thread.sleep(1000000);
				break;
			}
		}
		driver.switchTo().defaultContent();
	}

}
