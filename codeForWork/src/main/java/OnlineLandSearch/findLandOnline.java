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
import static OnlineLandSearch.Jurdical.findLastPage;
import static OnlineLandSearch.Jurdical.findPDFUrl;
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
import java.util.List;
import java.util.Map;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
	public static String[] court = {  "嘉義" };

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
				System.out.println((PropertiesSetting.getPropertyByKey(courtName)));
				courtName = ConvertTai(courtName);
				// 初次進入網站才建立header:對應欄位index資料
				String countyName = "";
				int rowCount = GoogleSheetHandler.findSheetLastRow(sheetService, sheetFileId);
				boolean firstTime = false;
				outer: for (int landtype = 0; landtype < 2; ++landtype) {
					WebDriver driver = startJurdical();
					visitJurdical(excelValue, driver, courtName, landtype);
					System.out.println(excelValue.getExcelHeaders());
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
						GoogleSheetHandler.clearOneCol(sheetService, sheetFileId, searchmemoIndex + 1);
						initialConfig = true;
					}
					try {
						// 抓[最後一頁]的資料
						int lastPage = findLastPage(driver);
						for (int pageNum = 2; pageNum <= lastPage; ++pageNum) {
							
								LoopTr: for (WebElement tr : driver.findElements(By.xpath(trsXpath))) {
									try {
										List<WebElement> tds = tr.findElements(By.tagName("td"));
										Row row = excelValue.new Row();
										if (tds.size() >= 6) {
											String getElementString = new String(
													tds.get(5).getText().getBytes(Charset.forName("UTF-8")), "UTF-8");
											row = FifthCellHandler(getElementString, excelValue);
										}

										// 檢查一 不持分
										if (row != null) {
											/*
											 * 法網站拍表格資訊
											 */
											for (Map.Entry<String, Integer> en : htmlHeader.entrySet()) {
												String htmlTdtext = tds.get(en.getValue()).getText();
												if (!htmlTdtext.contains("度分秒")) {
													row.addNewItem(en.getKey(), tds.get(en.getValue()).getText());
												}
											}
											// id = 年分_字號_價格
											Item item_id = row.addNewItem(LAND_ID,
													createLandId(row.getItem(columns[0]).getItemValue(),
															row.getItem(PRICE).getItemValue()));
											/*
											 * 除了法網站拍表格資訊，另外加入pdf資料、土地ID、圖片資訊
											 */
											String pdfUrl = findPDFUrl(tr);
											row.addNewItem(PDF_URL, pdfUrl);
											// 確認雲端上的資料有無此案號
											// 因為header index starts from 0, so add 1 to find the char representative in
											// Google sheet
											if (!firstTime) {
												System.out.println("初次抓資料");
												int rowIndex = GoogleSheetHandler.findOneCol(sheetService, sheetFileId,
														excelValue.getExcelHeaders().get(LAND_ID) + 1,
														item_id.getItemValue());
												String rowRange = "!A" + rowIndex + ":Y" + rowIndex;
												List<List<Object>> list = sheetService.spreadsheets().values()
														.get(sheetFileId, rowRange).execute().getValues();
												if (list != null) {
													int priceIndex = excelValue.getExcelHeaders().get(PRICE);
													if (StringUtils
															.isBlank(list.get(0).get(searchmemoIndex).toString())) {
														List<List<Object>> toWrite = new ArrayList<>();
														List<Object> track = new ArrayList<>();
														track.add("仍存在");
														toWrite.add(track);
														GoogleSheetHandler.updateOneValue(sheetService, sheetFileId,
																searchmemoIndex + 1, rowIndex, toWrite);
													}
													if (!list.get(0).get(priceIndex)
															.equals(row.getItem(PRICE).getItemValue())) {
														GoogleSheetHandler.addOneRowOnSheet(sheetService, sheetFileId,
																rowIndex, excelValue.rowToStringArr(row));
													}
													// 如果已經有案號資料，且金額一樣則繼續檢查下一筆tr
													continue LoopTr;
												}
											}

											Double unit_Price = 0.;
											try {
												unit_Price = Double.parseDouble(row.getItem(UNIT_PRICE).getItemValue());
											} catch (Exception e) {
												e.printStackTrace();
											}
											// 檢查三 單價不高於9000/坪
											if (unit_Price <= 0.9) {

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
//						excelValue.addaRow(row);
														GoogleSheetHandler.addOneRowOnSheet(sheetService, sheetFileId,
																rowCount++, excelValue.rowToStringArr(row));
//						excelValue.print();
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
							

//						driver.switchTo().window(driver.getWindowHandle());
							toPage(driver, pageNum);
						}

					} catch (Exception e) {
						e.printStackTrace();
						driver.quit();
					}
					driver.quit();
					continue outer;
				}

			}
		} catch (InterruptedException | IOException | GeneralSecurityException e) {
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
		htmlHeader = new OneToOneMap<String, Integer>();
		int cellIndex = 0;
		for (WebElement td : driver
				.findElements(By.xpath("/html/body/form[1]/table[1]/tbody/tr[4]/td[2]/table/tbody/tr[1]/td"))) {
			for (String column : columns) {
				String headerName = td.getText().split(newLine)[0];
				if (headerName.contains(column) && !headerName.equals("土地坐落/面積")) {
					htmlHeader.put(headerName, cellIndex);
					break;
				}
			}
			++cellIndex;
		}
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
		String id = String.join("_", Arrays.asList(nameInLaw.split("\\D+")));
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
	public static final String[] columns = { "字號", "拍賣日期", "點交", "備 註", "行政區", "經緯度", LAND_FOR, "面積", "使用分區", "使用地類別",
			"登記日期", "公告土地現值", SEARCH_MEMO, COUNTY, LAND_ID, PDF_URL, SQUARE, PRICE, UNIT_PRICE, ADDRESS, JURDI_INFO,
			PIC + MAX_SIZE, PIC + MIN_SIZE, PIC + MIN_SIZE + "(1)", PIC + MAX_SIZE + "(1)" };

}
