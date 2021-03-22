package OnlineLandSearch;

import static CommonAPI.ChineseAddressHandler.ConvertTai;
import static CommonAPI.ChineseAddressHandler.newLine;
import static CommonAPI.PDFHandler.readPdfContent;
import static CommonAPI.seleniumCommon.jsClick;
import static CommonAPI.seleniumCommon.takeSnapShot;
import static OnlineLandSearch.GovEmap.locationToArr;
import static OnlineLandSearch.GovEmap.search;
import static OnlineLandSearch.Jurdical.FifthCellHandler;
import static OnlineLandSearch.Jurdical.addJurdicalPdfInfo;
import static OnlineLandSearch.Jurdical.clickPDFUrl;
import static OnlineLandSearch.Jurdical.findLastPage;
import static OnlineLandSearch.Jurdical.startJurdical;
import static OnlineLandSearch.Jurdical.toPage;
import static OnlineLandSearch.Jurdical.trsXpath;
import static OnlineLandSearch.Jurdical.visitJurdical;
import static autoTest.scrappy.zoomIn;
import static autoTest.scrappy.zoomOut;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;

import CommonAPI.GoogleDriverHandler;
import CommonAPI.GoogleSheetHandler;
import dataStructure.ExcelValue;
import dataStructure.ExcelValue.Row;
import dataStructure.ExcelValue.Row.Item;
import dataStructure.OneToOneMap;
import dataStructure.PropertiesSetting;

public class findLandOnline {
//	public static String[] court = { "宜蘭" };
//	public static String[] court = { "嘉義" };
	public static String[] court = { "臺東" };

//	public static GoogleSheetHandler sheetService;
	public static Drive driveService;
	public static final String ABORIGINE = "原住民";
	// 因為html td並沒有包含欄位資訊，此map存html td位置對應欄位名稱，set時將map以空行區分
	private static OneToOneMap<String, Integer> htmlHeader;
	private static ExcelValue excelValue;
	private static PropertiesSetting prop;
	public final static String defaultPath = FileSystemView.getFileSystemView().getHomeDirectory().getPath();
	public final static String tempShotPath = defaultPath + "/temp.png";

	private static void initializeSetting() throws GeneralSecurityException, IOException {
		prop = new PropertiesSetting();
		excelValue = new ExcelValue();
		setHeader();
		driveService = GoogleDriverHandler.getService();

	}

	public static void main(String[] args) throws InterruptedException, IOException, GeneralSecurityException {
		initializeSetting();
		loopJurdical(excelValue);
	}

	public static void loopJurdical(ExcelValue excelValue) {
		// 初始設定，不管縣市地區，程式通用
		boolean initialConfig = false;
		try {
			for (String courtName : court) {
				Sheets sheetService = GoogleSheetHandler.getSheetService();
				String sheetFileId = PropertiesSetting
						.getPropertyByKey((PropertiesSetting.getPropertyByKey(courtName) + ".sheetFileId"));
				String desireCounties_arr = PropertiesSetting
						.getPropertyByKey((PropertiesSetting.getPropertyByKey(courtName) + ".desiredCounty"));

				List<String> desiredCounties = new ArrayList<>();
				if (desireCounties_arr != null) {
					desiredCounties = Arrays.asList(desireCounties_arr.split("#"));
				}
				courtName = ConvertTai(courtName);
				// 將所有幕前excel資料拉進來

				List<List<Object>> google_spreadsheet_id_list = null;
				Set<String> land_id_set = new HashSet<String>();

				String rowRange = "!A1:Y200";
				google_spreadsheet_id_list = sheetService.spreadsheets().values().get(sheetFileId, rowRange).execute()
						.getValues();
				int rowInx = 0;
				// 將以有資料存到map
				if (google_spreadsheet_id_list != null) {
					while (rowInx < google_spreadsheet_id_list.size()) {
						Object list_obj = google_spreadsheet_id_list.get(rowInx);

						if (list_obj instanceof List
								&& ((List) list_obj).size() > excelValue.getExcelHeaders().get(LAND_ID)) {
							Object land_id = ((List) list_obj).get(excelValue.getExcelHeaders().get(LAND_ID));

							if (land_id.toString().split("_").length > 1) {
								land_id_set.add(land_id.toString().split("_")[1]);
							}
						}
						++rowInx;
					}
				}

				// 初次進入網站才建立header:對應欄位index資料
				String countyName = "";
				int rowCount = GoogleSheetHandler.findSheetLastRow(sheetService, sheetFileId);
				boolean firstTime = false;
				outer: for (int landtype = 0; landtype < 2; ++landtype) {
					WebDriver driver = startJurdical();
					visitJurdical(driver, courtName, landtype);
//					System.out.println(excelValue.getExcelHeaders());
					int searchmemoIndex = excelValue.getExcelHeaders().get(SEARCH_MEMO);

					if (rowCount == 0) {
						firstTime = true;
						GoogleSheetHandler.addOneRowOnSheet(sheetService, sheetFileId, ++rowCount,
								ExcelValue.headerToStringArr(excelValue.getExcelHeaders()));
					} else {
						++rowCount;
					}

					if (initialConfig == false) {
						setHtmlHeader(driver);
						// 清空查詢備註
//						GoogleSheetHandler.clearOneCol(sheetService, sheetFileId, searchmemoIndex + 1);
						initialConfig = true;

					}
					try {
						int lastPage = findLastPage(driver);
						WebDriverWait wait = new WebDriverWait(driver, 10);
						wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("v2"));

						for (int pageNum = 2; pageNum <= lastPage; ++pageNum) {
							System.out.println("STOP0" + pageNum);

							LoopTr: for (WebElement tr : driver.findElements(By.xpath(trsXpath))) {
								try {
									wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("td")));
									List<WebElement> tds = tr.findElements(By.tagName("td"));
									Row row = excelValue.new Row();
									/*
									 * 法網站拍表格資訊
									 */
									for (Map.Entry<String, Integer> en : htmlHeader.entrySet()) {

										String htmlTdtext = tds.get(en.getValue()).getText();

										if (!htmlTdtext.contains("度分秒")) {
											row.addNewItem(en.getKey(), tds.get(en.getValue()).getText());
										}
									}

									// 檢查一 是否在想要的區域
									String district = row.getItem(COUNTY).getItemValue();
									if (desiredCounties.size() > 0 && district.split(newLine).length > 0) {
										district = district.split(newLine)[1];
										if (StringUtils.isNotBlank(district) && !desiredCounties.contains(district)) {
//											
											continue LoopTr;
										}
									}
									if (tds.size() >= 6) {
										String getElementString = new String(
												tds.get(5).getText().getBytes(Charset.forName("UTF-8")), "UTF-8");

										FifthCellHandler(row, getElementString, excelValue);
									}
									// id = 年分_字號_價格
									Item item_id = row.addNewItem(LAND_ID, createLandId(
											row.getItem(columns[0]).getItemValue(), row.getItem(PRICE).getItemValue()));

									// 確認雲端上的資料有無此案號
									// 因為header index starts from 0, so add 1 to find the char representative in
									// Google sheet
									if (!firstTime) {
										System.out.println("不是初次抓資料");

//											int rowIndex = GoogleSheetHandler.findOneCol(sheetService, sheetFileId,
//													excelValue.getExcelHeaders().get(LAND_ID) + 1,
//													item_id.getItemValue());
//											int rowIndex = -1;
										String land_id = item_id.getItemValue();
										if (land_id.split("_").length > 1) {
											String id = land_id.split("_")[1];
											if (land_id_set.contains(id)) {
												continue LoopTr;
											}
										}

//											
									}

									Double unit_Price = 0.;
									try {
										unit_Price = Double.parseDouble(row.getItem(UNIT_PRICE).getItemValue());
									} catch (Exception e) {
										e.printStackTrace();
									}
									// 檢查三 單價不高於9000/坪
									if (unit_Price <= 0.9) {
										/*
										 * 除了法網站拍表格資訊，另外加入pdf資料、土地ID、圖片資訊
										 */
										String mainWin = driver.getWindowHandle();
										clickPDFUrl(tr);
										Thread.sleep(300);
										String pdfUrl = "";
										for (String winHandle : driver.getWindowHandles()) {

											driver.switchTo().window(winHandle);
											String now_pdfUrl = driver.getCurrentUrl();
											if (now_pdfUrl.contains("pdf")) {
												pdfUrl = driver.getCurrentUrl();
												driver.close();
												driver.switchTo().window(mainWin);
												wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("v2"));
											}
										}

										if (StringUtils.isNotEmpty(pdfUrl)) {
											row.addNewItem(PDF_URL, pdfUrl);
											String pdfContent = readPdfContent(pdfUrl);

											// 檢查二 非原住民的地
											if (!pdfContent.contains(ABORIGINE)) {

												// 將pdf的使用情形加入item
												addJurdicalPdfInfo(row, pdfContent);
												/*
												 * 雲端前置
												 */
												countyName = row.getItem(COUNTY).getItemValue().split(newLine)[0];
												// 開始進入國土繪測
												String location[] = new String[4];
												location = locationToArr(row.getItem(COUNTY).getItemValue(),
														row.getItem(ADDRESS).getItemValue());
												WebDriver mapDriver = search(location, row,
														excelValue.getExcelHeaders());

												if (row.getItem(LAND_FOR) != null
														&& row.getItem(LAND_FOR).getItemValue() != null
														&& !row.getItem(LAND_FOR).getItemValue().contains("一般道路")) {
													takeSavePics(mapDriver, row, countyName);
													mapDriver.quit();
													// 每搜尋一筆 加入excel一次
//							excelValue.addaRow(row);
													GoogleSheetHandler.addOneRowOnSheet(sheetService, sheetFileId,
															rowCount++, excelValue.rowToStringArr(row));
//							excelValue.print();
												} else {
													mapDriver.quit();
												}

											}
										}

									}

								} catch (UnhandledAlertException e1) {
									e1.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							toPage(driver, pageNum);
						}
						driver.switchTo().defaultContent();
					} catch (Exception e) {
						e.printStackTrace();
						driver.quit();
					}

					driver.quit();
					continue outer;
				}

			}
		} catch (InterruptedException | IOException |

				GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	public static void takeSavePics(WebDriver mapDriver, Row row, String countyName) throws Exception {
		List<String> PIC = new ArrayList<>();
		for (String column : columns) {
			if (column.contains("圖片")) {
				PIC.add(column);
			}
		}
		zoomOut(1);
		Thread.sleep(500);
		takeSnapShot(mapDriver, tempShotPath);
		row.addNewItem(PIC.get(0), GoogleDriverHandler.savePicToGoogle(driveService, countyName,
				row.getItem(LAND_ID).getItemValue() + PIC.get(0)));

		zoomOut(4);
		Thread.sleep(7000);
		takeSnapShot(mapDriver, tempShotPath);
		row.addNewItem(PIC.get(1), GoogleDriverHandler.savePicToGoogle(driveService, countyName,
				row.getItem(LAND_ID).getItemValue() + PIC.get(1)));

		Thread.sleep(500);
		jsClick(mapDriver, By.id("qt-ctrl-quickSwitch"));

		Thread.sleep(7000);
		takeSnapShot(mapDriver, tempShotPath);
		row.addNewItem(PIC.get(2), GoogleDriverHandler.savePicToGoogle(driveService, countyName,
				row.getItem(LAND_ID).getItemValue() + PIC.get(2)));
		zoomIn(4);
		Thread.sleep(2000);
		takeSnapShot(mapDriver, tempShotPath);
		row.addNewItem(PIC.get(3), GoogleDriverHandler.savePicToGoogle(driveService, countyName,
				row.getItem(LAND_ID).getItemValue() + PIC.get(3)));
		Thread.sleep(2000);
		zoomIn(1);

	}

	public static void setHtmlHeader(WebDriver driver) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("v2"));
		WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tablecontext")));

		WebElement first_tr = table.findElement(By.tagName("thead")).findElement(By.tagName("tr"));

		htmlHeader = new OneToOneMap<String, Integer>();
		int cellIndex = 0;
		List<WebElement> tds = first_tr.findElements(By.tagName("td"));

		for (WebElement td : tds) {
			for (String column : columns) {
				String headerName = td.getText().split(newLine)[0];
				if (headerName.contains(column) && !headerName.equals("土地坐落")) {
//					System.out.println(headerName);
					htmlHeader.put(headerName, cellIndex);
					break;
				}
			}
			++cellIndex;
		}
		driver.switchTo().defaultContent();
	}

	public static void setHeader() {
		int numberOfColonHtml = 0;
		for (String column : columns) {
			if (!excelValue.getExcelHeaders().containsKey(column)) {
				excelValue.getExcelHeaders().put(column, numberOfColonHtml++);
			}
		}
	}

	public static String createLandId(String nameInLaw, String price) {
		// 把0754871號 移除最前面的0
		List<String> numbers = Arrays.asList(nameInLaw.split("\\D+"));
		List<String> new_numbers = new ArrayList<>();
		for (String number : numbers) {
			try {
				int number_dig = Integer.parseInt(number);
				new_numbers.add(String.valueOf(number_dig));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		String id = String.join("_", new_numbers);
		return id + (StringUtils.isBlank(price) ? "" : ("_" + price.replace(".0", "")));
	}

	/*
	 * 欄位設定
	 */
	public static final String LAND_ID = "id";
	public static final String PDF_URL = "pdf連結";
	public static final String SQUARE = "坪數";
	public static final String PRICE = "價錢(萬)";
	public static final String UNIT_PRICE = "萬/1坪";
	public static final String ADDRESS = "地址";
	public static final String JURDI_INFO = "法拍備註";
	public static final String COUNTY = "縣市";
	public static final String LAND_FOR = "國土利用現況調查";
	public static final String PIC = "圖片";
	public static final String MIN_SIZE = "(小)";
	public static final String MAX_SIZE = "(大)";
	public static final String SEARCH_MEMO = "查詢備註";
	public static final String[] columns = { "案號", "拍賣日期", "點交", "備註", "行政區", "經緯度", LAND_FOR, "面積", "使用分區", "使用地類別",
			"登記日期", "公告土地現值", SEARCH_MEMO, COUNTY, LAND_ID, PDF_URL, SQUARE, PRICE, UNIT_PRICE, ADDRESS, JURDI_INFO,
			PIC + MAX_SIZE, PIC + MIN_SIZE, PIC + MIN_SIZE + "(1)", PIC + MAX_SIZE + "(1)" };

}
