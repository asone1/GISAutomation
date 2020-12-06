package OnlineLandSearch;

import static CommonAPI.ChineseAddressHandler.ConvertTai;
import static CommonAPI.ChineseAddressHandler.newLine;
import static CommonAPI.ChineseAddressHandler.preprocessAddress;
import static CommonAPI.ChineseAddressHandler.relativeSymbol;
import static CommonAPI.seleniumCommon.clickPopUP;
import static CommonAPI.seleniumCommon.exeJs;
import static CommonAPI.seleniumCommon.jsClick;
import static CommonAPI.seleniumCommon.mustDo;
import static CommonAPI.seleniumCommon.startDriver;
import static CommonAPI.seleniumCommon.takeSnapShot;
import static CommonAPI.seleniumCommon.waitAndClick;
import static autoTest.scrappy.defaultPath;
import static autoTest.scrappy.zoomIn;
import static autoTest.scrappy.zoomOut;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import dataStructure.ExcelValue;
import dataStructure.ExcelValue.Row;
import dataStructure.ExcelValue.Row.Item;

public class GovEmap {

	static String setLocationElementId[] = { "city", "area_office", "section" };
	private final static String landQrybuttonxpath[] = {
			"/html/body/div[1]/div[2]/table/tbody/tr/td/div[4]/div[2]/div[2]/div[1]/div/div/div/ul/li[2]",
			"/html/body/div[1]/div[2]/table/tbody/tr/td/div[4]/div[2]/div[2]/div[1]/div/div/div/ul/li[3]" };

	private final static String landQryTablexpath[] = {
			"/html/body/div[1]/div[2]/table/tbody/tr/td/div[4]/div[2]/div[2]/div[1]/div/div/div/div/div[1]",
			"/html/body/div[1]/div[2]/table/tbody/tr/td/div[4]/div[2]/div[2]/div[1]/div/div/div/div/div[2]",
			"/html/body/div[1]/div[2]/table/tbody/tr/td/div[4]/div[2]/div[2]/div[1]/div/div/div/div/div[3]" };

	private final static String tableXpath[] = { "//table/tbody/tr/td[1]", "//table/tbody/tr/td[2]" };
	private final static String detailXpath = "//*[@id=\"div_cross\"]/input[2]";
	private final static String onColorXpath = "//*[@id=\"div_cross\"]/input[1]";
	// *[@id="qryLand_tab2_0"]/table/tbody/tr[2]/td[1]

	public static void findTable1(WebDriver driver, Map<String, Integer> excelHeaders, Row row) {
		WebElement td = driver.findElement(By.xpath(
				"/html/body/div[1]/div[2]/table/tbody/tr/td/div[4]/div[2]/div[2]/div[1]/div/div/div/div/div[1]/table/tbody/tr[1]/td"));

		boolean cached_0 = false;

		String[] txt = td.getText().split("\\n");
		for (String tdtext : txt) {
			if (tdtext.contains(":")) {
				Item cacheditem_0 = row.new Item();
				String combination[] = tdtext.split(":");
				for (String single : combination) {
					if (cached_0) {
						cached_0 = false;
						cacheditem_0.setValue(single);
					}
					if (excelHeaders.containsKey(single)) {
						cached_0 = true;
						cacheditem_0.setColumn(single);
					}
				}
				if (StringUtils.isNotBlank(cacheditem_0.getItemValue())) {
					row.addItem(cacheditem_0);
					cacheditem_0 = row.new Item();
				}
			}
		}

	}

	public static void findTable2(WebDriver driver, Map<String, Integer> excelHeaders, Row row) {
//		/html/body/div[1]/div[2]/table/tbody/tr/td/div[4]/div[2]/div[2]/div[1]/div/div/div/div/div[2]/table/tbody/tr[3]/td[1]
		List<WebElement> combo = driver.findElement(By.xpath(landQryTablexpath[1])).findElements(By.tagName("tr"));
//		/html/body/div[1]/div[2]/table/tbody/tr/td/div[4]/div[2]/div[2]/div[1]/div/div/div/div/div[2]/table/tbody/tr[3]/td[2]
//		List<WebElement> cells = driver.findElement(By.xpath(landQryTablexpath[1])).findElements(By.xpath(tableXpath[1]));
		for (WebElement tr : combo) {
			List<WebElement> tds = tr.findElements(By.tagName("td"));
			if (tds.size() >= 2 && excelHeaders.containsKey(tds.get(0).getText())) {
				Item item = row.new Item();
				item.setColumn(tds.get(0).getText());
				item.setValue(tds.get(1).getText());
				row.addItem(item);
//				System.out.println("in__" + row.getItems());
				if (item.getColumn().equals("公告土地現值")) {
					break;
				}
			}
		}

	}

//	@SuppressWarnings("finally")
//	public static Row findbyTable(WebDriver driver, WebElement tableEle, Map<String, Integer> excelHeaders, Row row,
//			boolean is2cols) {
//		boolean itemAdded = false;
//
//		try {
//			List<WebElement> tds = tableEle.findElements(By.xpath(tableXpath[0]));
////			List<WebElement> td2s = tableEle.findElements(By.xpath(tableXpath[1]));
//			int toBreak = -1;
//
//			boolean cached = false;
//			Item cacheditem = row.new Item();
//
//			for (int index = 0; index < tds.size(); ++index) {
//				if (index == toBreak)
//					break;
//				if (tds.get(index).getText() != null && !tds.get(index).getText().isEmpty()) {
//
//					boolean cached_0 = false;
//					String[] txt = tds.get(index).getText().split("\\n");
//					for (String tdtext : txt) {
//						if (tdtext.contains(":")) {
//							Item cacheditem_0 = row.new Item();
//							String combination[] = tdtext.split(":");
//							for (String single : combination) {
//								if (cached_0) {
//									cached_0 = false;
//									cacheditem_0.cell = single;
//								}
//								if (excelHeaders.containsKey(single)) {
//									cached_0 = true;
//									cacheditem_0.column = single;
//								}
//								if (single.contains("國土利用現況調查")) {
//									toBreak = index + 1;
//								}
//							}
//							if (StringUtils.isNotBlank(cacheditem_0.cell)) {
//								row.addItem(cacheditem_0);
//								itemAdded = true;
//								cacheditem_0 = row.new Item();
//							}
//						} else {
//							if (cached && !excelHeaders.containsKey(tdtext)) {
//								cacheditem.cell = tdtext;
//								cached = false;
//							}
//							if (excelHeaders.containsKey(tdtext)) {
//								cached = true;
//								cacheditem.column = tdtext;
//								if (tdtext.equals("公告土地現值")) {
//									toBreak = index + 2;
//								}
//							}
//						}
//
//					}
//					if (StringUtils.isNotBlank(cacheditem.cell)) {
//						row.addItem(cacheditem);
//						itemAdded = true;
//						cacheditem = row.new Item();
//					}
//
//				}
//			}
//		} catch (StaleElementReferenceException e) {
//			System.out.println(e);
//		} finally {
//			return row;
//		}
//	}

	public static WebDriver startMap() throws InterruptedException {
		WebDriver driver = startDriver();
		driver.get("https://maps.nlsc.gov.tw/T09/mapshow.action?In_type=web");
		driver.manage().window().maximize();

		Thread.sleep(5000);
		// 按確認按紐
		waitAndClick(driver, By.xpath("//html/body/div[31]/div[1]/button"));

		exeJs(driver, "notShowSysinfo();");
		Thread.sleep(300);

		return driver;
	}

	// 加段籍圖
	public static void addLayer(WebDriver driver) throws InterruptedException {
		// 塗層設定
		exeJs(driver, "$( '#layer_dialog' ).dialog('open'); folder_view('adg','CollapsiblePanel1');");
		Thread.sleep(300);
		exeJs(driver, "addIndexPage('layerlist','/pro/layerlist.html');$( '#layerlist_dialog' ).dialog('open');");
		Thread.sleep(300);
		exeJs(driver, "addMapsPage('002','layerlist','/pro/002_layer.html');");
		Thread.sleep(300);
		exeJs(driver, "chk_layertype('土地圖層',DMAPS,'DMAPS');");
		waitAndClick(driver, By.xpath("//html/body/div[32]/div[3]/div/button[1]"));
	}

	// 地號查詢
	public static void useFormalAddress(WebDriver driver, String[] location) throws Exception {
		// click定位查詢
		exeJs(driver, "folder('adg','CollapsiblePanel1');toggleControl('none');chk_allpos(false);");
		Thread.sleep(5000);
		// 開始再定位查詢輸入資料
		WebElement submenu = driver.findElement(By.id("submenu_pos"));
		int countEleId = 0;

		for (String eleId : setLocationElementId) {
			boolean click = false;
			List<WebElement> options = (new Select(submenu.findElement(By.id(eleId)))).getOptions();
			for (WebElement option : options) {
				if (location[countEleId] != null
						&& (option.getText().trim().equals(ConvertTai(location[countEleId])))) {
					option.click();
					click = true;
					Thread.sleep(500);
					++countEleId;
					break;
				}
			}
			if (click == false) {
				for (WebElement option : options) {
					if (location[countEleId] != null && option.getText().trim().equals(
							ConvertTai(location[countEleId].substring(0, location[countEleId].indexOf("段") + 1)))) {
						option.click();
						click = true;
						Thread.sleep(500);
						++countEleId;
						break;
					}
				}
			}
			if (click == false) {
				for (WebElement option : options) {
					if (location[countEleId] != null && option.getText().trim().contains(
							ConvertTai(location[countEleId].substring(0, location[countEleId].indexOf("段") + 1)))) {
						option.click();
						Thread.sleep(500);
						++countEleId;
						break;
					}
				}
			}
			if (click == false) {
				for (String s : location)
					System.out.println(s);
				throw new Exception("找不到地號");
			}
		}
// "landcode"
		if (location[3] != null) {
			driver.findElement(By.id("landcode")).sendKeys(location[3].replace("號", ""));
		}

		driver.findElement(By.id("div_cross_query")).click();

		try {
			clickPopUP(driver);
		} catch (UnhandledAlertException e) {
			driver.findElement(By.id("div_cross_query")).click();
			clickPopUP(driver);
		}

	}

	public static void openInfo(WebDriver driver) throws InterruptedException {
		// 點選查詢
		// onclick="toggleControl('PointQuery');"
		exeJs(driver, "toggleControl('PointQuery');");

		// get the center of the canvas(map)
		WebElement canvas = driver.findElement(By.id("content_map")).findElement(By.tagName("canvas"));
		new Actions(driver).moveToElement(canvas, 0, 0).click().perform();
		clickPopUP(driver);
		// 等page reload
		Thread.sleep(5000);
		try {
			driver.findElement(By.xpath("//input[@value='著色']")).click();
		} catch (NoSuchElementException | ElementClickInterceptedException e) {
			e.printStackTrace();
		}

	}

//	public static void onFormalColor(WebDriver driver) throws InterruptedException {
//		try {
//			driver.findElement(By.id("div_cross")).findElement(By.xpath("//input[@value='著色']")).click();
//		} catch (NoSuchElementException | ElementClickInterceptedException e) {
//			e.printStackTrace();
//		}
//	}

	public static void clickDetail(WebDriver driver) throws InterruptedException {
		for (int i = 0; i < 2; ++i) {
			waitAndClick(driver, By.xpath(detailXpath));
			Thread.sleep(1500);
		}
	}

	public static void findTwoTables(Row row, Map<String, Integer> excelHeaders, WebDriver driver)
			throws InterruptedException {
		// 點土地資訊 load資料

		try {
//			row = findbyTable(driver, driver.findElement(By.xpath(landQryTablexpath[0])), excelHeaders, row, false);
			findTable1(driver, excelHeaders, row);
			// 土地資訊
//			mustDo(driver, By.xpath(landQrybuttonxpath[0]));
			jsClick(driver, By.xpath(
					"/html/body/div[1]/div[2]/table/tbody/tr/td/div[4]/div[2]/div[2]/div[1]/div/div/div/ul/li[2]/code/a"));
			Thread.sleep(2000);
			findTable2(driver, excelHeaders, row);
//			System.out.println(row.getItems());
//			row = findbyTable(driver, driver.findElement(By.xpath(landQryTablexpath[1])), excelHeaders, row, true);

			// 地段資料
//					mustDo(driver, By.xpath(detailXpath));
//					Thread.sleep(500);
//					mustDo(driver, By.xpath(landQrybuttonxpath[1]));
//					findbyTable(driver, driver.findElement(By.xpath(landQryTablexpath[2])));
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		}
	}

	public static void closeInfo(WebDriver driver, boolean isFormal) throws InterruptedException {
		if (isFormal) {
			exeJs(driver, "folder_view('adg','CollapsiblePanel1');");
		} else {
			exeJs(driver, "folder_view('adg','CollapsiblePanel2');");
		}

		Thread.sleep(2000);
//		mustDo(driver, By.id("popup-closer"));
		jsClick(driver, By.id("popup-closer"));
	}

	public static void take4snapshot(String idName, String[] location, WebDriver driver, List<String> pics, Row row) {
		String defaultFilePath = "查詢結果";
		if (location.length >= 1)
			defaultFilePath = location[0];
		try {
			String urls[] = new String[4];
			urls[0] = defaultFilePath + "/" + idName + "0.png";
			zoomOut(1);
			Thread.sleep(500);
			takeSnapShot(driver, defaultPath + "/" + urls[0]);

			urls[1] = defaultFilePath + "/" + idName + "0_S.png";
			zoomOut(4);
			Thread.sleep(7000);
			takeSnapShot(driver, defaultPath + "/" + urls[1]);

			Thread.sleep(500);
			jsClick(driver, By.id("qt-ctrl-quickSwitch"));

			Thread.sleep(7000);
			urls[2] = defaultFilePath + "/" + idName + "1_S.png";
			takeSnapShot(driver, defaultPath + "/" + urls[2]);

			zoomIn(4);
			Thread.sleep(2000);
			urls[3] = defaultFilePath + "/" + idName + "1.png";
			takeSnapShot(driver, defaultPath + "/" + urls[3]);

			zoomIn(1);

			int idx = 0;
			if (pics.size() >= 4) {
				for (String url : urls) {
					row.addItem(row.new Item().setItem(pics.get(idx++), relativeSymbol + url));
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(idName + System.lineSeparator() + e);
		}
	}

	// 門牌查詢
	public static void useLegalAddress(WebDriver driver, String address) throws InterruptedException {

		By searchBox = By.id("lucene");
		mustDo(driver, searchBox);
		driver.findElement(searchBox).sendKeys(address);
		driver.findElement(By.id("lucene_search")).click();

		Thread.sleep(8000);
		By firstSearchResult = By.xpath("//*[@id=\"rc_model_list\"]/ul/li/a");
		try {
			jsClick(driver, firstSearchResult);
		} catch (Exception e) {
			try {
				Thread.sleep(8000);
				jsClick(driver, firstSearchResult);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		Thread.sleep(3000);
		openInfo(driver);
//		jsClick(driver, By.xpath(
//				"/html/body/div[1]/div[2]/table/tbody/tr/td/div[4]/div[2]/div[2]/div[1]/div/div/div/ul/li[2]/code/a"));

	}

	public static char[] legalAddrToken = { '弄', '街', '路', '巷' };

	// true = 用門牌查詢
	public static boolean isFormal(String location) {
		for (int i = 0; i < location.length(); i++) {
			for (int j = 0; j < legalAddrToken.length; ++j) {
				if (legalAddrToken[j] == location.charAt(i)) {
					return false;
				}
			}
		}
		return true;
	}

	public static void search(String[] location, Row row, List<String> pics, Map<String, Integer> excelHeaders)
			throws Exception {
		String idName = String.valueOf(Math.random());
		idName = row.getItem("字號").getValue();

		WebDriver driver = startMap();
		addLayer(driver);
		String address = row.getItem("地址").getValue();
		boolean isformal = isFormal(address);
		if (isformal) {
			useFormalAddress(driver, location);
		} else {
			useLegalAddress(driver, address);
		}
		Thread.sleep(5000);
		openInfo(driver);
		findTwoTables(row, excelHeaders, driver);
		Thread.sleep(500);
		closeInfo(driver, isformal);
		take4snapshot(idName, location, driver, pics, row);
		driver.quit();
	}

	public static String[] locationToArr(String county, String location_spec) {
		String[] location = new String[4];
		county = preprocessAddress(county).replace(" ", "");
		location_spec = preprocessAddress(location_spec).replace(" ", "");
		String temp[] = county.split(newLine);
		if (temp.length >= 2) {
			location[0] = temp[0];
			location[1] = temp[1];
			location_spec = location_spec.replace(location[0], "");
			location_spec = location_spec.replace(location[1], "").trim();
		}

		location[2] = location_spec.substring(0, (location_spec.lastIndexOf("段")) + 1).trim();
		location[3] = location_spec.substring((location_spec.lastIndexOf("段")) + 1, location_spec.length()).trim();

		return location;
	}

	public static WebDriver search(String[] location, Row row, Map<String, Integer> excelHeaders) throws Exception {
//		String idName = String.valueOf(Math.random());
//		idName = row.getItem("字號").getValue();

		WebDriver driver = startMap();
		addLayer(driver);
		String address = row.getItem("地址").getValue();
		boolean isformal = isFormal(address);
		if (isformal) {
			useFormalAddress(driver, location);
		} else {
			useLegalAddress(driver, address);
		}
		Thread.sleep(5000);
		openInfo(driver);
		findTwoTables(row, excelHeaders, driver);
		Thread.sleep(500);
		closeInfo(driver, isformal);
//		take4snapshot(idName, location, driver, pics, row);
		return driver;
	}
}
